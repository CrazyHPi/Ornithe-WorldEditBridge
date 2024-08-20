package webridge.mixins.capturedDrops;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import webridge.helpers.CapturedDrops;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(
            method = "spawnAsEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    shift = At.Shift.AFTER
            )
    )
    private static void captureBlockItem(World worldIn, BlockPos pos, ItemStack stack, CallbackInfo ci, @Local EntityItem entityItem) {
        if (CapturedDrops.isCapturingDrops()) {
            CapturedDrops.captureDrop(entityItem);
        }
    }
}
