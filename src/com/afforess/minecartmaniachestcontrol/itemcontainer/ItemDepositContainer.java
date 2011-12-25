package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
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
        Location pos = null;
        if (inventory instanceof MinecartManiaChest) {
            pos = ((MinecartManiaChest) inventory).getLocation();
        }
        for (final CompassDirection direction : directions) {
            for (final ItemMatcher matcher : getMatchers(direction)) {
                if (matcher != null) {
                    int amount = matcher.getAmount(-1);
                    while (inventory.contains(matcher) && (!matcher.amountIsSet() || (amount > 0))) {
                        final ItemStack itemStack = inventory.getItem(inventory.first(matcher));
                        final int toAdd = !matcher.amountIsSet() ? itemStack.getAmount() : (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                        if (!inventory.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                            break; //if we are not allowed to remove the items, give up
                        } else if (!deposit.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()))) {
                            break;
                        }
                        inventory.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                        amount -= toAdd;
                        MinecartManiaLogger.getInstance().info(String.format("[Deposit Items] Deposited %s %s;%d @ %s", toAdd, itemStack.getType().name(), itemStack.getDurability(), pos));
                    }
                }
            }
        }
    }
}
