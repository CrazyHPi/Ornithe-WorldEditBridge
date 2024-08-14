package webridge.mixins.events;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import webridge.WorldEditBridge;

@Mixin(CommandHandler.class)
public abstract class CommandHandlerMixin {
    EntityPlayerMP worldEditPlayer;
    boolean weEnabledPre;
    boolean nonWorldEditCommand;

    @Definition(id = "j", local = @Local(type = int.class, name = "j"))
    @Expression("j > -1")
    @Inject(method = "executeCommand", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void onCommand(ICommandSender sender, String rawCommand, CallbackInfoReturnable<Integer> cir, @Local ICommand iCommand, @Local String[] strings) {
        WorldEditBridge.onCommand(iCommand, sender, strings);
    }

    @Definition(id = "i", local = @Local(type = int.class, name = "i"))
    @Expression("i = 0")
    @Inject(method = "executeCommand", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void startEditSession(ICommandSender sender, String rawCommand, CallbackInfoReturnable<Integer> cir, @Local ICommand iCommand) {
        worldEditPlayer = sender instanceof EntityPlayerMP ? (EntityPlayerMP) sender : null;
        weEnabledPre = WorldEditBridge.worldEditEnabled();
        nonWorldEditCommand = iCommand != null && !iCommand.getClass().getName().startsWith("webridge");
        if (nonWorldEditCommand) {
            WorldEditBridge.startEditSession(worldEditPlayer);
        }
    }

    @Inject(method = "executeCommand", at = @At("RETURN"))
    private void finishEditSession(ICommandSender sender, String rawCommand, CallbackInfoReturnable<Integer> cir){
        if (nonWorldEditCommand && weEnabledPre) {
            WorldEditBridge.finishEditSession(worldEditPlayer);
        }
    }
}
