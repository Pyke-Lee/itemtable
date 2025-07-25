package io.github.pyke.gui;

import io.github.pyke.ItemTable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ItemTableCategoriesUI extends PaginatedUI {
    private final List<Map.Entry<String, ItemStack>> categories = new ArrayList<>();

    public ItemTableCategoriesUI(ItemTable plugin) {
        super(plugin);
        loadCategories();
    }

    private void loadCategories() {
        categories.clear();
        try (Connection conn = plugin.getMySQLManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT name, icon_data FROM item_categories");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                byte[] data = rs.getBytes("icon_data");
                ItemStack icon = ItemStack.deserializeBytes(data);

                ItemMeta meta = icon.getItemMeta();
                meta.displayName(Component.text(name));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(""));
                lore.add(Component.text("§6휠클릭: §f이동"));
                lore.add(Component.text("§c쉬프트+우클릭: §f삭제"));
                meta.lore(lore);
                icon.setItemMeta(meta);

                categories.add(new AbstractMap.SimpleEntry<>(name, icon));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("카테고리 불러오기 실패: " + e.getMessage());
        }
    }

    @Override
    protected void openPage(Player player, int page) {
        Inventory inv = createInventory("ItemTable Categories");
        int start = page * pageSize;
        int end = Math.min(start + pageSize, categories.size());

        for (int i = start; i < end; i++) {
            inv.setItem(i - start, categories.get(i).getValue());
        }

        addNavigationItems(inv, player);
        plugin.getPaginatedListener().register(player, this);
        player.openInventory(inv);
    }

    @Override
    protected boolean hasNextPage() {
        return (page + 1) * pageSize < categories.size();
    }

    @Override
    protected void handleItemClick(Player player, ClickType clickType, int slot) {
        int index = page * pageSize + slot;
        if (index >= categories.size()) return;

        String category = categories.get(index).getKey();

        if (clickType == ClickType.MIDDLE) {
            ItemTableItemsUI itemUI = new ItemTableItemsUI(plugin, category);
            itemUI.open(player);
        } else if (clickType == ClickType.SHIFT_RIGHT) {
            plugin.getMySQLManager().deleteCategory(category);
            player.sendMessage("§c카테고리 '" + category + "' 삭제됨");
            loadCategories();
            openPage(player, page);
        }
    }
}