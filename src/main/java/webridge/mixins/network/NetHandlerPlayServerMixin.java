package webridge.mixins.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.CarpetWorldEdit;
import webridge.WorldEditBridge;

@Mixin(NetHandlerPlayServer.class)
public abstract class NetHandlerPlayServerMixin {
    @Shadow
    public EntityPlayerMP player;

    @Inject(method = "processCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onWECUI(CPacketCustomPayload packetIn, CallbackInfo ci) {
        if (CarpetWorldEdit.CUI_PLUGIN_CHANNEL.equals(packetIn.getChannelName())) {
            PacketThreadUtil.checkThreadAndEnqueue(packetIn, (INetHandlerPlayServer) this, this.player.getServerWorld());
            WorldEditBridge.onCustomPayload(packetIn, player);
            ci.cancel();
        }
    }
}
