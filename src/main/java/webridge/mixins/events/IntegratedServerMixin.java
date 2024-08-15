package webridge.mixins.events;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void onServerLoaded(CallbackInfo ci) {
        WorldEditBridge.onServerLoaded((MinecraftServer) (Object) this);
        WorldEditBridge.isIntegratedServer = true;
    }
}
