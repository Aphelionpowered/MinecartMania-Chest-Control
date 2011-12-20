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
        for (final CompassDirection direction : directions) {
            final ItemMatcher[] list = getMatchers(direction);
            for (final ItemMatcher matcher : list) {
                if (matcher != null) {
                    for (int i = 0; i < inventory.size(); i++) {
                        final ItemStack itemStack = inventory.getItem(i);
                        if (itemStack != null) {
                            int amount = matcher.getAmount(inventory.amount(itemStack.getTypeId(), itemStack.getDurability()));
                            itemStack.setAmount(amount);
                            if (matcher.match(itemStack) && (amount > 0)) {
                                final int toAdd = (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
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
}
