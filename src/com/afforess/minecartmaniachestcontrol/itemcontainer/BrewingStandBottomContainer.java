package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

public class BrewingStandBottomContainer extends GenericItemContainer implements
        ItemContainer {
    
    private MinecartManiaBrewingStand brewingStand;
    
    public BrewingStandBottomContainer(MinecartManiaBrewingStand bs,
            String line, CompassDirection direction) {
        super(line, direction);
        if (line.toLowerCase().contains("bottom")) {
            String[] split = line.split(":");
            line = "";
            for (String s : split) {
                if (!s.toLowerCase().contains("bottom")) {
                    line += s + ":";
                }
            }
        }
        this.line = line;
        brewingStand = bs;
    }
    
    public void doCollection(MinecartManiaInventory withdraw) {
        ItemStack[] cartContents = withdraw.getContents();
        ItemStack[] standContents = brewingStand.getContents();
        for (CompassDirection direction : directions) {
            for (ItemStack item : cartContents) {
                if (item != null) {
                    for (ItemMatcher matcher : this.getMatchers(direction)) {
                        if(matcher==null) continue;
                        if (matcher.match(item)) {
                            for (int i = 0; i < 3; i++) {
                                ItemStack slotContents = brewingStand.getItem(i);
                                
                                // Ensure the slot is clear. (No stacking allowed)
                                if (slotContents != null) {
                                    continue; // Skip it.
                                }
                                
                                // Try to remove the item from the cart.
                                if (!withdraw.removeItem(item.getTypeId(), 1, (short) item.getDurability())) {
                                    //Failed, restore backup of inventory
                                    withdraw.setContents(cartContents);
                                    brewingStand.setContents(standContents);
                                    return;
                                }
                                
                                // Awesome, add it to the stand.
                                brewingStand.setItem(i, new ItemStack(item.getTypeId(), 1, (short) item.getDurability()));
                                
                            }
                        }
                    }
                }
            }
        }
    }
}
