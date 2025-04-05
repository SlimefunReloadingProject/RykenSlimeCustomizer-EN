package org.lins.mmmjjkx.rykenslimefuncustomizer.objects.customs.item;

import java.util.List;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class RSCItemStack {
    private final ItemStack item;

    public RSCItemStack(ItemStack item, String name, List<String> lore) {
        item.editMeta(meta -> {
            if (name != null && !name.isBlank()) {
                meta.setDisplayName(name);
            }

            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }
        });

        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }
}
