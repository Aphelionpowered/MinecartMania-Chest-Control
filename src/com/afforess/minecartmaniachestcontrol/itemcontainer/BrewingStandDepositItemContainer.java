package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;

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
            AbstractItem[] list = getItemList(direction);
            for (AbstractItem item : list) {
                if (item != null) {
                    for (int i = 0; i < 3; i++) {
                        ItemStack slotContents = brewingStand.getItem(i);
                        
                        // Slot MUST NOT be empty.
                        if (slotContents == null)
                            continue;
                        
                        // Not what we're looking for?
                        if (slotContents.getTypeId() != item.getId() || (item.hasData() && (item.getData() != -1 || slotContents.getDurability() != item.getData()))) {
                            continue; // Skip it
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
