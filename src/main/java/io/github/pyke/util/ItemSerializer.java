package io.github.pyke.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;
//import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemSerializer {
    private static final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new OptionalTypeAdapterFactory()).create();

//    private static final Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
//    private static Logger logger;

    public static void setLogger(Logger pluginsLogger) {
//        logger = pluginsLogger;
    }

    public static String serialize(ItemStack item) {
        Map<String, Object> serialized = item.serialize();
        return gson.toJson(serialized);
    }

    public static ItemStack deserialize(String json) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> map = gson.fromJson(json, type);
        ItemStack item = ItemStack.deserialize(map);

        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            System.out.println("복원된 아이템 인첸트: " + item.getItemMeta().getEnchants());
        } else {
            System.out.println("인첸트가 없습니다!");
        }

        return item;
    }
}
