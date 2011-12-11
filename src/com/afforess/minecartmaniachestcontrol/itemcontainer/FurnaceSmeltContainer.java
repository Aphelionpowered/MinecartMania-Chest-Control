package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class FurnaceSmeltContainer extends GenericItemContainer implements
        ItemContainer {
    MinecartManiaFurnace furnace;
    private static final int SLOT = 0;
    
    public FurnaceSmeltContainer(MinecartManiaFurnace furnace, String smelt,
            CompassDirection direction) {
        super(smelt, direction);
        this.furnace = furnace;
        if (smelt.toLowerCase().contains("smelt")) {
            String[] split = smelt.split(":");
            smelt = "";
            for (String s : split) {
                if (!s.toLowerCase().contains("smelt")) {
                    smelt += s + ":";
                }
            }
        }
        this.line = smelt;
    }
    
    public void doCollection(MinecartManiaInventory withdraw) {
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    //does not match the item already in the slot, continue
                    if (furnace.getItem(SLOT) != null && !matcher.match(furnace.getItem(SLOT))) {
                        continue;
                    }
                    ItemStack transfer = furnace.getItem(SLOT).clone();
                    if (transfer != null) {
                        int toAdd = Math.min(matcher.getAmount(), withdraw.amount(transfer.getTypeId(), transfer.getDurability()));
                        transfer.setAmount(toAdd);
                        if (furnace.getItem(SLOT) != null) {
                            toAdd = Math.min(64 - furnace.getItem(SLOT).getAmount(), toAdd);
                            transfer.setAmount(furnace.getItem(SLOT).getAmount() + toAdd);
                        }
                        if (withdraw.contains(transfer.getTypeId()) && withdraw.canRemoveItem(transfer.getTypeId(), toAdd, transfer.getDurability())) {
                            if (furnace.canAddItem(transfer)) {
                                withdraw.removeItem(transfer.getTypeId(), toAdd, transfer.getDurability());
                                furnace.setItem(SLOT, transfer);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
