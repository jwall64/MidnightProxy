package dev.jwall64.mdproxy.jwt;

import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.codec.JSONCodec;
import dev.jwall64.mdcfg.serializer.ConfigContext;
import dev.jwall64.mdcfg.serializer.SerializeResult;

import java.io.ByteArrayInputStream;
import java.util.Base64;

public class JWTReader {

    public static SerializeResult<JWT> readAny(String s, KeySupplier keySupplier) {

        int firstPeriod = s.indexOf('.');
        if(firstPeriod == -1) {
            return SerializeResult.failure("Found malformed JWT!");
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        JSONCodec json = JSONCodec.minified();

        String headerStr = s.substring(0, s.indexOf('.'));

        ConfigSection header;
        try {
            header = json.decode(ConfigContext.INSTANCE, new ByteArrayInputStream(decoder.decode(headerStr))).asSection();
        } catch (Exception ex) {
            return SerializeResult.failure("Unable to read JWT header!");
        }

        // JWE
        if(header.hasString("enc")) {
            return JWESerializer.read(s, keySupplier);
        }
        // JWS
        else {
            return JWSSerializer.read(s, keySupplier);
        }
    }

}
