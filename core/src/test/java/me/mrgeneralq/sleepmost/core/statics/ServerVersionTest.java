package me.mrgeneralq.sleepmost.core.statics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerVersionTest {

    @Test
    public void parsesSupportedPaperVersions() {
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("26.3-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("26.3.build.248-stable"));
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("git-Paper-123 (MC: 26.3)"));
        // Future 26.x releases keep the same supported API family.
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("26.4-R0.1-SNAPSHOT"));
    }

    @Test
    public void rejectsUnsupportedVersions() {
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.8.8-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.20.6-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("1.21.11-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("26.1-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("26.2-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("27.1-R0.1-SNAPSHOT"));
    }

    @Test
    public void only263IsSupported() {
        assertTrue(ServerVersion.V26_3.isSupported());
        assertFalse(ServerVersion.UNKNOWN.isSupported());
        assertEquals("26.3", ServerVersion.V26_3.getName());
        assertEquals("unknown", ServerVersion.UNKNOWN.getName());
    }

    @Test
    public void rejectsGarbage() {
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(null));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(""));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("not-a-version"));
    }
}
