package webridge.mixins.events.record;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBlockData;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

import static net.minecraft.command.CommandBase.buildString;

@Mixin(CommandBlockData.class)
public abstract class CommandBlockDataMixin {
    @Inject(
            method = "execute",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V"
            )
    )
    private void recordBlockEdit(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local BlockPos blockPos, @Local IBlockState iBlockState) throws CommandException {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        // cant capture the right nbt, idk...
        NBTTagCompound nBTTagCompound3;
        try {
            nBTTagCompound3 = JsonToNBT.getTagFromJson(buildString(args, 3));
        } catch (NBTException var12) {
            throw new CommandException("commands.blockdata.tagError", var12.getMessage());
        }
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockPos, iBlockState, nBTTagCompound3);
    }
}
