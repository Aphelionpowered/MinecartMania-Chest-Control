package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class TrashItemContainer extends GenericItemContainer implements ItemContainer {
    
    public TrashItemContainer(final String line, final CompassDirection direction) {
        super(line, direction);
    }
    
    public void doCollection(final MinecartManiaInventory other) {
        for (final CompassDirection direction : directions) {
            final ItemMatcher[] list = getMatchers(direction);
            for (final ItemMatcher matcher : list) {
                if (matcher != null) {
                    for (int i = 0; i < other.size(); i++) {
                        final ItemStack itemStack = other.getItem(i);
                        if (itemStack == null) {
                            continue;
                        }
                        int amount = matcher.getAmount(other.amount(matcher));
                        
                        itemStack.setAmount(amount);
                        while (matcher.match(itemStack) && (amount > 0)) {
                            final int toAdd = (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                            if (!other.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                                break; //if we are not allowed to remove the items, give up
                            }
                            other.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                            amount -= toAdd;
                        }
                    }
                }
            }
        }
    }
    
}
