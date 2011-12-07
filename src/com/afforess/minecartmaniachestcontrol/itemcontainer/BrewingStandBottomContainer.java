package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.ListUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;

public class BrewingStandBottomContainer extends GenericItemContainer implements
        ItemContainer {
    
    private MinecartManiaBrewingStand brewingStand;
    
    public BrewingStandBottomContainer(MinecartManiaBrewingStand bs,
            String line, CompassDirection direction) {
        super(line, direction);
        if (line.toLowerCase().contains("bottom")) {
            String[] split = line.split(":");
            line = "";
            for (String s : split) {
                if (!s.toLowerCase().contains("bottom")) {
                    line += s + ":";
                }
            }
        }
        this.line = line;
        brewingStand = bs;
    }
    
    public void doCollection(MinecartManiaInventory withdraw) {
        @SuppressWarnings("unchecked")
        ArrayList<AbstractItem> rawList = (ArrayList<AbstractItem>) ListUtils.toArrayList(Arrays.asList(getRawItemList()));
        
        for (CompassDirection direction : directions) {
            AbstractItem[] list = getItemList(direction);
            for (AbstractItem item : list) {
                if (item != null && rawList.contains(item)) {
                    int amount = item.getAmount();
                    while (withdraw.contains(item.type()) && (item.isInfinite() || amount > 0)) {
                        ItemStack itemStack = withdraw.getItem(withdraw.first(item.type()));
                        int toAdd = item.isInfinite() ? itemStack.getAmount() : (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                        if (!withdraw.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                            break; //if we are not allowed to remove the items, give up
                        } else if (!brewingStand.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()), 0, 2)) {
                            break;
                        }
                        withdraw.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                        amount -= toAdd;
                    }
                    rawList.remove(item);
                }
            }
        }
    }
    
}
