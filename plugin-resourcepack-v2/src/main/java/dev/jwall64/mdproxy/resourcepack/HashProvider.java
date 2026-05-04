package dev.jwall64.mdproxy.resourcepack;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.jwall64.mdcfg.registry.Registry;
import dev.jwall64.mdcfg.serializer.ObjectSerializer;
import dev.jwall64.mdcfg.serializer.Serializer;

public interface HashProvider {

    static final Logger LOGGER = LoggerFactory.getLogger("HashProvider");

    static final Registry<String, HashProviderType> TYPES = Registry.createStringRegistry();
    static final Serializer<HashProvider> SERIALIZER = TYPES.byIdSerializer().fieldOf("type").<HashProvider>dispatch(
        HashProviderType::serializer,
        HashProvider::type
    ).or(Known.SERIALIZER.cast(Known.class, HashProvider.class));
 
    record HashProviderType(Serializer<? extends HashProvider> serializer) {}

    CompletableFuture<String> getHash(ResourcePackEntry entry, Executor executor);

    HashProviderType type();


    default String getHashNow(ResourcePackEntry entry, Executor executor, long timeoutMillis) {
        return getHash(entry, executor).orTimeout(timeoutMillis, TimeUnit.MILLISECONDS).join();
    }

    default HashProvider memoize() {
        return new Memoized(this);
    }

    public record Known(String hash) implements HashProvider {
        @Override
        public CompletableFuture<String> getHash(ResourcePackEntry entry, Executor unused) {
            return CompletableFuture.completedFuture(hash);
        }

        @Override
        public HashProviderType type() {
            return TYPE;
        }

        public static final Serializer<Known> SERIALIZER = Serializer.STRING.flatMap(Known::hash, Known::new);
        public static final HashProviderType TYPE = new HashProviderType(SERIALIZER.fieldOf("hash"));

    }

    public record Query(URL url, Pattern pattern, int group, int maxResponseLength, Map<String, String> headers) implements HashProvider {

        public static final Pattern RAW_PATTERN = Pattern.compile("^[0-9a-fA-F]{40}$");

        public Query(URL url) {
            this(url, RAW_PATTERN, 0, 40, Map.of());
        }

        @Override
        public CompletableFuture<String> getHash(ResourcePackEntry entry, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    for(Map.Entry<String, String> header : headers.entrySet()) {
                        connection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    connection.connect();

                    if(connection.getResponseCode() != 200) {
                        LOGGER.warn("Failed to query hash for resource pack, got response code {}", connection.getResponseCode());
                        return null;
                    }

                    if(connection.getContentLengthLong() > maxResponseLength) {
                        LOGGER.warn("Failed to query hash for resource pack, response too long");
                        return null;
                    }

                    return pattern.matcher(new String(connection.getInputStream().readAllBytes())).results().findFirst().map(match -> {
                        if(match.groupCount() < group) {
                            LOGGER.warn("Failed to query hash for resource pack, got invalid hash response");
                            return null;
                        }
                        return match.group(group);
                    }).orElseGet(() -> {
                        LOGGER.warn("Failed to query hash for resource pack, got invalid hash response");
                        return null;
                    });

                } catch(IOException e) {
                    LOGGER.warn("Failed to query hash for resource pack", e);
                    return null;
                }
            }, executor);
        }

        @Override
        public HashProviderType type() {
            return TYPE;
        }

        public static final Serializer<Query> SERIALIZER =
            ObjectSerializer.create(
                ExtraSerializers.URL_SERIALIZER.entry("url", Query::url),
                ExtraSerializers.PATTERN_SERIALIZER.entry("response_pattern", Query::pattern).orElse(RAW_PATTERN),
                Serializer.INT.entry("response_group", Query::group).orElse(0),
                Serializer.INT.entry("max_response_length", Query::group).orElse(40),
                Serializer.STRING.mapOf().entry("request_headers", Query::headers).orElse(Map.of()),
                Query::new
            );

        public static final HashProviderType TYPE = new HashProviderType(SERIALIZER);
    }


    public record Header(URL url, String header, Pattern pattern, int group, Map<String, String> headers) implements HashProvider {

        public static final Pattern SHA1_PATTERN = Pattern.compile("sha1=([0-9a-fA-F]{40})");
        public static final String DEFAULT_HEADER = "Content-Digest";
        public static final Map<String, String> DEFAULT_HEADERS = Map.of("Want-Content-Digest", "sha1=9");

        public Header(URL url) {
            this(url, DEFAULT_HEADER, SHA1_PATTERN, 1, Map.of());
        }

        @Override
        public CompletableFuture<String> getHash(ResourcePackEntry entry, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    for(Map.Entry<String, String> header : headers.entrySet()) {
                        connection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    connection.setRequestMethod("HEAD");
                    connection.connect();

                    if(connection.getResponseCode() != 200) {
                        LOGGER.warn("Failed to query hash for resource pack, got response code {}", connection.getResponseCode());
                        return null;
                    }

                    String head = connection.getHeaderField(header);
                    if(head == null) {
                        LOGGER.warn("Failed to query hash for resource pack, missing hash header");
                        return null;
                    }

                    if(pattern == null) {
                        return head;
                    }

                    return pattern.matcher(head).results().findFirst().map(match -> {
                        if(match.groupCount() < group) {
                            LOGGER.warn("Failed to query hash for resource pack, got invalid hash header {}", head);
                            return null;
                        }
                        return match.group(group);
                    }).orElseGet(() -> { 
                        LOGGER.warn("Failed to query hash for resource pack, hash header did not match pattern: {}", head);
                        return null; 
                    });

                } catch(IOException e) {
                    LOGGER.warn("Failed to query hash for resource pack", e);
                    return null;
                }
            }, executor);
        }

        @Override
        public HashProviderType type() {
            return TYPE;
        }


        public static final Serializer<Header> SERIALIZER =
            ObjectSerializer.create(
                ExtraSerializers.URL_SERIALIZER.entry("url", Header::url),
                Serializer.STRING.entry("response_header", Header::header).orElse(DEFAULT_HEADER),
                ExtraSerializers.PATTERN_SERIALIZER.entry("response_pattern", Header::pattern).orElse(SHA1_PATTERN),
                Serializer.INT.entry("response_group", Header::group).orElse(1),
                Serializer.STRING.mapOf().entry("request_headers", Header::headers).orElse(Map.of()),
                Header::new
            );

        public static final HashProviderType TYPE = new HashProviderType(SERIALIZER);
    }

    public record Compute(Map<String, String> headers) implements HashProvider {

        @Override
        public CompletableFuture<String> getHash(ResourcePackEntry entry, Executor executor) {
            return CompletableFuture.supplyAsync(() -> {

                try {
                    HttpURLConnection connection = (HttpURLConnection) entry.resolveUrl().openConnection();
                    for(Map.Entry<String, String> header : headers.entrySet()) {
                        connection.setRequestProperty(header.getKey(), header.getValue());
                    }
                    connection.connect();

                    if(connection.getResponseCode() != 200) {
                        LOGGER.warn("Failed to query hash for resource pack, got response code {}", connection.getResponseCode());
                        return null;
                    }

                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    try(InputStream in = connection.getInputStream()) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while((read = in.read(buffer)) != -1) {
                            md.update(buffer, 0, read);
                        }
                    }
                    return HexFormat.of().formatHex(md.digest());

                } catch(IOException | GeneralSecurityException e) {
                    LOGGER.warn("Failed to query hash for resource pack", e);
                    return null;
                }
            }, executor);
        }

        @Override
        public HashProviderType type() {
            return TYPE;
        }

        public static final Serializer<Compute> SERIALIZER =
            ObjectSerializer.create(
                Serializer.STRING.mapOf().entry("request_headers", Compute::headers).orElse(Map.of()),
                Compute::new
            );

        public static final HashProviderType TYPE = new HashProviderType(SERIALIZER);
    }



    public class Memoized implements HashProvider {

        private final HashProvider parent;

        private volatile String hash = null;
        private volatile CompletableFuture<String> hashFuture = null;

        public Memoized(HashProvider parent) {
            this.parent = parent;
        }

        public HashProvider parent() {
            return parent;
        }

        @Override
        public CompletableFuture<String> getHash(ResourcePackEntry entry, Executor executor) {
            if(hash != null) {
                return CompletableFuture.completedFuture(hash);
            }
            if(hashFuture == null) {
                synchronized(this) {
                    if(hashFuture == null) {
                        hashFuture = parent.getHash(entry, executor).thenApply(h -> {
                            hash = h;
                            hashFuture = null;
                            return h;
                        });
                    }
                }
            }
            return hashFuture;
        }

        @Override
        public HashProviderType type() {
            return parent.type();
        }

        public void precompute(ResourcePackEntry entry, Executor executor) {
            hash = null;
            getHash(entry, executor).thenAccept(h -> {
                if(h == null) {
                    LOGGER.warn("Failed to precompute hash for resource pack {}", entry.url());
                }
            });
        }

        public static final Serializer<Memoized> SERIALIZER = HashProvider.SERIALIZER.<Memoized>flatMap(
            Memoized::parent,
            Memoized::new
        );
    }
}
