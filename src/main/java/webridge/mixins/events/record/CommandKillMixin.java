package webridge.mixins.events.record;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.command.CommandKill;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(CommandKill.class)
public abstract class CommandKillMixin {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "net/minecraft/command/CommandKill.notifyCommandListener(Lnet/minecraft/command/ICommandSender;Lnet/minecraft/command/ICommand;Ljava/lang/String;[Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private void recordEntityRemoval(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local Entity entity) {
        if (!(entity instanceof EntityPlayerMP)) {
            EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
            WorldEditBridge.recordEntityRemoval(worldEditPlayer, sender.getEntityWorld(), entity);
        }
    }
}
