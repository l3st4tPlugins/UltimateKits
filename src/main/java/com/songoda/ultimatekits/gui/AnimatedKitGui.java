package com.songoda.ultimatekits.gui;

import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.compatibility.CompatibleSound;
import com.songoda.core.gui.Gui;
import com.songoda.core.gui.GuiUtils;
import com.songoda.ultimatekits.UltimateKits;
import com.songoda.ultimatekits.kit.Kit;
import com.songoda.ultimatekits.kit.KitItem;
import com.songoda.ultimatekits.settings.Settings;
import com.songoda.ultimatekits.utils.ArmorType;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AnimatedKitGui extends Gui {

    static final Random rand = new Random();

    private final UltimateKits plugin;
    private final Player player;
    private final ItemStack give;
    private final ArrayDeque<KitItem> items = new ArrayDeque();
    private boolean finish = false;
    private boolean done = false;
    private int tick = 0, updateTick = 0;
    private int ticksPerUpdate = 3;
    private final int updatesPerSlow = 6;
    private final int ticksPerUpdateSlow = 10;
    private int task;

    public AnimatedKitGui(UltimateKits plugin, Player player, Kit kit, ItemStack give) {
        this.plugin = plugin;
        this.player = player;
        this.give = give;
        setRows(3);
        setAllowClose(false);
        setTitle(kit.getShowableName());
        setDefaultItem(GuiUtils.getBorderItem(CompatibleMaterial.GRAY_STAINED_GLASS_PANE));

        // ideally, we'd populate the items in such a way that the end item isn't far from the center when the animation is complete
        // would be something to do if people have large kit loot tables.
        List<KitItem> kitItems = kit.getContents();
        if(kitItems.isEmpty()) {
            throw new RuntimeException("Cannot give an empty kit!");
        }
        Collections.shuffle(kitItems);
        this.items.addAll(kitItems);
        while (this.items.size() < 10) {
            items.addAll(kitItems);
        }
        
        setItem(4, GuiUtils.getBorderItem(CompatibleMaterial.TRIPWIRE_HOOK));
        setItem(22, GuiUtils.getBorderItem(CompatibleMaterial.TRIPWIRE_HOOK));
        tick();
        setOnOpen(event -> {
            task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> tick(), 1L, 1L);
        });
    }

    void tick() {
        if (++tick < ticksPerUpdate) {
            return;
        }
        tick = 0;
        if (++updateTick >= updatesPerSlow) {
            updateTick = 0;
            if (++ticksPerUpdate >= ticksPerUpdateSlow) {
                finish = true;
            }
        }
        // now update the display
        // rainbow disco!
        for (int col = 0; col < 9; ++col) {
            if(col == 4) continue;
            setItem(0, col, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneColor(rand.nextInt(16))));
            setItem(2, col, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneColor(rand.nextInt(16))));
        }

        // item slider
        if (!done) {
            CompatibleSound.UI_BUTTON_CLICK.play(player, 5F, 5F);
            items.addFirst(items.getLast());
            items.removeLast();
            Iterator<KitItem> itemIter = items.iterator();
                for (int i = 9; i < 18; i++) {
                setItem(0, i, itemIter.next().getItem());
            }
        }

        // should we try to wrap it up?
        if (finish) {
            ItemStack item = getItem(13);
            if(item == null) {
                done = true; // idk.
            } else if (item.isSimilar(give)) {
                if (!done) {
                    done = true;
                    if (!Settings.AUTO_EQUIP_ARMOR_ROULETTE.getBoolean() || !ArmorType.equip(player, give)) {
                        Map<Integer, ItemStack> overfilled = player.getInventory().addItem(give);
                        for (ItemStack item2 : overfilled.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), item2);
                        }
                    }

                    CompatibleSound.ENTITY_PLAYER_LEVELUP.play(player, 10f, 10f);
                    plugin.getLocale().getMessage("event.create.won")
                            .processPlaceholder("item", WordUtils.capitalize(give.getType().name().toLowerCase().replace("_", " ")))
                            .sendPrefixedMessage(player);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::finish, 50);
                    setAllowClose(true);
                }

            }
        }

    }
    private void finish() {
        Bukkit.getScheduler().cancelTask(task);
        exit();
    }
}