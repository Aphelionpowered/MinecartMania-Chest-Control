package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class BrewingStandBottomContainer extends GenericItemContainer implements ItemContainer {
    
    private final MinecartManiaBrewingStand brewingStand;
    
    public BrewingStandBottomContainer(final MinecartManiaBrewingStand bs, String line, final CompassDirection direction) {
        super(line, direction);
        if (line.toLowerCase().contains("bottom")) {
            final String[] split = line.split(":");
            line = "";
            for (final String s : split) {
                if (!s.toLowerCase().contains("bottom")) {
                    line += s + ":";
                }
            }
        }
        this.line = line;
        brewingStand = bs;
    }
    
    public void doCollection(final MinecartManiaInventory withdraw) {
        final ItemStack[] cartContents = withdraw.getContents();
        final ItemStack[] standContents = brewingStand.getContents();
        for (final CompassDirection direction : directions) {
            for (final ItemStack item : cartContents) {
                if (item != null) {
                    for (final ItemMatcher matcher : getMatchers(direction)) {
                        if (matcher == null) {
                            continue;
                        }
                        if (matcher.match(item)) {
                            for (int i = 0; i < 3; i++) {
                                final ItemStack slotContents = brewingStand.getItem(i);
                                
                                // Ensure the slot is clear. (No stacking allowed)
                                if (slotContents != null) {
                                    continue; // Skip it.
                                }
                                
                                // Try to remove the item from the cart.
                                if (!withdraw.removeItem(item.getTypeId(), 1, item.getDurability())) {
                                    //Failed, restore backup of inventory
                                    withdraw.setContents(cartContents);
                                    brewingStand.setContents(standContents);
                                    return;
                                }
                                
                                // Awesome, add it to the stand.
                                brewingStand.setItem(i, new ItemStack(item.getTypeId(), 1, item.getDurability()));
                                
                            }
                        }
                    }
                }
            }
        }
    }
}
