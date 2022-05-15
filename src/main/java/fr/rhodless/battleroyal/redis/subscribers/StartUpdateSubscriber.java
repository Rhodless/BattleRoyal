package fr.rhodless.battleroyal.redis.subscribers;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.faction.SpecialFaction;
import fr.rhodless.battleroyal.messaging.pigdin.IncomingPacketHandler;
import fr.rhodless.battleroyal.messaging.pigdin.PacketListener;
import fr.rhodless.battleroyal.redis.StartUpdatePacket;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StartUpdateSubscriber implements PacketListener {

    @IncomingPacketHandler
    public void onReceive(StartUpdatePacket packet) {
        ConcurrentHashMap<String, SpecialFaction> map = packet.getMembers();

        map.forEach((s, specialFaction) -> {
            if(specialFaction.getLeader() == null) {
                specialFaction.setLeader(specialFaction.getMembers().get(0));
                map.put(s, specialFaction);
            }
        });

        Main.getFactionManager().setMembers(map);

        int i = 0;

        for (Map.Entry<String, SpecialFaction> entry : packet.getMembers().entrySet()) {
            String factionName = entry.getKey();

            Location location = Main.getTeleportationPoints().get(i);
            Main.getFactionManager().getLocations().put(factionName, location);
            i++;
        }
    }

}
