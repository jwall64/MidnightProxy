package dev.jwall64.mdproxy.command;

public interface CommandExecutor {

    void execute(CommandSender sender, String[] args);

}
