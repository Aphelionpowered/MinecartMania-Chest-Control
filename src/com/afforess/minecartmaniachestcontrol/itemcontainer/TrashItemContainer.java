package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class TrashItemContainer extends GenericItemContainer implements
        ItemContainer {
    
    public TrashItemContainer(String line, CompassDirection direction) {
        super(line, direction);
    }
    
    public void doCollection(MinecartManiaInventory other) {
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    for (int i = 0; i < other.size(); i++) {
                        ItemStack itemStack = other.getItem(i);
                        if (itemStack == null)
                            continue;
                        int amount = matcher.getAmount(other.amount(itemStack.getTypeId(), itemStack.getDurability()));
                        
                        itemStack.setAmount(amount);
                        while (matcher.match(itemStack) && amount > 0) {
                            int toAdd = (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
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
