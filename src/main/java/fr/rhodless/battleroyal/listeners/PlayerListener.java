package fr.rhodless.battleroyal.listeners;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.faction.SpecialFaction;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);
        Main.getScoreboardManager().onLogout(player);

        Main.getInstance().handleDeath(player, true, null);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        event.getDrops().clear();
        player.getInventory().clear();
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().setArmorContents(null);
        event.setDeathMessage(null);
        event.setDroppedExp(0);

        if (event.getEntity().getKiller() != null) {
            for (ItemStack itemStack : Main.getDeathInventory()) {
                event.getEntity().getKiller().getInventory().addItem(itemStack);
            }
        }

        Main.getInstance().handleDeath(player, false, player.getLocation());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);
        Main.getScoreboardManager().onLogin(player);

        SpecialFaction specialFaction = getFaction(player);

        if (specialFaction == null) {
            Main.getInstance().addSpectator(player);
            return;
        }

        String factionName = Main.getFactionManager().getMembers().keySet()
                .stream()
                .filter(s -> Main.getFactionManager().getMembers().get(s).equals(specialFaction))
                .findFirst()
                .orElse(null);

        player.setGameMode(GameMode.SURVIVAL);
        player.teleport(Main.getFactionManager().getLocations().get(factionName));
        player.getInventory().setContents(Main.getStartInventory().toArray(new ItemStack[0]));
        player.getInventory().setArmorContents(Main.getStartArmor().toArray(new ItemStack[0]));

        Main.getInstance().setupUsername(player);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player damager = (Player) event.getDamager();
        Player player = (Player) event.getEntity();

        SpecialFaction first = Main.getFactionManager().getMembers().values()
                .stream()
                .filter(sp -> sp.getMembers().contains(player.getUniqueId())
                        || player.getUniqueId().equals(sp.getLeader()))
                .findFirst()
                .orElse(null);

        SpecialFaction second = Main.getFactionManager().getMembers().values()
                .stream()
                .filter(sp -> sp.getMembers().contains(damager.getUniqueId())
                        || damager.getUniqueId().equals(sp.getLeader()))
                .findFirst()
                .orElse(null);

        if (second == null || first == null) return;
        if (first.getLeader() != null && first.getLeader().equals(second.getLeader())) event.setCancelled(true);
    }

    public SpecialFaction getFaction(Player player) {

        for (Map.Entry<String, SpecialFaction> entry : Main.getFactionManager().getMembers().entrySet()) {
            SpecialFaction sp = entry   .getValue();
            if (sp.getMembers().contains(player.getUniqueId()) || sp.getLeader().equals(player.getUniqueId())) {
                return sp;
            }
        }

        return null;
    }

}
