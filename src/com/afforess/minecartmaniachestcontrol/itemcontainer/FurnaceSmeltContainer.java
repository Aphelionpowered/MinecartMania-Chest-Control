package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

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
                    for (int i = 0; i < withdraw.size(); i++) {
                        if (withdraw.getItem(i) != null) {
                            // Try to match up what we need with what we have.
                            if (!matcher.match(withdraw.getItem(i))) {
                                continue;
                            }
                            // Figure out exactly what we matched.
                            ItemStack item = withdraw.getItem(i).clone();
                            if (item.getAmount() == -1) {
                                item.setAmount(64);
                            }
                            
                            int available = withdraw.amount(item.getTypeId(), item.getDurability());
                            int requested = matcher.getAmount(available);
                            
                            // Determine how much we need to fill the requirements of the system.
                            int toAdd = Math.min(requested, available);
                            item.setAmount(toAdd);
                            
                            // If the furnace has stuff in the catalyst slot already...
                            if (furnace.getItem(SLOT) != null) {
                                
                                // Figure out what it is...
                                ItemStack catalyst = furnace.getItem(SLOT);
                                
                                // If it's what we want to put in there anyway, adjust our transaction amount accordingly
                                if (catalyst.getTypeId() == item.getTypeId() && catalyst.getDurability() == item.getDurability()) {
                                    toAdd = Math.min(0,Math.min(64 - catalyst.getAmount(), toAdd));
                                    item.setAmount(catalyst.getAmount() + toAdd);
                                } else {
                                    // Otherwise, get rid of it.
                                    if (furnace.canRemoveItem(catalyst.getTypeId(), catalyst.getAmount(), catalyst.getDurability())) {
                                        if (withdraw.canAddItem(catalyst)) {
                                            furnace.removeItem(catalyst.getTypeId(), catalyst.getAmount(), catalyst.getDurability());
                                            furnace.setItem(SLOT, null);
                                        }
                                    }
                                    
                                }
                            }
                            if (withdraw.contains(item.getTypeId()) && withdraw.canRemoveItem(item.getTypeId(), toAdd, item.getDurability())) {
                                if (furnace.canAddItem(item)) {
                                    withdraw.removeItem(item.getTypeId(), toAdd, item.getDurability());
                                    furnace.setItem(SLOT, item);
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
