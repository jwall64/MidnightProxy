package dev.jwall64.mdproxy.resourcepack;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import dev.jwall64.mdcfg.serializer.SerializeResult;
import dev.jwall64.mdcfg.serializer.Serializer;

public final class ExtraSerializers {

    static final Serializer<URL> URL_SERIALIZER = Serializer.STRING.<URL>map(
        url -> SerializeResult.success(url.toString()), 
        str -> {
            try {
                return SerializeResult.success(new URI(str).toURL());
            } catch(MalformedURLException | URISyntaxException | IllegalArgumentException e) {
                return SerializeResult.failure("Invalid URL: " + str, e);
            }
        });


    static final Serializer<Pattern> PATTERN_SERIALIZER = Serializer.STRING.<Pattern>map(
        pattern -> SerializeResult.success(pattern.pattern()), 
        str -> {
            try {
                return SerializeResult.success(Pattern.compile(str));
            } catch(PatternSyntaxException e) {
                return SerializeResult.failure("Invalid regex pattern: " + str, e);
            }
        });
}
