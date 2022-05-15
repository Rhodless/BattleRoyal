package fr.rhodless.battleroyal.faction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class SpecialFaction {

    private UUID leader;
    private List<UUID> members;

}
