package com.songoda.ultimatekits.conversion;

import com.songoda.ultimatekits.UltimateKits;
import com.songoda.ultimatekits.conversion.hooks.CMIHook;
import com.songoda.ultimatekits.conversion.hooks.DefaultHook;
import com.songoda.ultimatekits.conversion.hooks.EssentialsHook;
import com.songoda.ultimatekits.conversion.hooks.UltimateCoreHook;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;

public class Convert {

    public static void runKitConversions() {
        if (!UltimateKits.getInstance().getKitConfig().contains("Kits")) {
            if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
                try {
                    Class.forName("com.earth2me.essentials.metrics.MetricsListener");
                } catch (ClassNotFoundException ex) {
                    convertKits(new EssentialsHook());
                }
            }
            if (Bukkit.getPluginManager().isPluginEnabled("UltimateCore")) {
                convertKits(new UltimateCoreHook());
            }
            if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
                convertKits(new CMIHook());
            }
        }
        if (!isInJsonFormat()) {
            convertKits(new DefaultHook());
        }
    }

    private static void convertKits(Hook hook) {
        Set<String> kits = hook.getKits();
        for (String kit : kits) {
            List<String> serializedItems = new ArrayList<>();
            for (ItemStack item : hook.getItems(kit)) {
                serializedItems.add(UltimateKits.getInstance().getItemSerializer().serializeItemStackToJson(item));
            }
            UltimateKits.getInstance().getKitConfig().set("Kits." + kit + ".items", serializedItems);
            UltimateKits.getInstance().getKitConfig().set("Kits." + kit + ".delay", hook.getDelay(kit));
            UltimateKits.getInstance().getKitConfig().set("Kits." + kit + ".price", 0D);
        }
        UltimateKits.getInstance().getKitConfig().save();
    }

    private static boolean isInJsonFormat() {
        for (String kit : UltimateKits.getInstance().getKitConfig().getConfigurationSection("Kits").getKeys(false)) {
            if (UltimateKits.getInstance().getKitConfig().contains("Kits." + kit + ".items")) {
                List<String> itemList = UltimateKits.getInstance().getKitConfig().getStringList("Kits." + kit + ".items");
                if (itemList.size() > 0) {
                    if (itemList.get(0).startsWith("{")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
