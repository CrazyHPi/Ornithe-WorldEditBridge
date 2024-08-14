package webridge.mixins.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import webridge.WorldEditBridge;

@Mixin(PlayerInteractionManager.class)
public abstract class PlayerInteractionManagerMixin {
    @Shadow
    public World world;

    @Shadow
    public EntityPlayerMP player;

    @Inject(method = "onBlockClicked", at = @At("HEAD"), cancellable = true)
    private void onPlayerLeftClick(BlockPos pos, EnumFacing side, CallbackInfo ci) {
        if (!WorldEditBridge.onLeftClickBlock(world, pos, player)) {
            player.connection.sendPacket(new SPacketBlockChange(world, pos));
            ci.cancel();
        }
    }

    @Inject(
            method = "processRightClickBlock",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/EntityPlayer;isSneaking()Z"
            ),
            cancellable = true
    )
    private void onPlayerRightClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, CallbackInfoReturnable<EnumActionResult> cir) {
        if (!WorldEditBridge.onRightClickBlock(world, pos, this.player)) {
            this.player.connection.sendPacket(new SPacketBlockChange(world, pos));
            cir.setReturnValue(EnumActionResult.FAIL);
        }
    }
}
