package io.github.pyke.commands;

import io.github.pyke.ItemTable;
import io.github.pyke.data.MySQLManager;
import io.github.pyke.gui.ItemTableCategoriesUI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class ItemTableCmd implements CommandExecutor {
    private final ItemTable plugin;

    public ItemTableCmd(ItemTable plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c플레이어만 사용할 수 있는 명령어입니다.");
            return true;
        }

        MySQLManager db = plugin.getMySQLManager();

        if (args.length == 0) {
            ItemTableCategoriesUI ui = new ItemTableCategoriesUI(plugin);
            ui.open(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("categories") && args.length == 3 && args[1].equalsIgnoreCase("add")) {
            String category = args[2];

            if (db.categoryExists(category)) {
                player.sendMessage("§c이미 존재하는 카테고리입니다: " + category);
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                player.sendMessage("§c아이템을 손에 들고 사용해주세요.");
                return true;
            }

            db.addCategory(category, item);
            player.sendMessage("§a카테고리 추가 완료: " + category);
            return true;
        }

        if (args[0].equalsIgnoreCase("item") && args.length == 4 && args[1].equalsIgnoreCase("add")) {
            String category = args[2];
            String key = args[3];

            if (!db.categoryExists(category)) {
                player.sendMessage("§c해당 카테고리가 존재하지 않습니다: " + category);
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                player.sendMessage("§c아이템을 손에 들고 사용해주세요.");
                return true;
            }

            try {
                if (db.itemkeyExists(category, key)) {
                    player.sendMessage("§c이미 존재하는 키입니다: " + key);
                    return true;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            db.addItem(category, key, item);
            player.sendMessage("§a아이템 등록 완료: " + key);
            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§6[최상급] 곡괭이"));

            meta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            meta.addEnchant(Enchantment.FORTUNE, 3, true);
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addEnchant(Enchantment.MENDING, 1, true);

            item.setItemMeta(meta);
            player.getInventory().addItem(item);
            player.sendMessage("§a테스트 아이템 지급 완료");
            return true;
        }

        player.sendMessage("§c잘못된 명령어입니다.");
        return true;
    }
}