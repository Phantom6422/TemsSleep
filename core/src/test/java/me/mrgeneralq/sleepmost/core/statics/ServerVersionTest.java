package me.mrgeneralq.sleepmost.core.statics;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServerVersionTest {

    @Test
    public void parsesYearBasedVersions() {
        assertEquals(ServerVersion.V26_1, ServerVersion.parse("26.1-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_2, ServerVersion.parse("26.2-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("26.3-R0.1-SNAPSHOT"));
        // Future 26.x builds must keep the newest known capability set.
        assertEquals(ServerVersion.V26_3, ServerVersion.parse("26.4-R0.1-SNAPSHOT"));
    }

    @Test
    public void parsesLegacyVersions() {
        assertEquals(ServerVersion.V1_8, ServerVersion.parse("1.8.8-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V1_16, ServerVersion.parse("1.16.5-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V1_20, ServerVersion.parse("1.20.6-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V1_21, ServerVersion.parse("1.21.11-R0.1-SNAPSHOT"));
        assertEquals(ServerVersion.V1_21, ServerVersion.parse("git-Paper-123 (MC: 1.21.11)"));
    }

    @Test
    public void yearBasedVersionsSupportTitles() {
        // This is the capability that gates the clock animation.
        assertTrue(ServerVersion.V26_3.supportsTitles());
        assertTrue(ServerVersion.V26_3.supportsBossBars());
        assertTrue(ServerVersion.V26_3.supportsGameRules());
        assertTrue(ServerVersion.V26_3.supportsClickableText());
        assertTrue(ServerVersion.V26_3.hasTimeSkipEvent());
    }

    @Test
    public void oldVersionsKeepTheirCapabilities() {
        assertFalse(ServerVersion.V1_8.supportsTitles());
        assertFalse(ServerVersion.V1_8.supportsGameRules());
        assertTrue(ServerVersion.V1_12.supportsTitles());
        assertTrue(ServerVersion.V1_15.hasTimeSkipEvent());
    }

    @Test
    public void rejectsGarbage() {
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(null));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse(""));
        assertEquals(ServerVersion.UNKNOWN, ServerVersion.parse("not-a-version"));
    }
}
