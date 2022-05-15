package fr.rhodless.battleroyal.commands;

import fr.rhodless.battleroyal.Main;
import fr.rhodless.battleroyal.utils.CC;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BattleRoyalCommand implements CommandExecutor {

    private void sendHelpMessage(Player player) {
        player.sendMessage(CC.translate("&8&m-------------------"));
        player.sendMessage(CC.translate("&8- &a/br help&8: &fAfficher cette page"));
        player.sendMessage(CC.translate("&8- &a/br addtp&8: &fAjoute un point de tp"));
        player.sendMessage(CC.translate("&8- &a/br setinventory&8: &fChange l'inventaire"));
        player.sendMessage(CC.translate("&8- &a/br setdeathinventory&8: &fChange l'inventaire de mort"));
        player.sendMessage(CC.translate("&8&m-------------------"));
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        Player player = (Player) commandSender;

        if(args.length == 0) {
            sendHelpMessage(player);
            return false;
        }

        if(args[0].equalsIgnoreCase("addtp")) {
            List<Location> locations = Main.getTeleportationPoints();
            locations.add(player.getLocation());

            Main.setTeleportationPoints(locations);
            Main.getInstance().getConfig().set("TELEPORT-POINTS", locations);

            Main.getInstance().saveConfig();

            player.sendMessage(CC.translate("&aVous avez ajouté un point de téléportation"));
        } else if(args[0].equalsIgnoreCase("setinventory")) {
            List<ItemStack> inventory =  Arrays.asList(player.getInventory().getContents());
            List<ItemStack> armor =  Arrays.asList(player.getInventory().getArmorContents());

            Main.setStartInventory(inventory);
            Main.setStartArmor(armor);

            Main.getInstance().getConfig().set("INVENTORY", inventory);
            Main.getInstance().getConfig().set("INVENTORY-ARMOR", armor);

            Main.getInstance().saveConfig();

            player.sendMessage(CC.translate("&aVous avez changé l'inventaire de départ"));
        } else if(args[0].equalsIgnoreCase("setdeathinventory")) {
            List<ItemStack> inventory =  Arrays.asList(player.getInventory().getContents());

            Main.setDeathInventory(inventory);

            Main.getInstance().getConfig().set("DEATH-INVENTORY", inventory);

            Main.getInstance().saveConfig();

            player.sendMessage(CC.translate("&aVous avez changé l'inventaire de mort"));
        } else {
            sendHelpMessage(player);
        }

        return false;
    }
}
