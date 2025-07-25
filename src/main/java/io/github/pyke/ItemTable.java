package io.github.pyke;

import io.github.pyke.commands.ItemTableCmd;
import io.github.pyke.data.MySQLManager;
import io.github.pyke.listener.PaginatedListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public class ItemTable extends JavaPlugin {
    private MySQLManager mySQLManager;
    private PaginatedListener paginatedListener;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        mySQLManager = new MySQLManager(this);
        if (!mySQLManager.connect()) {
            getLogger().severe("MySQL 연결 실패로 플러그인을 비활성화합니다.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        paginatedListener = new PaginatedListener();
        getServer().getPluginManager().registerEvents(paginatedListener, this);

        Objects.requireNonNull(getCommand("ItemTable")).setExecutor(new ItemTableCmd(this));
    }

    @Override
    public void onDisable() {
        mySQLManager.disconnect();
        paginatedListener.clearAll();
    }

    public MySQLManager getMySQLManager() {
        return mySQLManager;
    }

    public PaginatedListener getPaginatedListener() {
        return paginatedListener;
    }
}
