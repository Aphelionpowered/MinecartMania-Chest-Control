package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class ItemCollectionContainer extends GenericItemContainer implements
        ItemContainer {
    private MinecartManiaInventory inventory;
    
    public ItemCollectionContainer(MinecartManiaInventory inventory,
            String line, CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(MinecartManiaInventory withdraw) {
        Player owner = null;
        String temp = null;
        if (inventory instanceof MinecartManiaChest) {
            temp = ((MinecartManiaChest) inventory).getOwner();
        }
        if (temp != null) {
            owner = Bukkit.getServer().getPlayer(temp);
        }
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    for (int i = 0; i < withdraw.size(); i++) {
                        ItemStack itemStack = withdraw.getItem(i);
                        if (itemStack == null)
                            continue;
                        int amount = matcher.getAmount(withdraw.amount(itemStack.getTypeId(), itemStack.getDurability()));
                        
                        if (matcher.match(itemStack)) {
                            int toAdd = itemStack.getAmount() > amount ? amount : itemStack.getAmount();
                            itemStack.setAmount(toAdd);
                            if (!withdraw.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                                break; //if we are not allowed to remove the items, give up
                            } else if (!inventory.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()), owner)) {
                                break;
                            }
                            withdraw.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                            amount -= toAdd;
                        }
                    }
                }
            }
        }
    }
}
