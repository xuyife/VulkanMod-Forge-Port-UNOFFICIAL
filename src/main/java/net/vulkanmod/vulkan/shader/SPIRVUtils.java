package net.vulkanmod.vulkan.shader;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.vulkanmod.Initializer;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.lwjgl.util.shaderc.ShadercIncludeResolveI;
import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultReleaseI;
import org.lwjgl.vulkan.VK12;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memASCII;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class SPIRVUtils {
    private static final boolean DEBUG = false;
    private static final boolean OPTIMIZATIONS = true;

    private static long compiler;
    private static long options;

    //The dedicated Includer and Releaser Inner Classes used to Initialise #include Support for ShaderC
    private static final ShaderIncluder SHADER_INCLUDER = new ShaderIncluder();
    private static final ShaderReleaser SHADER_RELEASER = new ShaderReleaser();
    private static final long pUserData = 0;

    private static ObjectArrayList<String> includePaths;

    private static float time = 0.0f;

    static {
        initCompiler();
    }

    private static void initCompiler() {
        compiler = shaderc_compiler_initialize();

        if(compiler == NULL) {
            throw new RuntimeException("Failed to create shader compiler");
        }

        options = shaderc_compile_options_initialize();

        if(options == NULL) {
            throw new RuntimeException("Failed to create compiler options");
        }

        if(OPTIMIZATIONS)
            shaderc_compile_options_set_optimization_level(options, shaderc_optimization_level_performance);

        if(DEBUG)
            shaderc_compile_options_set_generate_debug_info(options);

        shaderc_compile_options_set_target_env(options, shaderc_env_version_vulkan_1_2, VK12.VK_API_VERSION_1_2);
        shaderc_compile_options_set_include_callbacks(options, SHADER_INCLUDER, SHADER_RELEASER, pUserData);

        includePaths = new ObjectArrayList<>();
        addIncludePath("/assets/vulkanmod/shaders/include/", Lists.newArrayList("fog.glsl", "light.glsl", "matrix.glsl", "projection.glsl"));
    }

    public static void addIncludePath(String path, List<String> fileNames) {
        try {
            for (String fileName : fileNames) {
                String fullPath = path + fileName;

                Initializer.LOGGER.info("Trying to load: {}", fullPath);

                try (InputStream is = SPIRVUtils.class.getResourceAsStream(fullPath)) {
                    if (is != null) {
                        byte[] data = is.readAllBytes();
                        Initializer.LOGGER.info("Loaded: {} ({} bytes)", fullPath, data.length);
                        includePaths.add(fullPath);
                    } else {
                        Initializer.LOGGER.error("Failed to load: {}", fullPath);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SPIRV compileShaderAbsoluteFile(String shaderFile, ShaderKind shaderKind) {
        try {
            String source = "";
            try (InputStream is = SPIRVUtils.class.getResourceAsStream(shaderFile)) {
                if (is != null) {
                    source = IOUtils.toString(is, StandardCharsets.UTF_8);
                } else {
                    Initializer.LOGGER.error("Shader resource not found: {}", shaderFile);
                }
            }
            return compileShader(shaderFile, source, shaderKind);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind) {
        long startTime = System.nanoTime();

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", options);

        if(result == NULL) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if(shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V:\n" + shaderc_result_get_error_message(result));
        }

        time += (System.nanoTime() - startTime) / 1000000.0f;

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    private static SPIRV readFromStream(InputStream inputStream) {
        try {
            byte[] bytes = inputStream.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes);
            buffer.position(0);

            return new SPIRV(MemoryUtil.memAddress(buffer), buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("unable to read inputStream");
    }

    public enum ShaderKind {
        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader),
        COMPUTE_SHADER(shaderc_glsl_compute_shader);

        private final int kind;

        ShaderKind(int kind) {
            this.kind = kind;
        }
    }

    private static class ShaderIncluder implements ShadercIncludeResolveI {
        private static final int MAX_PATH_LENGTH = 4096;

        @Override
        public long invoke(long user_data, long requested_source, int type,
                           long requesting_source, long include_depth) {
            if (requested_source == 0) {
                Initializer.LOGGER.error("ERROR: requested_source is NULL");
                return 0;
            }

            try {
                String requested = readStringSafe(requested_source);
                String requesting = requesting_source != 0 ? readStringSafe(requesting_source) : "unknown";

                Initializer.LOGGER.info("Include requested: {} from: {}", requested, requesting);

                byte[] content = findIncludeFile(requested);
                if (content == null) {
                    Initializer.LOGGER.error("ERROR: Include file not found: {}", requested);
                    return createErrorResult(requested);
                }

                return createIncludeResult(requested, content);

            } catch (Exception e) {
                Initializer.LOGGER.error("Exception in include callback: {}", e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }

        private String readStringSafe(long ptr) {
            if (ptr == 0) return null;
            return memASCII(ptr);
        }

        private byte[] findIncludeFile(String filename) throws IOException {
            Initializer.LOGGER.info("Trying to find include file: {}", filename);
            for (String includePath : includePaths) {
                if (includePath.endsWith(filename)) {
                    try (InputStream is = SPIRVUtils.ShaderIncluder.class.getResourceAsStream(includePath)) {
                        Initializer.LOGGER.info("Full path: {}", includePath);
                        if (is != null) {
                            Initializer.LOGGER.trace("Found include file: {} in path: {}", filename, includePath);
                            return is.readAllBytes();
                        }
                    }
                }
            }

            return null;
        }

        private long createIncludeResult(String filename, byte[] content) {

            try {
                MemoryStack stack = MemoryStack.stackGet();

                ShadercIncludeResult result = ShadercIncludeResult.malloc(stack);
                result.source_name(stack.ASCII(filename));
                result.content(stack.bytes(content));
                result.user_data(0);

                return result.address();

            } catch (Exception e) {
                Initializer.LOGGER.error("Error creating include result: {}", e.getMessage());
                e.printStackTrace();
                return 0;
            }
        }

        private long createErrorResult(String filename) {
            try {
                MemoryStack stack = MemoryStack.stackGet();
                String errorMsg = "ERROR: Cannot find include file: " + filename;
                byte[] errorBytes = errorMsg.getBytes(StandardCharsets.UTF_8);

                ShadercIncludeResult result = ShadercIncludeResult.malloc(stack);
                result.source_name(stack.ASCII(filename));
                result.content(stack.bytes(errorBytes));
                result.user_data(0);

                return result.address();
            } catch (Exception e) {
                return 0;
            }
        }
    }

    //TODO: Don't actually need the Releaser at all, (MemoryStack frees this for us)
    //But ShaderC won't let us create the Includer without a corresponding Releaser, (so we need it anyway)
    private static class ShaderReleaser implements ShadercIncludeResultReleaseI {

        @Override
        public void invoke(long user_data, long include_result) {
            //TODO:Maybe dump Shader Compiled Binaries here to a .Misc Diretcory to allow easy caching.recompilation...
        }
    }

    public static final class SPIRV implements NativeResource {

        private final long handle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode) {
            this.handle = handle;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode() {
            return bytecode;
        }

        @Override
        public void free() {
//            shaderc_result_release(handle);
            bytecode = null; // Help the GC
        }
    }

}