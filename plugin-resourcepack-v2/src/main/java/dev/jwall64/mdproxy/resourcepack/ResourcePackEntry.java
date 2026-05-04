package dev.jwall64.mdproxy.resourcepack;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;
import dev.jwall64.mdproxy.requirement.ConnectionRequirement;
import dev.jwall64.mdproxy.util.MessageUtil;
import dev.jwall64.pseudonym.ParameterTransformer;
import dev.jwall64.pseudonym.PartialMessage;
import dev.jwall64.pseudonym.PipelineContext;
import dev.jwall64.pseudonym.Placeholder;
import dev.jwall64.pseudonym.text.Component;

public record ResourcePackEntry(UUID uuid, UrlTemplate url, HashProvider.Memoized hash, boolean required, @Nullable Component message, @Nullable ConnectionRequirement requirement) {
   
    public void reload(Executor executor) {
        hash.precompute(this, executor);
    }

    public URL resolveUrl() {
        return url.resolve(this);
    }

    public static Serializer<ResourcePackEntry> SERIALIZER = ObjectSerializer.create(
            Serializer.UUID.entry("uuid", ResourcePackEntry::uuid),
            UrlTemplate.SERIALIZER.entry("url", ResourcePackEntry::url),
            HashProvider.Memoized.SERIALIZER.entry("hash", ResourcePackEntry::hash),
            Serializer.BOOLEAN.entry("required", ResourcePackEntry::required).orElse(false),
            Component.SERIALIZER.entry("message", ResourcePackEntry::message).optional(),
            ConnectionRequirement.SERIALIZER.entry("requirement", ResourcePackEntry::requirement).optional(),
            ResourcePackEntry::new
    ); 


    public static class UrlTemplate {
        
        private PartialMessage<String> template;

        public UrlTemplate(PartialMessage<String> template) {
            this.template = template;
        }

        public URL resolve(ResourcePackEntry entry) {
            try {
                PipelineContext ctx = PipelineContext.builder()
                    .withContextPlaceholder(new Placeholder<>("pack_hash", String.class, pCtx -> {
                        String hash = entry.hash().getHashNow(entry, Runnable::run, 10000L);
                        if(hash == null) return Optional.empty();
                        return Optional.of(hash);
                    }, ParameterTransformer.IDENTITY))
                    .build();
                String resolved = PartialMessage.resolve(template, ctx);
                return new URI(resolved).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw new RuntimeException("Unable to resolve URL for resource pack entry " + entry.uuid(), e);
            }
        }

        public static final Serializer<UrlTemplate> SERIALIZER = MessageUtil.PARSE_SERIALIZER.flatMap(
            t -> t.template, 
            UrlTemplate::new
        );

    }

}
