package io.github.pyke.gui;

import io.github.pyke.ItemTable;
import io.github.pyke.util.ItemSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
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

public class ItemTableCategoriesUI implements Listener {
    private final ItemTable plugin;
    private int iPage = 0;
    private final int ITEMS_PER_PAGE = 45;
    private final List<ItemStack> icons = new ArrayList<>();

    public ItemTableCategoriesUI(ItemTable plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        iPage = 0;
        loadIconsFromDatabase();
        renderPage(player, iPage);
    }

    private void loadIconsFromDatabase() {
        icons.clear();
        Connection connection = plugin.getMySQLManager().getConnection();
        if (connection == null) return;

        try (PreparedStatement stmt = connection.prepareStatement("SELECT name, icon_json FROM item_categories");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                String iconJson = rs.getString("icon_json");

                ItemStack icon = ItemSerializer.deserialize(iconJson);

                var meta = icon.getItemMeta();
                if (meta == null) continue;

                meta.displayName(Component.text(name));
                icon.setItemMeta(meta);

                icons.add(icon);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void renderPage(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text("ItemTable Categories"));

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, icons.size());

        for (int i = start; i < end; i++) {
            ItemStack original = icons.get(i);
            ItemStack item = original.clone();  // 복제본 생성

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            // 로어 새로 생성 (기존 로어 유지하면서 중복 방지)
            List<Component> lore = new ArrayList<>();
            if (meta.hasLore()) {
                for (Component line : Objects.requireNonNull(meta.lore())) {
                    String plain = PlainTextComponentSerializer.plainText().serialize(line);
                    if (!plain.contains("휠클릭: 이동") && !plain.contains("쉬프트+우클릭: 삭제")) {
                        lore.add(line);
                    }
                }
            }
            lore.add(Component.text(""));
            lore.add(Component.text("§6휠클릭: §f이동"));
            lore.add(Component.text("§c쉬프트+우클릭: §f삭제"));

            meta.lore(lore);
            item.setItemMeta(meta);

            gui.setItem(i - start, item);
        }

        // 하단 빈 공간 채우기
        ItemStack filler = createPane(Material.WHITE_STAINED_GLASS_PANE, "");
        for (int i = 45; i <= 52; i++) {
            gui.setItem(i, filler);
        }

        // 이전 페이지 버튼
        boolean hasPrevious = page > 0;
        gui.setItem(45, createPane(hasPrevious ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§f이전 페이지"));

        // 다음 페이지 버튼
        boolean hasNext = (page + 1) * ITEMS_PER_PAGE < icons.size();
        gui.setItem(53, createPane(hasNext ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, "§f다음 페이지"));

        player.openInventory(gui);
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
        if (!title.equals("ItemTable Categories")) { return; }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) { return; }
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < icons.size()) {
            if (event.getClick() == ClickType.SHIFT_RIGHT) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || !clickedItem.hasItemMeta()) return;

                String displayName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(clickedItem.getItemMeta().displayName()));
                if (displayName.isEmpty()) return;

                plugin.getMySQLManager().deleteCategory(displayName);
                player.sendMessage("§c카테고리 '" + displayName + "' 가 삭제되었습니다.");

                loadIconsFromDatabase();   // 삭제 후 아이콘 리스트 갱신
                renderPage(player, iPage);
            }
            else if (event.getClick() == ClickType.MIDDLE) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || !clickedItem.hasItemMeta()) return;

                String displayName = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(clickedItem.getItemMeta().displayName()));
                if (displayName.isEmpty()) return;

                new ItemTableItemsUI(plugin, displayName).open(player);
            }
        }

        if (slot == 45 && iPage > 0) {
            iPage--;
            renderPage(player, iPage);
        } else if (slot == 53 && (iPage + 1) * ITEMS_PER_PAGE < icons.size()) {
            iPage++;
            renderPage(player, iPage);
        }
    }
}