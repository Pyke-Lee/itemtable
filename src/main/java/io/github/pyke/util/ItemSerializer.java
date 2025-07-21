package io.github.pyke.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemSerializer {
    private static final Gson gson = new GsonBuilder().create();
    private static Logger logger;

    public static void setLogger(Logger pluginsLogger) {
        logger = pluginsLogger;
    }

    public static String serialize(ItemStack item) {
        return gson.toJson(item.serialize());
    }

    @SuppressWarnings("unchecked")
    public static ItemStack deserialize(String json) {
        try {
            Map<String, Object> map = gson.fromJson(json, Map.class);
            return ItemStack.deserialize(map);
        } catch (Exception e) {
            if (logger != null) {
                logger.log(Level.SEVERE, "ItemStack 역직렬화 중 오류 발생", e);
            } else {
                e.printStackTrace();
            }
            return null;
        }
    }
}
