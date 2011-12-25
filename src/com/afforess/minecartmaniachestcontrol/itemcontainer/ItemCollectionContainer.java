package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class ItemCollectionContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaInventory inventory;
    
    public ItemCollectionContainer(final MinecartManiaInventory inventory, final String line, final CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(final MinecartManiaInventory withdraw) {
        MinecartManiaLogger.getInstance().debug("Processing Collection Sign. Text: " + this.line);
        Player owner = null;
        String temp = null;
        Location pos = null;
        if (inventory instanceof MinecartManiaChest) {
            temp = ((MinecartManiaChest) inventory).getOwner();
            pos = ((MinecartManiaChest) inventory).getLocation();
        }
        if (temp != null) {
            owner = Bukkit.getServer().getPlayer(temp);
        }
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    int amount = matcher.getAmount(-1);
                    while (withdraw.contains(matcher) && (!matcher.amountIsSet() || amount > 0)) {
                        ItemStack itemStack = withdraw.getItem(withdraw.first(matcher));
                        int toAdd = !matcher.amountIsSet() ? itemStack.getAmount() : (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                        if (!withdraw.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                            break; //if we are not allowed to remove the items, give up
                        } else if (!inventory.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()), owner)) {
                            break;
                        }
                        withdraw.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                        amount -= toAdd;
                        MinecartManiaLogger.getInstance().info(String.format("[Collect Items] Collected %s %s;%d @ %s", toAdd, itemStack.getType().name(), itemStack.getDurability(), pos.toString()));
                    }
                }
            }
        }
    }
}
