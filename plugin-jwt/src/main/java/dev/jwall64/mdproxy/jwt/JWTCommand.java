package dev.jwall64.mdproxy.jwt;

import dev.jwall64.jwt.Algorithm;
import dev.jwall64.jwt.JsonWebKey;
import dev.jwall64.mdproxy.command.ArgumentParser;
import dev.jwall64.mdproxy.command.CommandExecutor;
import dev.jwall64.mdproxy.command.CommandSender;

import java.util.Set;

public class JWTCommand implements CommandExecutor {

    private final Set<String> subcommands = Set.of("genKey", "clearKey");

    private final ArgumentParser parser = new ArgumentParser()
            .addOption("alg", 'a', "HS256");

    @Override
    public void execute(CommandSender sender, String[] args) {

        ArgumentParser.ParseResult res = parser.parse(args);
        if(res.isError()) {
            sender.sendMessage(res.getError());
            return;
        }

        ArgumentParser.Parsed parsed = res.getOutput();

        String subcommand;
        if(parsed.getPositionalArgumentCount() < 3 || !subcommands.contains((subcommand = parsed.getPositionalArgument(1)))) {
            sender.sendMessage("Usage: " + args[0] + " genKey/clearKey <keyName> [-a <algorithm>]");
            return;
        }

        String name = parsed.getPositionalArgument(2);
        KeyStore store = sender.getProxy().getPluginManager().get(JWTPlugin.class).getKeyStore();

        switch (subcommand) {
            case "genKey":

                Algorithm alg = Algorithm.byId(parsed.getValue("alg"));
                if(alg == null) {
                    sender.sendMessage("Invalid algorithm");
                    return;
                }

                JsonWebKey key = new JsonWebKey.Builder()
                    .withKeyId(name)
                    .withUse(alg.use())
                    .withAlgorithm(alg)
                    .generate(alg.type());

                store.put(name, key);

                sender.sendMessage("New key " + name + " generated");
                break;

            case "clearKey":

                store.clear(name);
                sender.sendMessage("Key " + name + " cleared");
                break;
        }
    }
}
