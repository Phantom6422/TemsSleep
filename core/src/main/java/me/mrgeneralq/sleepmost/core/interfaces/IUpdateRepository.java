package me.mrgeneralq.sleepmost.core.interfaces;

public interface IUpdateRepository {

    /**
     * @return the latest version, fetching it when the cache is stale. May block.
     */
    String getLatestVersion();

    /**
     * @return the last successfully fetched version without any network I/O.
     */
    String getCachedVersion();

    /**
     * @return the human-readable URL for the repository's latest releases.
     */
    String getReleasesUrl();
}
