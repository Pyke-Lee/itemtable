package io.github.pyke.listener;

import io.github.pyke.ItemTable;
import io.github.pyke.gui.PaginatedUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PaginatedListener implements Listener {
    private final Map<UUID, PaginatedUI> openUIs = new HashMap<>();

    public void register(Player player, PaginatedUI ui) {
        openUIs.put(player.getUniqueId(), ui);
    }

    public void unregister(Player player) {
        openUIs.remove(player.getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        PaginatedUI ui = openUIs.get(player.getUniqueId());
        if (ui == null) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(event.getView().getTopInventory())) return;

        event.setCancelled(true);
        ui.handleClick(player, event.getClick(), event.getSlot());
    }

    public void clearAll() {
        openUIs.clear();
    }
}
