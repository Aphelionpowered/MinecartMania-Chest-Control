package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class ItemDepositContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaInventory inventory;
    
    public ItemDepositContainer(final MinecartManiaInventory inventory, final String line, final CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(final MinecartManiaInventory deposit) {
        final ItemStack[] cartContents = deposit.getContents();
        final ItemStack[] standContents = inventory.getContents();
        for (final CompassDirection direction : directions) {
            final ItemMatcher[] list = getMatchers(direction);
            for (int idx = 0; idx < standContents.length; idx++) {
                for (final ItemMatcher item : list) {
                    if (item != null) {
                        final ItemStack slotContents = inventory.getItem(idx);
                        
                        // Slot MUST NOT be empty.
                        if (slotContents == null) {
                            continue;
                        }
                        
                        //does not match the item already in the slot, or isn't an item we want so, continue
                        if ((inventory.getItem(idx) == null) || !item.match(inventory.getItem(idx))) {
                            continue;
                        }
                        
                        // See if we can add this crap to the Minecart.
                        if (!deposit.addItem(slotContents)) {
                            //Failed, restore backup of inventory
                            deposit.setContents(cartContents);
                            inventory.setContents(standContents);
                            return;
                        }
                        
                        // Now remove the slot contents.
                        inventory.setItem(idx, null);
                    }
                }
            }
        }
    }
}
