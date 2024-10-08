package webridge;

import com.sk89q.worldedit.util.command.CommandMapping;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Arrays;
import java.util.List;

class CommandWrapper extends CommandBase {
    private CommandMapping command;

    protected CommandWrapper(CommandMapping command) {
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getPrimaryAlias();
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList(command.getAllAliases());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
    }

    @Override
    public String getUsage(ICommandSender icommandsender) {
        return "/" + command.getPrimaryAlias() + " " + command.getDescription().getUsage();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public int compareTo(ICommand o) {
        return super.compareTo((ICommand) o);
    }
}
