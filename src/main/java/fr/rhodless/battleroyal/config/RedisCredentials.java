package fr.rhodless.battleroyal.config;

import fr.rhodless.battleroyal.Main;
import org.bukkit.configuration.file.FileConfiguration;
import redis.clients.jedis.JedisPool;

public class RedisCredentials {

    public static JedisPool getJedisPool() {
        FileConfiguration config = Main.getInstance().getConfig();

        if (config.getBoolean("redis.auth")) {
            return new JedisPool(
                    config.getString("redis.host"),
                    config.getInt("redis.port"),
                    config.getString("redis.user"),
                    config.getString("redis.password")
            );
        } else {
            return new JedisPool(
                    config.getString("redis.host"),
                    config.getInt("redis.port")
            );
        }

    }

    public static void init() {
        FileConfiguration config = Main.getInstance().getConfig();

        if (config.get("redis.host") == null) {
            config.set("redis.host", "127.0.0.1");
            Main.getInstance().saveConfig();
        }

        if (config.get("redis.port") == null) {
            config.set("redis.port", 6379);
            Main.getInstance().saveConfig();
        }

        if (config.get("redis.user") == null) {
            config.set("redis.user", "user");
            Main.getInstance().saveConfig();
        }

        if (config.get("redis.auth") == null) {
            config.set("redis.auth", false);
            Main.getInstance().saveConfig();
        }

        if (config.get("redis.password") == null) {
            config.set("redis.password", "mon_mot_de_passe");
            Main.getInstance().saveConfig();
        }
    }

}
