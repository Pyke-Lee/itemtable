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

import java.util.ArrayList;
import java.util.List;

public class ItemTableCmd implements CommandExecutor {
    private final ItemTable plugin;

    public ItemTableCmd(ItemTable plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) { return false; }

        if (args.length == 0) {
            new ItemTableCategoriesUI(plugin).open(player);
            return true;
        }

        MySQLManager db = plugin.getMySQLManager();

        if (args[0].equalsIgnoreCase("categories") && args.length == 3 && args[1].equalsIgnoreCase("add")) {
            String category = args[2];
            if (db.categoryExists(category)) {
                player.sendMessage("§c중복된 카테고리 '" + category + "'");
                return true;
            }

            ItemStack item = player.getInventory().getItemInMainHand();
            db.addCategory(category, item);
            player.sendMessage("§a카테고리 '" + category + "'추가됨");
            return true;
        }

        if (args[0].equalsIgnoreCase("item") && args.length == 4 && args[1].equalsIgnoreCase("add")) {
            String category = args[2];
            String key = args[3];
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                player.sendMessage("§c들고 있는 아이템이 없습니다.");
                return true;
            }

            db.addItem(category, key, item);
            player.sendMessage("§a아이템 저장 완료: " + key);
            return true;
        }

        if (args[0].equalsIgnoreCase("test") && args.length == 1) {
            ItemStack item = new ItemStack(Material.NETHERITE_PICKAXE);
            ItemMeta meta = item.getItemMeta();

            meta.addEnchant(Enchantment.EFFICIENCY, 5, false);
            meta.addEnchant(Enchantment.FORTUNE, 3, false);
            meta.addEnchant(Enchantment.UNBREAKING, 3, false);
            meta.addEnchant(Enchantment.MENDING, 1, false);

            meta.displayName(Component.text("§6[최상급] 곡괭이"));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("테스트용 아이템입니다."));
            meta.lore(lore);
            item.setItemMeta(meta);

            player.getInventory().addItem(item);
        }

        return false;
    }
}
