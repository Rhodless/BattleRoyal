package fr.rhodless.battleroyal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.rhodless.battleroyal.commands.BattleRoyalCommand;
import fr.rhodless.battleroyal.config.Messages;
import fr.rhodless.battleroyal.config.RedisCredentials;
import fr.rhodless.battleroyal.config.Teams;
import fr.rhodless.battleroyal.faction.SpecialFaction;
import fr.rhodless.battleroyal.listeners.PlayerListener;
import fr.rhodless.battleroyal.manager.FactionManager;
import fr.rhodless.battleroyal.messaging.Pidgin;
import fr.rhodless.battleroyal.redis.StartUpdatePacket;
import fr.rhodless.battleroyal.redis.subscribers.StartUpdateSubscriber;
import fr.rhodless.battleroyal.scoreboard.ScoreboardUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@SuppressWarnings({"unchecked"})
public class Main extends JavaPlugin implements PluginMessageListener {

    @Getter
    private static Main instance;
    @Getter
    private static FactionManager factionManager;
    @Getter
    private static Pidgin pidgin;
    @Getter
    @Setter
    private static List<Location> teleportationPoints;
    @Getter
    @Setter
    private static List<ItemStack> startInventory;
    @Getter
    @Setter
    private static List<ItemStack> startArmor;
    @Getter
    @Setter
    private static List<ItemStack> deathInventory;
    @Getter
    private static ScoreboardUtils scoreboardManager;

    private String scoreboardTitle;
    private List<String> scoreboardLines;
    private ScheduledExecutorService executorMonoThread;
    private ScheduledExecutorService scheduledExecutorService;
    private Scoreboard scoreboard;

    @Override
    public void onEnable() {
        this.saveConfig();
        instance = this;

        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        this.scheduledExecutorService = Executors.newScheduledThreadPool(2);
        this.executorMonoThread = Executors.newScheduledThreadPool(1);

        scoreboardManager = new ScoreboardUtils();
        factionManager = new FactionManager(this);
        pidgin = new Pidgin("battle-royal", RedisCredentials.getJedisPool());
        teleportationPoints = (List<Location>) getConfig().getList("TELEPORT-POINTS");
        startInventory = (List<ItemStack>) getConfig().getList("INVENTORY");
        deathInventory = (List<ItemStack>) getConfig().getList("DEATH-INVENTORY");
        startArmor = (List<ItemStack>) getConfig().getList("INVENTORY-ARMOR");

        this.scoreboardLines = getConfig().getStringList("SCOREBOARD");
        this.scoreboardTitle = getConfig().getString("SCOREBOARD-TITLE");
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.createTeams();

        Messages.init();
        RedisCredentials.init();
        this.registerPackets();
        this.registerSubscribers();
        this.registerListeners();

        getCommand("br").setExecutor(new BattleRoyalCommand());
    }

    public void registerPackets() {
        pidgin.registerPacket(StartUpdatePacket.class);
    }

    public void registerSubscribers() {
        pidgin.registerListener(new StartUpdateSubscriber());
    }

    public void registerListeners() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        pluginManager.registerEvents(new PlayerListener(), this);
    }


    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        final ByteArrayDataInput in = ByteStreams.newDataInput(message);
        in.readUTF();
    }

    public static void sendToServer(Player player, String server) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
    }

    public void handleDeath(Player player, boolean disconnect, Location location) {
        for (String s : Main.getFactionManager().getMembers().keySet()) {
            SpecialFaction specialFaction = Main.getFactionManager().getMembers().get(s);
            List<UUID> members = specialFaction.getMembers();

            if (members.contains(player.getUniqueId()) || player.getUniqueId().equals(specialFaction.getLeader())) {
                if (player.getUniqueId().equals(specialFaction.getLeader())) {
                    eliminateTeam(player);
                    specialFaction.setLeader(null);
                } else {
                    members.remove(player.getUniqueId());
                }

                if (disconnect) {
                    Bukkit.getOnlinePlayers().forEach(player1 -> Messages.MORT_DECO.send(player1,
                            new Messages.Replacement("<player>", player.getName())
                    ));
                } else {
                    Bukkit.getOnlinePlayers().forEach(player1 -> Messages.MORT.send(player1,
                            new Messages.Replacement("<player>", player.getName())
                    ));
                }

                Main.getInstance().checkWin();
                Main.getFactionManager().getMembers().put(s, specialFaction);
            }
        }

        if (!disconnect) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                player.spigot().respawn();
                player.teleport(location);
                Main.getInstance().addSpectator(player);
            }, 15);
        }
    }

    public void checkWin() {
        List<SpecialFaction> alive = new ArrayList<>();

        Main.getFactionManager().getMembers().forEach((s, specialFaction) -> {
            if (!(specialFaction.getLeader() == null && specialFaction.getMembers().size() == 0)) {
                alive.add(specialFaction);
            }
        });

        if (alive.size() == 0) {
            Bukkit.getOnlinePlayers().forEach(Messages.WIN_NOBODY::send);
            stop();
        } else if (alive.size() == 1) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                String winner;
                if (alive.get(0).getLeader() != null) winner = Bukkit.getPlayer(alive.get(0).getLeader()).getName();
                else winner = Bukkit.getPlayer(alive.get(0).getMembers().get(0)).getName();
                Messages.WIN_TEAM.send(player, new Messages.Replacement("<winner>", winner));
            });
            stop();
        }
    }

    public void stop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getServer().shutdown();
            }
        }.runTaskLater(this, 10 * 20);
    }

    public void eliminateTeam(Player leader) {
        SpecialFaction specialFaction = Main.getFactionManager()
                .getMembers()
                .values()
                .stream()
                .filter(s -> leader.getUniqueId().equals(s.getLeader()))
                .findFirst()
                .orElse(null);

        if (specialFaction == null) return;

        specialFaction.getMembers().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            player.setHealth(0);
        });
    }

    public void addSpectator(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public void createTeams() {
        Arrays.stream(Teams.values()).forEach(teams -> {
            if (scoreboard.getTeam(teams.getDisplay()) == null) {
                Team team = scoreboard.registerNewTeam(teams.getDisplay());
                team.setPrefix(teams.getDisplay());
            }
        });
    }

    public void setupUsername(Player player) {
        boolean captain = getFactionManager().getMembers().values().stream()
                .anyMatch(specialFaction -> (player.getUniqueId()).equals(specialFaction.getLeader()));

        if (captain) {
            Teams.KING.addMember(player);
        } else {
            Teams.PLAYER.addMember(player);
        }
    }
}
