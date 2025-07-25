package io.github.pyke;

import io.github.pyke.commands.ItemTableCmd;
import io.github.pyke.data.MySQLManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ItemTable extends JavaPlugin {
    private MySQLManager mySQLManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        mySQLManager = new MySQLManager(this);
        if (!mySQLManager.connect()) {
            getLogger().severe("MySQL 연결 실패로 플러그인을 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Objects.requireNonNull(getCommand("ItemTable")).setExecutor(new ItemTableCmd(this));
    }

    @Override
    public void onDisable() {
        mySQLManager.disconnect();
    }

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }
}