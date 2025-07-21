package io.github.pyke.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.logging.Level;

import static io.github.pyke.util.ItemSerializer.serialize;

public class MySQLManager {
    private final JavaPlugin plugin;
    private static Connection connection;

    public MySQLManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        String host = plugin.getConfig().getString("mysql.host");
        int port = plugin.getConfig().getInt("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String user = plugin.getConfig().getString("mysql.user");
        String password = plugin.getConfig().getString("mysql.password");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC", user, password);
            plugin.getLogger().info("MySQL 연결 선공");
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL 연결 실패", e);
            return false;
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("MySQL 연결 종료");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL 연결 실패", e);
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean categoryExists(String categoryName) {
        String sql = "SELECT 1 FROM item_categories WHERE name = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL 카테고리 존재 여부 조회 실패", e);
            return false;
        }
    }

    public boolean itemkeyExists(String category, String key) {
        String sql = "SELECT 1 FROM items WHERE category_name = ? AND item_key = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, key);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL 아이템 키 중복 체크 실패", e);
            return false;
        }
    }

    public void addCategory(String name, ItemStack item) {
        String json = serialize(item);

        String sql = "INSERT INTO item_categories (name, icon_json) VALUES (?, ?)";
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, json);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "§c카테고리 추가 실패", e);
        }
    }

    public void deleteCategory(String categoryName) {
        String sql = "DELETE FROM item_categories WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "§c카테고리 삭제 실패", e);
        }
    }

    public void addItem(String name, String key, ItemStack item) {
        String json = serialize(item);

        String sql = "INSERT INTO items (category_name, item_key, item_data) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, key);
            stmt.setString(3, json);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "§c아이템 추가 실패", e);
        }
    }
}
