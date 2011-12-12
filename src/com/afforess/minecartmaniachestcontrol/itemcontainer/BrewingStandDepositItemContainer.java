package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class BrewingStandDepositItemContainer extends GenericItemContainer
        implements ItemContainer {
    
    private MinecartManiaBrewingStand brewingStand;
    
    public BrewingStandDepositItemContainer(
            MinecartManiaBrewingStand brewingStand, String line,
            CompassDirection direction) {
        super(line, direction);
        this.brewingStand = brewingStand;
    }
    
    public void doCollection(MinecartManiaInventory deposit) {
        ItemStack[] cartContents = deposit.getContents();
        ItemStack[] standContents = brewingStand.getContents();
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher item : list) {
                if (item != null) {
                    for (int i = 0; i < 3; i++) {
                        ItemStack slotContents = brewingStand.getItem(i);
                        
                        // Slot MUST NOT be empty.
                        if (slotContents == null)
                            continue;
                        
                        //does not match the item already in the slot, or isn't an item we want so, continue
                        if (brewingStand.getItem(i) == null || !item.match(brewingStand.getItem(i))) {
                            continue;
                        }
                        
                        // See if we can add this crap to the Minecart.
                        if (!deposit.addItem(slotContents)) {
                            //Failed, restore backup of inventory
                            deposit.setContents(cartContents);
                            brewingStand.setContents(standContents);
                            return;
                        }
                        
                        // Now remove the slot contents.
                        brewingStand.setItem(i, null);
                    }
                }
            }
        }
    }
    
}
