package io.github.pyke.gui;

import io.github.pyke.ItemTable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ItemTableItemsUI extends PaginatedUI {
    private final String categoryName;
    private final List<Pair<String, ItemStack>> items = new ArrayList<>();

    public ItemTableItemsUI(ItemTable plugin, String categoryName) {
        super(plugin);
        this.categoryName = categoryName;
        loadItems();
    }

    private void loadItems() {
        items.clear();
        try (Connection conn = plugin.getMySQLManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT item_key, item_data FROM items WHERE category_name = ?")) {

            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String key = rs.getString("item_key");
                byte[] data = rs.getBytes("item_data");
                ItemStack item = ItemStack.deserializeBytes(data);

                ItemMeta meta = item.getItemMeta();
                List<Component> lore = new ArrayList<>();

                if (meta.hasLore()) {
                    for (Component line : Objects.requireNonNull(meta.lore())) {
                        String plain = PlainTextComponentSerializer.plainText().serialize(line);
                        if (!plain.contains("휠클릭") && !plain.contains("쉬프트+우클릭")) {
                            lore.add(line);
                        }
                    }
                }

                lore.add(Component.text(""));
                lore.add(Component.text("§a코드: §7" + categoryName + "." + key));
                lore.add(Component.text("§6휠클릭: §f지급"));
                lore.add(Component.text("§c쉬프트+우클릭: §f삭제"));

                meta.lore(lore);
                item.setItemMeta(meta);

                items.add(Pair.of(key, item));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("아이템 목록 불러오기 실패: " + e.getMessage());
        }
    }

    @Override
    protected void openPage(Player player, int page) {
        Inventory inv = createInventory("ItemTable " + categoryName);
        int start = page * pageSize;
        int end = Math.min(start + pageSize, items.size());

        for (int i = start; i < end; i++) {
            inv.setItem(i - start, items.get(i).getRight());
        }

        addNavigationItems(inv, player);
        plugin.getPaginatedListener().register(player, this);
        player.openInventory(inv);
    }

    @Override
    protected boolean hasNextPage() {
        return (page + 1) * pageSize < items.size();
    }

    @Override
    protected void handleItemClick(Player player, ClickType clickType, int slot) {
        int index = page * pageSize + slot;
        if (index >= items.size()) return;

        Pair<String, ItemStack> pair = items.get(index);
        String key = pair.getLeft();
        ItemStack item = pair.getRight().clone();

        if (clickType == ClickType.MIDDLE) {
            ItemStack originItem = plugin.getMySQLManager().loadItem(categoryName, key);
            if (originItem != null) {
                originItem.setAmount(1); // 지급 수량은 1개로 고정
                player.getInventory().addItem(originItem);
                player.sendMessage("§a아이템 지급: " + key);
            } else {
                player.sendMessage("§c아이템을 불러올 수 없습니다: " + key);
            }
        } else if (clickType == ClickType.SHIFT_RIGHT) {
            plugin.getMySQLManager().deleteItem(categoryName, key);
            player.sendMessage("§c아이템 삭제됨: " + key);
            loadItems();
            openPage(player, page);
        }
    }
}