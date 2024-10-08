package webridge.mixins.accessors;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkProviderServer.class)
public interface ChunkProviderServerAccessor {
    @Accessor("droppedChunks")
    Set<Long> getDroppedChunks();

    @Accessor("loadedChunks")
    Long2ObjectMap<Chunk> getLoadedChunks();
}
