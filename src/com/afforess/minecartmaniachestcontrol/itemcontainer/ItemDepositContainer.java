package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.utils.ListUtils;

public class ItemDepositContainer extends GenericItemContainer implements
        ItemContainer {
    private MinecartManiaInventory inventory;
    
    public ItemDepositContainer(MinecartManiaInventory inventory, String line,
            CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(MinecartManiaInventory deposit) {
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    int amount = matcher.getAmount();
                    for (int i = 0; i < inventory.size(); i++) {
                        ItemStack itemStack = inventory.getItem(i);
                        itemStack.setAmount(amount);
                        if (matcher.match(itemStack) && amount > 0) {
                            int toAdd = (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                            if (!inventory.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                                break; //if we are not allowed to remove the items, give up
                            } else if (!deposit.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()))) {
                                break;
                            }
                            inventory.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                            amount -= toAdd;
                        }
                    }
                }
            }
        }
    }
}
