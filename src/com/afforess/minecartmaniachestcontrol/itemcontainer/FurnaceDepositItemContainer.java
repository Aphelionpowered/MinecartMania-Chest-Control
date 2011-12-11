package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class FurnaceDepositItemContainer extends GenericItemContainer implements
        ItemContainer {
    private MinecartManiaFurnace furnace;
    private static final int SLOT = 2;
    
    public FurnaceDepositItemContainer(MinecartManiaFurnace furnace,
            String line, CompassDirection direction) {
        super(line, direction);
        this.furnace = furnace;
    }
    
    public void doCollection(MinecartManiaInventory deposit) {
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    //does not match the item already in the slot, continue
                    if (furnace.getItem(SLOT) == null || !matcher.match(furnace.getItem(SLOT))) {
                        continue;
                    }
                    int toRemove = furnace.getItem(SLOT).getAmount();
                    if (matcher.getAmount() < toRemove) {
                        toRemove = matcher.getAmount();
                    }
                    ItemStack transfer = furnace.getItem(SLOT).clone();
                    if (transfer != null) {
                        transfer.setAmount(toRemove);
                        if (furnace.canRemoveItem(transfer.getTypeId(), toRemove, transfer.getDurability())) {
                            if (deposit.canAddItem(transfer)) {
                                if (deposit.addItem(transfer)) {
                                    furnace.setItem(SLOT, null);
                                } else {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
}
