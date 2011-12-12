package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.inventory.MinecartManiaFurnace;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

public class FurnaceFuelContainer extends GenericItemContainer implements
        ItemContainer {
    MinecartManiaFurnace furnace;
    private static final int SLOT = 1;
    
    public FurnaceFuelContainer(MinecartManiaFurnace furnace, String fuel,
            CompassDirection direction) {
        super(fuel, direction);
        this.furnace = furnace;
        if (fuel.toLowerCase().contains("fuel")) {
            String[] split = fuel.split(":");
            fuel = "";
            for (String s : split) {
                if (!s.toLowerCase().contains("fuel")) {
                    fuel += s + ":";
                }
            }
        }
        this.line = fuel;
    }
    
    public void doCollection(MinecartManiaInventory withdraw) {
        for (CompassDirection direction : directions) {
            ItemMatcher[] list = getMatchers(direction);
            for (ItemMatcher matcher : list) {
                if (matcher != null) {
                    //does not match the item already in the slot, continue
                    if (furnace.getItem(SLOT) != null && !matcher.match(furnace.getItem(SLOT))) {
                        continue;
                    }
                    if (furnace.getItem(SLOT) != null) {
                        ItemStack transfer = furnace.getItem(SLOT).clone();
                        int toAdd = Math.min(matcher.getAmountForTransfer(), withdraw.amount(transfer.getTypeId(), transfer.getDurability()));
                        transfer.setAmount(toAdd);
                        toAdd = Math.min(64 - furnace.getItem(SLOT).getAmount(), toAdd);
                        transfer.setAmount(furnace.getItem(SLOT).getAmount() + toAdd);
                        if (withdraw.contains(transfer.getTypeId(), transfer.getDurability()) && withdraw.canRemoveItem(transfer.getTypeId(), toAdd, transfer.getDurability())) {
                            if (furnace.canAddItem(transfer)) {
                                withdraw.removeItem(transfer.getTypeId(), toAdd, transfer.getDurability());
                                furnace.setItem(SLOT, transfer);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
