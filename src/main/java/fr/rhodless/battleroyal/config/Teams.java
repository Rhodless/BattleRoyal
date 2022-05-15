package fr.rhodless.battleroyal.config;

import fr.rhodless.battleroyal.Main;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

@Getter
@RequiredArgsConstructor
public enum Teams {

    PLAYER("§7"),
    KING("§6§l");

    private final String display;

    public void addMember(Player player) {
        Team team = Main.getInstance().getScoreboard().getTeam(getDisplay());

        team.addEntry(player.getName());
    }

}
