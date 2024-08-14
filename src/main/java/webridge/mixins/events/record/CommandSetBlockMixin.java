package webridge.mixins.events.record;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.server.CommandSetBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;
import webridge.helpers.CapturedDrops;

@Mixin(CommandSetBlock.class)
public abstract class CommandSetBlockMixin {
    NBTTagCompound worldEditTag;
    EntityPlayerMP worldEditPlayer;


    @Definition(id = "args", local = @Local(type = String[].class, argsOnly = true))
    @Expression("args.length >= 6")
    @Inject(method = "execute", at = @At(value = "MIXINEXTRAS:EXPRESSION"))
    private void setup(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local boolean flag, @Local NBTTagCompound nbttagcompound) {
        worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        worldEditTag = flag ? nbttagcompound : null;
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"
            )
    )
    private void preDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local BlockPos blockPos) {
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockPos, Blocks.AIR.getDefaultState(), worldEditTag);
        CapturedDrops.setCapturingDrops(true);
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;destroyBlock(Lnet/minecraft/util/math/BlockPos;Z)Z",
                    shift = At.Shift.AFTER
            )
    )
    private void postDestroy(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world) {
        CapturedDrops.setCapturingDrops(false);
        for (EntityItem drop : CapturedDrops.getCapturedDrops()) {
            WorldEditBridge.recordEntityCreation(worldEditPlayer, world, drop);
        }
        CapturedDrops.clearCapturedDrops();
    }

    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;",
                    ordinal = 0
            )
    )
    private void recordBlockEdit(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local BlockPos blockPos, @Local IBlockState iBlockState) {
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockPos, iBlockState, worldEditTag);
    }
}
