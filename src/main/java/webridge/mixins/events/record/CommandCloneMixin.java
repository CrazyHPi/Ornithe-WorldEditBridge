package webridge.mixins.events.record;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.command.CommandClone;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(CommandClone.class)
public abstract class CommandCloneMixin {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",
                    ordinal = 1
            )
    )
    private void recordBlockEdit1(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local(name = "blockPos7") BlockPos blockPos7) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockPos7, Blocks.AIR.getDefaultState(), null);
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",
                    ordinal = 2
            )
    )
    private void recordBlockEdit2(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local CommandClone.StaticCloneData staticCloneData) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, staticCloneData.pos, staticCloneData.blockState, staticCloneData.nbt);

    }
}
