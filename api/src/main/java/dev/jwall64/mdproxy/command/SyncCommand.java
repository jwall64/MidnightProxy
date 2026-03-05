package dev.jwall64.mdproxy.command;

public class SyncCommand implements CommandExecutor {
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.getProxy().storageProvider().reload(sender.getProxy());
        sender.sendMessage("Synchronized proxy storage");
    }
}
