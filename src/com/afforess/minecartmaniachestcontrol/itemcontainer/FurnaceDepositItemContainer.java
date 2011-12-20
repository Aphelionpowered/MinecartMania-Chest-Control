package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class FurnaceDepositItemContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaFurnace furnace;
    private static final int SLOT = 2;
    
    public FurnaceDepositItemContainer(final MinecartManiaFurnace furnace, final String line, final CompassDirection direction) {
        super(line, direction);
        this.furnace = furnace;
    }
    
    public void doCollection(final MinecartManiaInventory deposit) {
        final ItemStack[] cartContents = deposit.getContents();
        final ItemStack[] standContents = furnace.getContents();
        for (final CompassDirection direction : directions) {
            final ItemMatcher[] list = getMatchers(direction);
            for (final ItemMatcher item : list) {
                if (item != null) {
                    final ItemStack slotContents = furnace.getItem(SLOT);
                    
                    // Slot MUST NOT be empty.
                    if (slotContents == null) {
                        continue;
                    }
                    
                    //does not match the item already in the slot, or isn't an item we want so, continue
                    if ((furnace.getItem(SLOT) == null) || !item.match(furnace.getItem(SLOT))) {
                        continue;
                    }
                    
                    // See if we can add this crap to the Minecart.
                    if (!deposit.addItem(slotContents)) {
                        //Failed, restore backup of inventory
                        deposit.setContents(cartContents);
                        furnace.setContents(standContents);
                        return;
                    }
                    
                    // Now remove the slot contents.
                    furnace.setItem(SLOT, null);
                }
            }
        }
    }
}
