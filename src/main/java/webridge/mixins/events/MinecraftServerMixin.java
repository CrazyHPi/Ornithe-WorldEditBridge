package webridge.mixins.events;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void loadServer(CallbackInfo ci) {
        WorldEditBridge.loadMinecraftServer((MinecraftServer) (Object) this);
        WorldEditBridge.isIntegratedServer = false;
    }

    @Inject(method = "loadAllWorlds", at = @At("HEAD"))
    private void onServerLoaded(CallbackInfo ci) {
        WorldEditBridge.onServerLoaded((MinecraftServer) (Object) this);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;updateTimeLightAndEntities()V",
                    shift = At.Shift.BEFORE
            )
    )
    private void onTick(CallbackInfo ci) {
        WorldEditBridge.onStartTick();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onServerStop(CallbackInfo ci) {
        WorldEditBridge.onServerStopped((MinecraftServer) (Object) this);
    }
}
