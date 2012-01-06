package com.afforess.minecartmaniachestcontrol.itemcontainer;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaDoubleChest;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

public class ItemDepositContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaInventory inventory;
    
    public ItemDepositContainer(final MinecartManiaInventory inventory, final String line, final CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(final MinecartManiaInventory deposit) {
        String pos = "??? (" + inventory.getClass().getCanonicalName() + ")";
        Location loc = null;
        if (inventory instanceof MinecartManiaChest) {
            loc = ((MinecartManiaChest) inventory).getLocation();
        }
        if (inventory instanceof MinecartManiaDoubleChest) {
            loc = ((MinecartManiaDoubleChest) inventory).getLocation();
        }
        if (loc != null) {
            pos = String.format("%s @ %d,%d,%d", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        for (final CompassDirection direction : directions) {
            HashMap<SpecificMaterial, Integer> debugInfo = new HashMap<SpecificMaterial, Integer>();
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
                        
                        // DEBUGGING
                        SpecificMaterial mat = new SpecificMaterial(itemStack.getTypeId(), itemStack.getDurability());
                        if (!debugInfo.containsKey(mat)) {
                            debugInfo.put(mat, toAdd);
                        } else {
                            debugInfo.put(mat, debugInfo.get(mat) + toAdd);
                        }
                    }
                }
            }
            for (Entry<SpecificMaterial, Integer> entry : debugInfo.entrySet()) {
                MinecartManiaLogger.getInstance().info(String.format("[Deposit Items] Deposited %s %s;%d @ %s", entry.getValue(), Material.getMaterial(entry.getKey().id).name(), entry.getKey().durability, pos));
            }
        }
    }
}
