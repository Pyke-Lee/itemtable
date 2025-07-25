package io.github.pyke.gui;

import io.github.pyke.ItemTable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class PaginatedUI {
    protected final ItemTable plugin;
    protected int page = 0;
    protected final int pageSize = 45;

    public PaginatedUI(ItemTable plugin) {
        this.plugin = plugin;
    }

    protected Inventory createInventory(String title) {
        return Bukkit.createInventory(null, 54, Component.text(title));
    }

    protected void addNavigationItems(Inventory inv, Player player) {
        // 이전 페이지 버튼
        Material prevMaterial = (page > 0) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack prev = new ItemStack(prevMaterial);
        ItemMeta prevMeta = prev.getItemMeta();
        if (prevMeta != null) {
            prevMeta.displayName(Component.text("§f이전 페이지"));
            prev.setItemMeta(prevMeta);
        }
        inv.setItem(45, prev);

        // 다음 페이지 버튼
        Material nextMaterial = (hasNextPage()) ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack next = new ItemStack(nextMaterial);
        ItemMeta nextMeta = next.getItemMeta();
        if (nextMeta != null) {
            nextMeta.displayName(Component.text("§f다음 페이지"));
            next.setItemMeta(nextMeta);
        }
        inv.setItem(53, next);

        // 하단 빈 슬롯 (46~52)
        ItemStack filler = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.displayName(Component.text(" "));
        filler.setItemMeta(fillerMeta);

        for (int i = 46; i <= 52; i++) {
            inv.setItem(i, filler);
        }
    }

    public void open(Player player) {
        openPage(player, page);
    }

    protected abstract void openPage(Player player, int page);

    protected abstract boolean hasNextPage();

    protected abstract void handleItemClick(Player player, ClickType clickType, int slot);

    public void handleClick(Player player, ClickType clickType, int slot) {
        if (slot == 45 && page > 0) {
            page--;
            openPage(player, page);
        }
        else if (slot == 53 && hasNextPage()) {
            page++;
            openPage(player, page);
        }
        else if (slot >= 0 && slot < pageSize) {
            handleItemClick(player, clickType, slot);
        }
    }
}