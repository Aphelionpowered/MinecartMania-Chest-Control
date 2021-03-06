/**
 * 
 */
package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

/**
 * @author Rob
 * 
 */
public class BrewingStandTopContainer extends GenericItemContainer implements ItemContainer {
    
    private static final int SLOT = 3;
    private final MinecartManiaBrewingStand stand;
    
    public BrewingStandTopContainer(final MinecartManiaBrewingStand mmbs, String catalyst, final CompassDirection direction) {
        super(catalyst, direction);
        stand = mmbs;
        if (catalyst.toLowerCase().contains("top")) {
            final String[] split = catalyst.split(":");
            catalyst = "";
            for (final String s : split) {
                if (!s.toLowerCase().contains("top")) {
                    catalyst += s + ":";
                }
            }
        }
        line = catalyst;
    }
    
    public void doCollection(final MinecartManiaInventory withdraw) {
        for (final CompassDirection direction : directions) {
            final ItemMatcher[] list = getMatchers(direction);
            for (final ItemMatcher matcher : list) {
                if (matcher != null) {
                    for (int i = 0; i < withdraw.size(); i++) {
                        if (withdraw.getItem(i) != null) {
                            // Try to match up what we need with what we have.
                            if (!matcher.match(withdraw.getItem(i))) {
                                continue;
                            }
                            // Figure out exactly what we matched.
                            final ItemStack item = withdraw.getItem(i).clone();
                            if (item.getAmount() == -1) {
                                item.setAmount(64);
                            }
                            
                            final int available = withdraw.amount(item.getTypeId(), item.getDurability());
                            final int requested = matcher.getAmount(available);
                            
                            // Determine how much we need to fill the requirements of the system.
                            int toAdd = Math.min(requested, available);
                            item.setAmount(toAdd);
                            
                            // If the stand has stuff in the catalyst slot already...
                            if (stand.getItem(SLOT) != null) {
                                
                                // Figure out what it is...
                                final ItemStack catalyst = stand.getItem(SLOT);
                                
                                // If it's what we want to put in there anyway, adjust our transaction amount accordingly
                                if ((catalyst.getTypeId() == item.getTypeId()) && (catalyst.getDurability() == item.getDurability())) {
                                    toAdd = Math.min(0, Math.min(64 - catalyst.getAmount(), toAdd));
                                    item.setAmount(catalyst.getAmount() + toAdd);
                                } else {
                                    // Otherwise, get rid of it.
                                    if (stand.canRemoveItem(catalyst.getTypeId(), catalyst.getAmount(), catalyst.getDurability())) {
                                        if (withdraw.canAddItem(catalyst)) {
                                            stand.removeItem(catalyst.getTypeId(), catalyst.getAmount(), catalyst.getDurability());
                                            stand.setItem(SLOT, null);
                                        }
                                    }
                                    
                                }
                            }
                            if (withdraw.contains(item.getTypeId()) && withdraw.canRemoveItem(item.getTypeId(), toAdd, item.getDurability())) {
                                if (stand.canAddItem(item)) {
                                    withdraw.removeItem(item.getTypeId(), toAdd, item.getDurability());
                                    stand.setItem(SLOT, item);
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
