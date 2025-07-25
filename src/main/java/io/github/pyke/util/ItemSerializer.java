package io.github.pyke.util;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemSerializer {
    public static String serialize(ItemStack item) {
        return Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack deserialize(String json) {
        byte[] bytes = Base64.getDecoder().decode(json);
        return ItemStack.deserializeBytes(bytes);
    }
}
