package dev.jwall64.mdproxy.command;

public class StopCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.getProxy().shutdown();
    }
}
