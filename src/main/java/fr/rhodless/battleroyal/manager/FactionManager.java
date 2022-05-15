package fr.rhodless.battleroyal.manager;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.faction.SpecialFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class FactionManager {

    private HashMap<String, String> leaderName;
    private ConcurrentHashMap<String, SpecialFaction> members;
    private HashMap<String, Location> locations;

    public FactionManager(Main main) {
        this.members = new ConcurrentHashMap<>();
        this.locations = new HashMap<>();
    }

}
