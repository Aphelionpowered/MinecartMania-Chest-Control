package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniachestcontrol.RecipeManager.RecipeData;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public abstract class ChestStorage {
    
    public static Location getSpawnLocation(MinecartManiaChest chest) {
        Block center = chest.getLocation().getBlock();
        Location result = getAdjacentTrack(center);
        if (result == null && chest.getNeighborChest() != null) {
            result = getAdjacentTrack(chest.getNeighborChest().getLocation().getBlock());
        }
        return result;
    }
    
    private static Location getAdjacentTrack(Block center) {
        if (MinecartUtils.isTrack(center.getRelative(-1, 0, 0))) {
            return center.getRelative(-1, 0, 0).getLocation();
        } else if (MinecartUtils.isTrack(center.getRelative(-1, -1, 0)) && MinecartUtils.isSlopedTrack(center.getRelative(-1, -1, 0))) {
            return center.getRelative(-1, 0, 0).getLocation();
        }
        if (MinecartUtils.isTrack(center.getRelative(0, 0, -1))) {
            return center.getRelative(0, 0, -1).getLocation();
        } else if (MinecartUtils.isTrack(center.getRelative(0, -1, -1)) && MinecartUtils.isSlopedTrack(center.getRelative(0, -1, -1))) {
            return center.getRelative(0, 0, -1).getLocation();
        }
        if (MinecartUtils.isTrack(center.getRelative(1, 0, 0))) {
            return center.getRelative(1, 0, 0).getLocation();
        } else if (MinecartUtils.isTrack(center.getRelative(1, -1, 0)) && MinecartUtils.isSlopedTrack(center.getRelative(1, -1, 0))) {
            return center.getRelative(1, 0, 0).getLocation();
        }
        if (MinecartUtils.isTrack(center.getRelative(0, 0, 1))) {
            return center.getRelative(0, 0, 1).getLocation();
        } else if (MinecartUtils.isTrack(center.getRelative(0, -1, 1)) && MinecartUtils.isSlopedTrack(center.getRelative(0, -1, 1))) {
            return center.getRelative(0, 0, 1).getLocation();
        }
        return null;
    }
    
    public static boolean doMinecartCollection(MinecartManiaMinecart minecart) {
        if (minecart.getBlockTypeAhead() != null) {
            if (minecart.getBlockTypeAhead().getType().getId() == Item.CHEST.getId()) {
                MinecartManiaChest chest = MinecartManiaWorld.getMinecartManiaChest((Chest) minecart.getBlockTypeAhead().getState());
                
                if (SignCommands.isNoCollection(chest)) {
                    return false;
                }
                
                if (minecart instanceof MinecartManiaStorageCart) {
                    MinecartManiaStorageCart storageCart = (MinecartManiaStorageCart) minecart;
                    boolean failed = false;
                    for (ItemStack item : storageCart.getInventory().getContents()) {
                        if (!chest.addItem(item)) {
                            failed = true;
                            break;
                        }
                    }
                    if (!failed) {
                        storageCart.getInventory().clear();
                    }
                }
                if (chest.addItem(minecart.getType().getId())) {
                    
                    minecart.kill(false);
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean doCollectParallel(MinecartManiaMinecart minecart) {
        ArrayList<Block> blockList = minecart.getParallelBlocks();
        for (Block block : blockList) {
            if (block.getState() instanceof Chest) {
                MinecartManiaChest chest = MinecartManiaWorld.getMinecartManiaChest((Chest) block.getState());
                ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 1);
                for (Sign sign : signList) {
                    for (int i = 0; i < sign.getNumLines(); i++) {
                        if (sign.getLine(i).toLowerCase().contains("parallel")) {
                            sign.setLine(i, "[Parallel]");
                            if (!minecart.isMovingAway(block.getLocation())) {
                                if (chest.addItem(minecart.getType().getId())) {
                                    minecart.kill(false);
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private static int getNumItemsInBlock(Item item) {
        switch (item) {
            case CLAY_BALL:
            case SNOW_BALL:
            case STRING:
                return 4;
            default:
                return 9;
        }
    }
    
    public static void doCrafting(MinecartManiaStorageCart minecart) {
        HashSet<Block> blockList = minecart.getAdjacentBlocks(minecart.getRange());
        for (Block block : blockList) {
            if (block.getTypeId() == Item.WORKBENCH.getId()) {
                ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(block.getLocation(), 2);
                for (Sign sign : signList) {
                    if (sign.getLine(0).toLowerCase().contains("craft items")) {
                        sign.setLine(0, "[Craft Items]");
                        // For each line on the sign
                        String itemListString = "";
                        for (int i = 1; i < sign.getNumLines(); i++) {
                            if (i > 1)
                                itemListString += ":";
                            itemListString += sign.getLine(i);
                        }
                        for (AbstractItem item : ItemUtils.getItemStringListToMaterial(itemListString.split(":"))) {
                            // Get the recipe, if possible
                            RecipeData recipe = RecipeManager.findRecipe(item);
                            
                            if (recipe == null)
                                continue; // Skip if we can't find it.
                            if (recipe.ingredients == null || recipe.ingredients.size() == 0)
                                continue;
                            
                            boolean outOfIngredients = false;
                            
                            int loops = 0;
                            
                            //System.out.println("RECIPE: "+recipe.results.toString());
                            // Until we're out of ingredients, or the loop has been executed 64 times.
                            while (!outOfIngredients && loops < 64) {
                                loops++;
                                // Loop through the list of ingredients for this recipe
                                for (ItemStack stack : recipe.ingredients) {
                                    // See if we have the needed ingredient
                                    Item sitem = Item.getItem(stack);
                                    if (sitem == null) {
                                        System.out.println("Could not find item for " + stack.toString());
                                        outOfIngredients = true;
                                        break;
                                    }
                                    if (minecart.amount(sitem) < stack.getAmount()) {
                                        // Otherwise, break out of the loop.
                                        outOfIngredients = true;
                                        //System.out.println("OOI: "+stack.toString());
                                        break;
                                    }
                                }
                                
                                if (outOfIngredients)
                                    break;
                                
                                if (!minecart.canAddItem(recipe.results)) {
                                    //System.out.println("CAI: "+recipe.results.toString());
                                    outOfIngredients = true;
                                    break;
                                }
                                
                                // Loop through again to actually remove the items
                                for (ItemStack stack : recipe.ingredients) {
                                    //System.out.println("[Craft Items] Removed "+stack.toString()+" from minecart!");
                                    minecart.removeItem(stack.getTypeId(), stack.getAmount(), stack.getDurability());
                                }
                                // Take it from the cart
                                minecart.addItem(recipe.results);
                                //System.out.println("[Craft Items] Added "+recipe.results.toString()+" to minecart!");
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void doItemCompression(MinecartManiaStorageCart minecart) {
        HashSet<Block> blockList = minecart.getAdjacentBlocks(minecart.getRange());
        for (Block block : blockList) {
            if (block.getTypeId() == Item.WORKBENCH.getId()) {
                ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(block.getLocation(), 2);
                for (Sign sign : signList) {
                    for (int i = 0; i < sign.getNumLines(); i++) {
                        if (sign.getLine(i).toLowerCase().contains("compress items") || sign.getLine(i).toLowerCase().contains("compress")) {
                            sign.setLine(i, "[Compress Items]");
                            //TODO handling for custom recipies?
                            Item[][] compressable = { { Item.IRON_INGOT, Item.GOLD_INGOT, Item.LAPIS_LAZULI, Item.DIAMOND, Item.CLAY_BALL, Item.SNOW_BALL }, { Item.IRON_BLOCK, Item.GOLD_BLOCK, Item.LAPIS_BLOCK, Item.DIAMOND_BLOCK, Item.CLAY, Item.SNOW_BLOCK } };
                            int n = 0;
                            for (Item m : compressable[0]) {
                                int amtPerBlock = getNumItemsInBlock(m);
                                int amt = 0;
                                int slot = 0;
                                for (ItemStack item : minecart.getContents()) {
                                    if (item != null && m.equals(Item.getItem(item))) {
                                        amt += item.getAmount();
                                        minecart.setItem(slot, null);
                                    }
                                    slot++;
                                }
                                int compressedAmt = amt / amtPerBlock;
                                int left = amt % amtPerBlock;
                                while (compressedAmt > 0) {
                                    minecart.addItem(compressable[1][n].getId(), Math.min(64, compressedAmt));
                                    compressedAmt -= Math.min(64, compressedAmt);
                                }
                                if (left > 0) {
                                    ItemStack item = compressable[0][n].toItemStack();
                                    item.setAmount(left);
                                    minecart.addItem(item);
                                }
                                
                                n++;
                            }
                        }
                    }
                }
            }
        }
    }
    /*
     * public static boolean doEmptyChestInventory(MinecartManiaStorageCart minecart) { ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2); for (Sign sign : signList) { if (sign.getLine(0).toLowerCase().contains("trash items")) { //return InventoryUtils.doInventoryTransaction(minecart, null, sign, minecart.getDirectionOfMotion()); } } return false; }
     * 
     * public static void setMaximumItems(MinecartManiaStorageCart minecart) { ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2); for (Sign sign : signList) { if (sign.getLine(0).toLowerCase().contains("max items")) { String[] list = {sign.getLine(1), sign.getLine(2), sign.getLine(3) }; AbstractItem[] items = ItemUtils.getItemStringListToMaterial(list); for (AbstractItem item : items) { if (!item.isInfinite()) { minecart.setMaximumItem(item.type(), item.getAmount()); } } sign.setLine(0, "[Max Items]"); if (!sign.getLine(1).isEmpty()) { sign.setLine(1, StringUtils.addBrackets(sign.getLine(1))); } if (!sign.getLine(2).isEmpty()) { sign.setLine(2, StringUtils.addBrackets(sign.getLine(2))); } if (!sign.getLine(3).isEmpty()) { sign.setLine(3, StringUtils.addBrackets(sign.getLine(3))); } sign.update(); } } }
     * 
     * public static void setMinimumItems(MinecartManiaStorageCart minecart) { ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2); for (Sign sign : signList) { if (sign.getLine(0).toLowerCase().contains("min items")) { String[] list = {sign.getLine(1), sign.getLine(2), sign.getLine(3) }; AbstractItem[] items = ItemUtils.getItemStringListToMaterial(list); for (AbstractItem item : items) { if (!item.isInfinite()) { minecart.setMinimumItem(item.type(), item.getAmount()); } } sign.setLine(0, "[Min Items]"); if (!sign.getLine(1).isEmpty()) { sign.setLine(1, StringUtils.addBrackets(sign.getLine(1))); } if (!sign.getLine(2).isEmpty()) { sign.setLine(2, StringUtils.addBrackets(sign.getLine(2))); } if (!sign.getLine(3).isEmpty()) { sign.setLine(3, StringUtils.addBrackets(sign.getLine(3))); } sign.update(); } } }
     */
}
