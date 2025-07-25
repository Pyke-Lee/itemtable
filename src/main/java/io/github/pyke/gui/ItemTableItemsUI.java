package io.github.pyke.gui;

import io.github.pyke.ItemTable;
import io.github.pyke.util.ItemSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemTableItemsUI implements Listener {
    private final ItemTable plugin;
    private int page = 0;
    private final int ITEMS_PER_PAGE = 45;
    private final List<Pair<String, ItemStack>> items = new ArrayList<>();
    private final String categoryName;
    private Player currentPlayer = null;

    public ItemTableItemsUI(ItemTable plugin, String categoryName) {
        this.plugin = plugin;
        this.categoryName = categoryName;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        page = 0;
        this.currentPlayer = player;
        loadItemsFromDatabase();
        renderPage(player, page);
    }

    private void loadItemsFromDatabase() {
        items.clear();
        Connection connection = plugin.getMySQLManager().getConnection();
        if (null == connection) { return; }

        String sql = "SELECT item_key, item_data FROM items WHERE category_name = ?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String itemKey = rs.getString("item_key");
                String itemDataJson = rs.getString("item_data");

                ItemStack item = ItemSerializer.deserialize(itemDataJson);

                items.add(Pair.of(itemKey, item));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("아이템 목록 로드 중 오류 발생: " + e.getMessage());
        }
    }

    private void renderPage(Player player, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("ItemTable " + categoryName));

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        for(int i = start; i < end; ++i) {
            Pair<String,  ItemStack> pair = items.get(i);
            String itemKey = pair.getLeft();
            ItemStack original = pair.getRight();
            ItemStack item = original.clone();

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                for (Component line : Objects.requireNonNull(meta.lore())) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);
                    if (!plain.contains("휠클릭: 지급") && !plain.contains("쉬프트+우클릭: 삭제")) {
                        lore.add(line);
                    }
                }
            }
            lore.add(Component.text(""));
            lore.add(Component.text("§a코드: §7" + categoryName + "." + itemKey));
            lore.add(Component.text("§6휠클릭: §f지급"));
            lore.add(Component.text("§c쉬프트+우클릭: §f삭제"));

            meta.lore(lore);
            item.setItemMeta(meta);
            inventory.setItem(i - start, item);
        }

        ItemStack filler = createPane(Material.WHITE_STAINED_GLASS_PANE, "");
        for(int i = 0; i < 7; ++i) {
            inventory.setItem(46 + i, filler);
        }

        boolean hasPrevious = page > 0;
        inventory.setItem(45, createPane(hasPrevious ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§f이전 페이지"));

        boolean hasNext = (page + 1) * ITEMS_PER_PAGE < items.size();
        inventory.setItem(53, createPane(hasNext ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§f다음 페이지"));

        player.openInventory(inventory);
    }

    private ItemStack createPane(Material color, String name) {
        ItemStack item = new ItemStack(color);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        if (!title.equals("ItemTable " + categoryName)) { return; }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) { return; }
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < Math.min(45, items.size())) {
            ItemStack clickedItem = event.getCurrentItem();
            if (null == clickedItem || clickedItem.getType() == Material.AIR) return;

            if (event.getClick() == ClickType.MIDDLE) {
                Pair<String, ItemStack> pair = items.get(slot);
                String itemKey = pair.getLeft();
                ItemStack itemToGive = pair.getRight().clone();
                itemToGive.setAmount(1);

                player.getInventory().addItem(itemToGive);
                player.sendMessage("§a아이템 지급: " + itemKey);
                return;
            }
        }

        if (slot == 45 && page > 0) {
            page--;
            renderPage(player, page);
        } else if (slot == 53 && (page + 1) * ITEMS_PER_PAGE < items.size()) {
            page++;
            renderPage(player, page);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) { return; }
        if (!currentPlayer.equals(player)) { return; }

        if (event.getView().title().toString().equals("ItemTable " + categoryName)) {
            new ItemTableCategoriesUI(plugin).open(player);
        }
    }
}
