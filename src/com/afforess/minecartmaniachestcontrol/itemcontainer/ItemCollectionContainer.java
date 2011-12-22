package com.afforess.minecartmaniachestcontrol.itemcontainer;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

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
        final ItemStack[] cartContents = withdraw.getContents();
        final ItemStack[] chestContents = inventory.getContents();
        for (final CompassDirection direction : directions) {
            for (final ItemStack item : cartContents) {
                if (item != null) {
                    for (final ItemMatcher matcher : getMatchers(direction)) {
                        if (matcher == null) {
                            continue;
                        }
                        
                        if (matcher.match(item)) {
                            for (int i = 0; i < chestContents.length; i++) {
                                final ItemStack slotContents = inventory.getItem(i);
                                
                                // Get the amount we want to add to the slot
                                int amount = item.getAmount();
                                
                                // Get the maximum stack size (or just 64 if we've disabled that)
                                int maxamount = MinecartManiaWorld.getMaxStackSize(item);
                                
                                // Non-empty slot
                                if (slotContents != null) {
                                    // Ensure we have the same ID and durability and enchantments
                                    if (slotContents.getTypeId() == item.getTypeId() && slotContents.getDurability() == item.getDurability() && slotContents.getEnchantments() == item.getEnchantments()) {
                                        
                                        // Figure out how much we have to add to complete the stack.
                                        amount = Math.min(amount, Math.max(0, maxamount - amount));
                                        
                                    } else {
                                        // Skip it, not the same.
                                        continue;
                                    }
                                }
                                // Try to remove the item from the cart.
                                if (!withdraw.removeItem(item.getTypeId(), amount, item.getDurability())) {
                                    //Failed, restore backup of inventory
                                    withdraw.setContents(cartContents);
                                    inventory.setContents(chestContents);
                                    return;
                                }
                                
                                // Awesome, add it to the stand.
                                inventory.setItem(i, new ItemStack(item.getTypeId(), amount, item.getDurability()));
                            }
                        }
                    }
                }
            }
        }
    }
}
