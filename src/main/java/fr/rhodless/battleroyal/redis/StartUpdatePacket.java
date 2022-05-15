package fr.rhodless.battleroyal.redis;

import fr.rhodless.battleroyal.faction.SpecialFaction;
import fr.rhodless.battleroyal.messaging.pigdin.Packet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class StartUpdatePacket implements Packet {

    private final ConcurrentHashMap<String, SpecialFaction> members;

}
