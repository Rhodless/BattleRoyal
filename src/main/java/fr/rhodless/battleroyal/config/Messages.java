package fr.rhodless.battleroyal.config;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.utils.CC;
import lombok.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

@Getter
@AllArgsConstructor
public enum Messages {

    PREFIX("&c&lSplifight &8&l» &f"),
    MORT_DECO("<prefix>&a<player> &fa été éliminé par &cdéconnexion&f."),
    MORT("<prefix>&a<player> &fa été éliminé."),
    WIN_NOBODY("<prefix>&cPersonne ne gagne la partie..."),
    WIN_TEAM("<prefix>&fVictoire de l'équipe de &a<winner>")

    ;

    @Setter
    private String display;

    public static String placeHolders(String message) {
        return message.replace("<prefix>", PREFIX.getDisplay());
    }

    public String send(Player player, Replacement... replacements) {
        String message = CC.translate(placeHolders(getDisplay()));
        for (Replacement replacement : replacements) {
            message = message.replace(replacement.getIndex(), replacement.getReplace());
        }

        if (display.equalsIgnoreCase("false")) return message;

        player.sendMessage(message);
        return message;
    }

    @SneakyThrows
    public static void init() {
        FileConfiguration config = Main.getInstance().getConfig();
        config.load(new File(Main.getInstance().getDataFolder() + "/config.yml"));

        for (Messages value : values()) {
            if (config.get("messages." + value.name()) == null) {
                config.set("messages." + value.name(), value.getDisplay());
                Main.getInstance().saveConfig();
            }

            value.setDisplay(config.getString("messages." + value.name()));
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Replacement {

        private final CharSequence index;
        private final CharSequence replace;

    }
}
