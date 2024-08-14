package webridge.mixins.events.record;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandSummon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(CommandSummon.class)
public abstract class CommandSummonMixin {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;setLocationAndAngles(DDDFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void summonEntity(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local Entity entity) {
        // this injection point is the best we can do
        // for some reason, inject before notifyCommandListener() will fail to capture locals
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordEntityCreation(worldEditPlayer, world, entity);
    }
}
