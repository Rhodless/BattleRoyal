package fr.rhodless.battleroyal.scoreboard;

import fr.rhodless.battleroyal.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
public class ScoreboardUtils {
    private final Map<UUID, ScoreboardModifier> scoreboards;

    public ScoreboardUtils() {
        scoreboards = new HashMap<>();
        int ipCharIndex = 0;
        int cooldown = 0;

        Main.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() ->
        {
            try {
                for (ScoreboardModifier scoreboard : scoreboards.values())
                    Main.getInstance().getExecutorMonoThread().execute(scoreboard::setLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 80, 80, TimeUnit.MILLISECONDS);

        Main.getInstance().getScheduledExecutorService().scheduleAtFixedRate(() ->
        {
            try {
                for (ScoreboardModifier scoreboard : scoreboards.values())
                    Main.getInstance().getExecutorMonoThread().execute(scoreboard::reloadData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void onDisable() {
        scoreboards.values().forEach(ScoreboardModifier::onLogout);
    }

    public void onLogin(Player player) {
        if (scoreboards.containsKey(player.getUniqueId())) {
            return;
        }
        scoreboards.put(player.getUniqueId(), new ScoreboardModifier(player));
    }

    public void onLogout(Player player) {
        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).onLogout();
            scoreboards.remove(player.getUniqueId());
        }
    }

    public void update(Player player) {
        if (scoreboards.containsKey(player.getUniqueId())) {
            scoreboards.get(player.getUniqueId()).reloadData();
        }
    }

}