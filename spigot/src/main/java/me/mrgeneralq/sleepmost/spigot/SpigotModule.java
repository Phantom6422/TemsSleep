package me.mrgeneralq.sleepmost.spigot;

import me.mrgeneralq.sleepmost.core.interfaces.IPlatformModule;
import me.mrgeneralq.sleepmost.core.providers.GameRuleProvider;
import me.mrgeneralq.sleepmost.spigot.services.SpigotGameRuleService;

public class SpigotModule implements IPlatformModule {

    @Override
    public void setup() {
        // Never install the Spigot implementation on Paper: the GameRule.ADVANCE_TIME
        // field no longer exists there, while PaperModule provides GameRules.ADVANCE_TIME.
        if (isPaper()) {
            return;
        }
        if (GameRuleProvider.get() == null) {
            GameRuleProvider.registerGameRuleService(new SpigotGameRuleService());
        }
    }

    private static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
