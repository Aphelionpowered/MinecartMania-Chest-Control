package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.ListUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;

public class BrewingStandDepositItemContainer extends GenericItemContainer
        implements ItemContainer {
    
    private MinecartManiaBrewingStand brewingStand;
    
    public BrewingStandDepositItemContainer(
            MinecartManiaBrewingStand brewingStand, String line,
            CompassDirection direction) {
        super(line, direction);
        this.brewingStand = brewingStand;
    }
    
    public void doCollection(MinecartManiaInventory deposit) {
        ArrayList<AbstractItem> rawList = (ArrayList<AbstractItem>) ListUtils.toArrayList(Arrays.asList(getRawItemList()));
        for (CompassDirection direction : directions) {
            AbstractItem[] list = getItemList(direction);
            for (AbstractItem item : list) {
                if (item != null && rawList.contains(item)) {
                    int amount = item.getAmount();
                    while (brewingStand.contains(item.type(),0,2) && (item.isInfinite() || amount > 0) ) {
                        ItemStack itemStack = brewingStand.getItem(brewingStand.first(item.type(),0,2));
                        int toAdd = item.isInfinite() ? itemStack.getAmount() : (itemStack.getAmount() > amount ? amount : itemStack.getAmount());
                        if (!brewingStand.canRemoveItem(itemStack.getTypeId(), toAdd, itemStack.getDurability())) {
                            break; //if we are not allowed to remove the items, give up
                        }
                        else if (!deposit.addItem(new ItemStack(itemStack.getTypeId(), toAdd, itemStack.getDurability()))) {
                            break;
                        }
                        brewingStand.removeItem(itemStack.getTypeId(), toAdd, itemStack.getDurability());
                        amount -= toAdd;
                    }
                    rawList.remove(item);
                }
            }
        }
    }
    
}
