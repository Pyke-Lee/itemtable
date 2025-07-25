package io.github.pyke.data;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;
import java.util.logging.Level;

public class MySQLManager {
    private final JavaPlugin plugin;
    private static Connection connection;

    public MySQLManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                return true; // 이미 열려 있으면 재연결 안 함
            }

            String host = plugin.getConfig().getString("mysql.host");
            int port = plugin.getConfig().getInt("mysql.port");
            String database = plugin.getConfig().getString("mysql.database");
            String user = plugin.getConfig().getString("mysql.user");
            String password = plugin.getConfig().getString("mysql.password");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";

            connection = DriverManager.getConnection(url, user, password);
            plugin.getLogger().info("MySQL 연결 성공");
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
        try {
            if (connection == null || connection.isClosed()) {
                plugin.getLogger().warning("MySQL 연결이 닫혀 있음. 재연결 시도...");
                if (!connect()) {
                    plugin.getLogger().severe("MySQL 재연결 실패");
                    return null;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("연결 상태 확인 중 오류 발생: " + e.getMessage());
            if (!connect()) {
                plugin.getLogger().severe("예외 발생 후 재연결 실패");
                return null;
            }
        }

        return connection;
    }

    public boolean categoryExists(String name) {
        String sql = "SELECT 1 FROM item_categories WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().warning("카테고리 존재 확인 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean itemkeyExists(String category, String key) throws SQLException {
        String sql = "SELECT 1 FROM items WHERE category_name = ? AND item_key = ? LIMIT 1";
        Connection conn = getConnection();

        if (conn == null) {
            plugin.getLogger().severe("itemkeyExists(): MySQL 연결이 유효하지 않습니다.");
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, key);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL 아이템 키 중복 체크 실패", e);
            return false;
        }
    }

    public void addCategory(String name, ItemStack icon) {
        String sql = "INSERT INTO item_categories (name, icon_data) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setBytes(2, icon.serializeAsBytes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("카테고리 추가 실패: " + e.getMessage());
        }
    }

    public void deleteCategory(String name) {
        String sql = "DELETE FROM item_categories WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("카테고리 삭제 실패: " + e.getMessage());
        }
    }

    public void addItem(String category, String key, ItemStack item) {
        String sql = "INSERT INTO items (category_name, item_key, item_data) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE item_data = VALUES(item_data)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, key);
            stmt.setBytes(3, item.serializeAsBytes());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("아이템 추가 실패: " + e.getMessage());
        }
    }

    public void deleteItem(String category, String key) {
        String sql = "DELETE FROM items WHERE category_name = ? AND item_key = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category);
            stmt.setString(2, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("아이템 삭제 실패: " + e.getMessage());
        }
    }

    public ItemStack loadItem(String categoryName, String key) {
        String sql = "SELECT item_data FROM items WHERE category_name = ? AND item_key = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            stmt.setString(2, key);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                byte[] bytes = rs.getBytes("item_data");
                return ItemStack.deserializeBytes(bytes);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("아이템 로드 실패: " + e.getMessage());
        }
        return null;
    }
}
