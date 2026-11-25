package webridge.mixins.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("dataFixer")
    DataFixer getDataFixer();
}
