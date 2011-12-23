package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public class ItemCollectionContainer extends GenericItemContainer implements ItemContainer {
    private final MinecartManiaInventory inventory;
    
    public ItemCollectionContainer(final MinecartManiaInventory inventory, final String line, final CompassDirection direction) {
        super(line, direction);
        this.inventory = inventory;
    }
    
    public void doCollection(final MinecartManiaInventory withdraw) {
        final ItemStack[] cartContents = withdraw.getContents().clone();
        final ItemStack[] chestContents = inventory.getContents().clone();
        for (final CompassDirection direction : directions) {
            for (final ItemMatcher matcher : getMatchers(direction)) {
                if (matcher == null) {
                    continue;
                }
                ItemStack item = null;
                // Find the first matching item.
                for (final ItemStack mySlot : withdraw.getContents().clone()) {
                    if (matcher.match(mySlot)) {
                        item = mySlot;
                        break;
                    }
                }
                // Nothing matches
                if (item == null) {
                    continue;
                }
                
                // Get the maximum stack size (or just 64 if we've disabled that)
                final int maxamount = MinecartManiaWorld.getMaxStackSize(item);
                
                // Get the amount we want to add to the slot
                final int amountRequested = Math.min(withdraw.amount(matcher), matcher.getAmount(Integer.MAX_VALUE));
                
                // Get the amount of available slots
                int emptySlots = 0;
                
                // How much room is available in terms of incomplete stacks (<64)
                int slack = 0;
                
                for (final ItemStack slot : inventory.getContents()) {
                    // Non-empty slot
                    if ((slot != null) && (slot.getType() != Material.AIR)) {
                        // Ensure we have the same ID and durability and enchantments
                        if ((slot.getTypeId() == item.getTypeId()) && (slot.getDurability() == item.getDurability())) {
                            //if the slot amount is negative, skip it.
                            if (slot.getAmount() < 0) {
                                continue;
                            }
                            
                            // Figure out how much we have to add to complete the stack.
                            slack += Math.min(maxamount, Math.max(0, maxamount - slot.getAmount()));
                        } else {
                            // Skip it, not the same.
                            continue;
                        }
                    } else {
                        // Empty slot, count it.
                        emptySlots++;
                    }
                }
                // And finally, add up the number of empty slots (times stack size) and how much slack we have.
                // If larger than the amount requested (or the stuff available in the cart), then use the requested number.
                final int amount = Math.min(amountRequested, (emptySlots * maxamount) + slack);
                String amountDebug = String.format("amount = (%d * %d) + %d", emptySlots, maxamount, slack);
                amountDebug += String.format("\nRequested: %d", amountRequested);
                
                String error = "";
                // Try to remove the items from the chest.
                if (withdraw.removeItem(item.getTypeId(), amount, item.getDurability())) {
                    // Awesome, add it to the cart.
                    if (inventory.addItem(new ItemStack(item.getTypeId(), amount, item.getDurability()))) {
                        MinecartManiaLogger.getInstance().info(String.format("[Collect Items]  Collected %s;%d@%d", item.getTypeId(), item.getDurability(), amount));
                        continue;
                    } else {
                        error = "Failed to add to chest";
                    }
                } else {
                    error = "Failed to remove from cart: " + withdraw.getFailureReason();
                }
                error += "\n" + amountDebug;
                MinecartManiaLogger.getInstance().info(String.format("[Collect Items]  FAILED to collect %s;%d@%d: %s", item.getTypeId(), item.getDurability(), amount, error));
                //Failed, restore backup of inventory
                withdraw.setContents(cartContents);
                inventory.setContents(chestContents);
                return;
            }
        }
    }
}
