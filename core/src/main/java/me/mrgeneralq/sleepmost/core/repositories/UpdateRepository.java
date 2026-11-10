package me.mrgeneralq.sleepmost.core.repositories;

import me.mrgeneralq.sleepmost.core.interfaces.IUpdateRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the latest release from a public GitHub repository.
 * <p>
 * Only the {@code tag_name} field of the GitHub Releases API response is
 * consumed, so no JSON library is required and the parsed input stays tiny.
 */
public class UpdateRepository implements IUpdateRepository {

    private static final long CACHE_TIMER_IN_MS = 7_200_000L;
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 5_000;
    private static final int MAX_RESPONSE_BYTES = 65_536;

    private static final Pattern TAG_PATTERN = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)*");

    private final String repository;
    private volatile String cachedUpdateVersion;
    private volatile long lastChecked;

    public UpdateRepository(String repository) {
        this.repository = repository;
    }

    @Override
    public synchronized String getLatestVersion() {

        long now = System.currentTimeMillis();
        if (this.lastChecked != 0 && now - this.lastChecked < CACHE_TIMER_IN_MS) {
            return this.getCachedVersion();
        }

        // Mark the attempt before doing I/O so concurrent requests do not
        // stampede the remote endpoint.
        this.lastChecked = now;

        try {
            String version = requestLatestRelease();
            if (version != null) {
                this.cachedUpdateVersion = version;
            }
        } catch (IOException ignored) {
            // Keep the last known version and retry after the cache expires.
        }

        return this.getCachedVersion();
    }

    @Override
    public String getCachedVersion() {
        String cached = this.cachedUpdateVersion;
        return cached == null ? "unknown" : cached;
    }

    @Override
    public String getReleasesUrl() {
        return "https://github.com/" + this.repository + "/releases/latest";
    }

    private String requestLatestRelease() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI
                .create("https://api.github.com/repos/" + this.repository + "/releases/latest")
                .toURL()
                .openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Accept", "application/vnd.github+json");
        connection.setRequestProperty("User-Agent", "TemsSleep-UpdateChecker");

        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) {
            return null;
        }

        ByteArrayOutputStream body = new ByteArrayOutputStream(512);
        try (InputStream inputStream = connection.getInputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                if (body.size() + read > MAX_RESPONSE_BYTES) {
                    return null;
                }
                body.write(buffer, 0, read);
            }
        }

        return parseTagVersion(body.toString(StandardCharsets.UTF_8));
    }

    /**
     * Extracts and normalizes {@code tag_name} from a GitHub release payload.
     * Package-private for regression tests.
     */
    static String parseTagVersion(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        Matcher tagMatcher = TAG_PATTERN.matcher(json);
        if (!tagMatcher.find()) {
            return null;
        }

        String tag = tagMatcher.group(1).trim();
        if (tag.startsWith("v") || tag.startsWith("V")) {
            tag = tag.substring(1);
        }

        return VERSION_PATTERN.matcher(tag).matches() ? tag : null;
    }
}
