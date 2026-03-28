package fr.jaeforzs.ris;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemRegistry {

    private static volatile List<Item> ALL_ITEMS = List.of();
    private static final Random RANDOM = new Random();

    public static void initialize() {
        List<Item> localItems = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item != Items.AIR) {
                localItems.add(item);
            }
        }

        
        ALL_ITEMS = List.copyOf(localItems);
        SpeedrunningARandomItem.LOGGER.info("Loaded {} items", ALL_ITEMS.size());
    }

    public static Item getRandomItem() {
        List<Item> items = ALL_ITEMS;
        if (items.isEmpty()) {
            initialize();
            items = ALL_ITEMS;
            if (items.isEmpty()) return Items.DIAMOND;
        }
        return items.get(RANDOM.nextInt(items.size()));
    }

    public static List<Item> getAllItems() {
        if (ALL_ITEMS.isEmpty()) {
            initialize();
        }
        return ALL_ITEMS;
    }

    public static int getItemCount() {
        return getAllItems().size();
    }
}