package net.vulkanmod.mixin.util;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Long2ByteOpenHashMap.class)
public interface Long2ByteOpenHashMapAccessor {
    @Invoker("ensureCapacity")
    void callEnsureCapacity(int capacity);
}
