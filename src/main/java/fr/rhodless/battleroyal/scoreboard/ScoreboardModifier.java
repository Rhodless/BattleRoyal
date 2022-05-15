package fr.rhodless.battleroyal.scoreboard;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.faction.SpecialFaction;
import fr.rhodless.battleroyal.utils.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * This file is part of SamaGamesAPI.
 *
 * SamaGamesAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SamaGamesAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SamaGamesAPI.  If not, see <http://www.gnu.org/licenses/>.
 */
public class ScoreboardModifier {
    private final Player player;
    private final UUID uuid;
    private final ObjectiveSign toReturn;

    ScoreboardModifier(Player player) {
        this.player = player;
        uuid = player.getUniqueId();
        toReturn = new ObjectiveSign("sidebar", CC.translate(Main.getInstance().getScoreboardTitle()));

        reloadData();
        toReturn.addReceiver(player);
    }

    public void reloadData() {
    }

    public void setLines() {

        List<String> realLines = new ArrayList<>();
        List<String> lines = Main.getInstance().getScoreboardLines();

        for (String line : lines) {
            line = line.replace("<pseudo>", player.getName());
            line = line.replace("<players>", "" + getPlayers());
            line = line.replace("<teams>", "" + getTeams());
            line = line.replace("<leader>", "" + getLeader(player));

            realLines.add(CC.translate(line));
        }

        int i = 0;

        for (String realLine : realLines) {
            toReturn.setLine(i, realLine);
            i++;
        }

        toReturn.updateLines();
    }

    public int getPlayers() {
        int i = 0;
        for (Map.Entry<String, SpecialFaction> entry : Main.getFactionManager().getMembers().entrySet()) {
            SpecialFaction specialFaction = entry.getValue();
            i += specialFaction.getMembers().size();
            if (specialFaction.getLeader() != null) i++;
        }

        return i;
    }

    public String getLeader(Player player) {
        SpecialFaction specialFaction = Main.getFactionManager().getMembers().values()
                .stream()
                .filter(sp -> player.getUniqueId().equals(sp.getLeader()) || sp.getMembers().contains(player.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (specialFaction == null) return "&cAucun";

        return Bukkit.getOfflinePlayer(specialFaction.getLeader()).getName();
    }

    public int getTeams() {
        List<SpecialFaction> alive = new ArrayList<>();

        Main.getFactionManager().getMembers().forEach((s, specialFaction) -> {
            if(!(specialFaction.getLeader() == null && specialFaction.getMembers().size() == 0)) {
                alive.add(specialFaction);
            }
        });

        return alive.size();
    }

    public void onLogout() {
        toReturn.removeReceiver(Bukkit.getServer().getOfflinePlayer(uuid));
    }
}