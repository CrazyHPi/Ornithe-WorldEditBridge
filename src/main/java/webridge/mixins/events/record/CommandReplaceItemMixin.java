package webridge.mixins.events.record;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.command.CommandReplaceItem;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.WorldEditBridge;

@Mixin(CommandReplaceItem.class)
public abstract class CommandReplaceItemMixin {
    @Definition(id = "iInventory", local = @Local(type = IInventory.class))
    @Definition(id = "IInventory", type = IInventory.class)
    @Definition(id = "tileEntity", local = @Local(type = TileEntity.class))
    @Expression("iInventory = (IInventory) tileEntity")
    @Inject(method = "execute", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void recordBlockEdit(MinecraftServer server, ICommandSender sender, String[] args, CallbackInfo ci, @Local World world, @Local BlockPos blockPos, @Local TileEntity tileEntity) {
        EntityPlayerMP worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        WorldEditBridge.recordBlockEdit(worldEditPlayer, world, blockPos, world.getBlockState(blockPos), tileEntity.writeToNBT(new NBTTagCompound()));
    }
}
