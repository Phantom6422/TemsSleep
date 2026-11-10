package me.mrgeneralq.sleepmost.core.repositories;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UpdateRepositoryTest {

    @Test
    public void parsesGithubTagWithPrefix() {
        assertEquals("5.9.0", UpdateRepository.parseTagVersion("{\"tag_name\":\"v5.9.0\",\"name\":\"v5.9.0\"}"));
        assertEquals("5.9.0", UpdateRepository.parseTagVersion("{\"tag_name\":\"V5.9.0\"}"));
        assertEquals("5.9.0", UpdateRepository.parseTagVersion("{\"tag_name\":\"5.9.0\"}"));
    }

    @Test
    public void rejectsInvalidTags() {
        assertNull(UpdateRepository.parseTagVersion(null));
        assertNull(UpdateRepository.parseTagVersion(""));
        assertNull(UpdateRepository.parseTagVersion("{\"message\":\"Not Found\"}"));
        assertNull(UpdateRepository.parseTagVersion("{\"tag_name\":\"latest\"}"));
        assertNull(UpdateRepository.parseTagVersion("{\"tag_name\":\"v5.9.0-beta\"}"));
    }

    @Test
    public void buildsReleasesUrl() {
        UpdateRepository repository = new UpdateRepository("Phantom6422/TemsSleep");
        assertEquals("https://github.com/Phantom6422/TemsSleep/releases/latest", repository.getReleasesUrl());
    }
}
