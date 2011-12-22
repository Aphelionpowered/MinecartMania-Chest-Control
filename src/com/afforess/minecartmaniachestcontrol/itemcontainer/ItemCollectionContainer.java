package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class ItemCollectionContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaInventory inventory;
    
    public ItemCollectionContainer(final MinecartManiaInventory inventory, final String line, final CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(final MinecartManiaInventory withdraw) {
        final ItemStack[] cartContents = withdraw.getContents();
        final ItemStack[] standContents = inventory.getContents();
        for (final CompassDirection direction : directions) {
            for (final ItemStack item : cartContents) {
                if (item != null) {
                    for (final ItemMatcher matcher : getMatchers(direction)) {
                        if (matcher == null) {
                            continue;
                        }
                        if (matcher.match(item)) {
                            for (int i = 0; i < standContents.length; i++) {
                                final ItemStack slotContents = inventory.getItem(i);
                                
                                // Ensure the slot is clear. (No stacking allowed)
                                if (slotContents != null) {
                                    continue; // Skip it.
                                }
                                
                                // Try to remove the item from the cart.
                                if (!withdraw.removeItem(item.getTypeId(), 1, item.getDurability())) {
                                    //Failed, restore backup of inventory
                                    withdraw.setContents(cartContents);
                                    inventory.setContents(standContents);
                                    return;
                                }
                                
                                // Awesome, add it to the stand.
                                inventory.setItem(i, new ItemStack(item.getTypeId(), 1, item.getDurability()));
                                
                            }
                        }
                    }
                }
            }
        }
    }
}
