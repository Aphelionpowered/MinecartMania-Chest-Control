package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
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
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

public abstract class ChestStorage {
    
    public static Location getSpawnLocation(final MinecartManiaChest chest) {
        final Block center = chest.getLocation().getBlock();
        Location result = getAdjacentTrack(center);
        if ((result == null) && (chest.getNeighborChest() != null)) {
            result = getAdjacentTrack(chest.getNeighborChest().getLocation().getBlock());
        }
        return result;
    }
    
    private static Location getAdjacentTrack(final Block center) {
        if (MinecartUtils.isTrack(center.getRelative(-1, 0, 0)))
            return center.getRelative(-1, 0, 0).getLocation();
        else if (MinecartUtils.isTrack(center.getRelative(-1, -1, 0)) && MinecartUtils.isSlopedTrack(center.getRelative(-1, -1, 0)))
            return center.getRelative(-1, 0, 0).getLocation();
        if (MinecartUtils.isTrack(center.getRelative(0, 0, -1)))
            return center.getRelative(0, 0, -1).getLocation();
        else if (MinecartUtils.isTrack(center.getRelative(0, -1, -1)) && MinecartUtils.isSlopedTrack(center.getRelative(0, -1, -1)))
            return center.getRelative(0, 0, -1).getLocation();
        if (MinecartUtils.isTrack(center.getRelative(1, 0, 0)))
            return center.getRelative(1, 0, 0).getLocation();
        else if (MinecartUtils.isTrack(center.getRelative(1, -1, 0)) && MinecartUtils.isSlopedTrack(center.getRelative(1, -1, 0)))
            return center.getRelative(1, 0, 0).getLocation();
        if (MinecartUtils.isTrack(center.getRelative(0, 0, 1)))
            return center.getRelative(0, 0, 1).getLocation();
        else if (MinecartUtils.isTrack(center.getRelative(0, -1, 1)) && MinecartUtils.isSlopedTrack(center.getRelative(0, -1, 1)))
            return center.getRelative(0, 0, 1).getLocation();
        return null;
    }
    
    public static boolean doMinecartCollection(final MinecartManiaMinecart minecart) {
        if (minecart.getBlockTypeAhead() != null) {
            if (minecart.getBlockTypeAhead().getType().getId() == Material.CHEST.getId()) {
                final MinecartManiaChest chest = MinecartManiaWorld.getMinecartManiaChest((Chest) minecart.getBlockTypeAhead().getState());
                
                if (SignCommands.isNoCollection(chest))
                    return false;
                
                if (minecart instanceof MinecartManiaStorageCart) {
                    final MinecartManiaStorageCart storageCart = (MinecartManiaStorageCart) minecart;
                    boolean failed = false;
                    for (final ItemStack item : storageCart.getInventory().getContents()) {
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
    
    public static boolean doCollectParallel(final MinecartManiaMinecart minecart) {
        final ArrayList<Block> blockList = minecart.getParallelBlocks();
        for (final Block block : blockList) {
            if (block.getState() instanceof Chest) {
                final MinecartManiaChest chest = MinecartManiaWorld.getMinecartManiaChest((Chest) block.getState());
                final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 1);
                for (final Sign sign : signList) {
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
    
    private static int getNumItemsInBlock(final Material m) {
        switch (m) {
            case CLAY_BALL:
            case SNOW_BALL:
            case STRING:
                return 4;
            default:
                return 9;
        }
    }
    
    public static void debug(final MinecartManiaStorageCart minecart, final String msg) {
        if (minecart.getDataValue("MMDebug") != null) {
            System.out.println(msg);
        }
    }
    
    public static void doCrafting(final MinecartManiaStorageCart minecart) {
        //Efficiency. Don't process overlapping tiles repeatedly, waste of time
        final int interval = minecart.getDataValue("Craft Interval") == null ? -1 : (Integer) minecart.getDataValue("Farm Interval");
        if (interval > 0) {
            minecart.setDataValue("Craft Interval", interval - 1);
        } else {
            minecart.setDataValue("Craft Interval", minecart.getRange() / 2);
            final HashSet<Block> blockList = minecart.getAdjacentBlocks(minecart.getRange());
            for (final Block block : blockList) {
                if (block.getTypeId() == Material.WORKBENCH.getId()) {
                    final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(block.getLocation(), 2);
                    for (final Sign sign : signList) {
                        if (sign.getLine(0).toLowerCase().contains("craft items")) {
                            sign.setLine(0, "[Craft Items]");
                            // For each line on the sign
                            String itemListString = "";
                            for (int i = 1; i < sign.getNumLines(); i++) {
                                if (i > 1) {
                                    itemListString += ":";
                                }
                                itemListString += sign.getLine(i);
                            }
                            for (final SpecificMaterial item : ItemUtils.getItemStringListToMaterial(itemListString.split(":"))) {
                                // Get the recipe, if possible
                                final RecipeData recipe = RecipeManager.findRecipe(item);
                                
                                if (recipe == null) {
                                    continue; // Skip if we can't find it.
                                }
                                if ((recipe.ingredients == null) || (recipe.ingredients.size() == 0)) {
                                    continue;
                                }
                                
                                boolean outOfIngredients = false;
                                
                                int loops = 0;
                                
                                final List<ItemStack> fixedIngredients = new ArrayList<ItemStack>();
                                
                                debug(minecart, "RECIPE: " + recipe.results.toString() + " (d: " + recipe.results.getDurability() + ")");
                                // Until we're out of ingredients, or the loop has been executed 64 times.
                                while (!outOfIngredients && (loops < 64)) {
                                    fixedIngredients.clear();
                                    
                                    loops++;
                                    // Loop through the list of ingredients for this recipe
                                    for (final ItemStack stack : recipe.ingredients) {
                                        boolean found = false;
                                        
                                        if (stack.getDurability() == (short) -1) {
                                            // See what we have
                                            ItemStack subitem = null;
                                            for (int is = 0; is < minecart.size(); is++) {
                                                final ItemStack si = minecart.getItem(is);
                                                if ((si != null) && (si.getTypeId() == stack.getTypeId())) {
                                                    subitem = si;
                                                    break;
                                                }
                                                
                                            }
                                            if (subitem == null) {
                                                continue;
                                            }
                                            stack.setDurability(subitem.getDurability());
                                            
                                            // See if we have the needed ingredient
                                            final int num = minecart.amount(stack.getTypeId(), stack.getDurability());
                                            if (minecart.amount(stack.getTypeId(), stack.getDurability()) < stack.getAmount()) {
                                                continue;
                                            } else {
                                                debug(minecart, "Cart has " + num + " " + recipe.results.toString() + " (d: " + recipe.results.getDurability() + ")!");
                                                found = true;
                                                break;
                                            }
                                        } else {
                                            if (stack.getDurability() == -1) {
                                                stack.setDurability((short) 0);
                                            }
                                            
                                            // See if we have the needed ingredients
                                            if (minecart.amount(stack.getTypeId(), stack.getDurability()) >= stack.getAmount()) {
                                                found = true;
                                            } else {
                                                debug(minecart, "OOI: " + stack.toString() + " (d: " + stack.getDurability() + ")");
                                                outOfIngredients = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            outOfIngredients = true;
                                            debug(minecart, "OOI: " + stack.toString() + " (d: " + stack.getDurability() + ")");
                                            break;
                                        } else {
                                            //debug(minecart, "Ingredient found: " + stack.toString() + " (d: " + stack.getDurability() + ")");
                                            fixedIngredients.add(stack);
                                        }
                                    }
                                    
                                    if (outOfIngredients) {
                                        break;
                                    }
                                    
                                    // Double-check
                                    debug(minecart, "Recipe for " + recipe.results.toString() + " (d: " + recipe.results.getDurability() + ")");
                                    for (final ItemStack stack : fixedIngredients) {
                                        if (minecart.canRemoveItem(stack.getTypeId(), stack.getAmount(), stack.getDurability())) {
                                            debug(minecart, " + " + stack.toString() + " (d: " + stack.getDurability() + ")");
                                        } else {
                                            debug(minecart, "OOI: " + stack.toString() + " (d: " + stack.getDurability() + ")");
                                            outOfIngredients = true;
                                            break;
                                        }
                                    }
                                    
                                    if (outOfIngredients) {
                                        break;
                                    }
                                    
                                    if (!minecart.canAddItem(recipe.results)) {
                                        debug(minecart, "CAI: " + recipe.results.toString());
                                        outOfIngredients = true;
                                        break;
                                    }
                                    
                                    // Loop through again to actually remove the items
                                    for (final ItemStack stack : fixedIngredients) {
                                        debug(minecart, "[Craft Items] Removed " + stack.toString() + " (d: " + stack.getDurability() + ") from minecart!");
                                        minecart.removeItem(stack.getTypeId(), stack.getAmount(), stack.getDurability());
                                    }
                                    // Take it from the cart
                                    minecart.addItem(recipe.results);
                                    debug(minecart, "[Craft Items] Added " + recipe.results.toString() + " to minecart!");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void doItemCompression(final MinecartManiaStorageCart minecart) {
        final HashSet<Block> blockList = minecart.getAdjacentBlocks(minecart.getRange());
        for (final Block block : blockList) {
            if (block.getTypeId() == Material.WORKBENCH.getId()) {
                final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(block.getLocation(), 2);
                for (final Sign sign : signList) {
                    for (int i = 0; i < sign.getNumLines(); i++) {
                        if (sign.getLine(i).toLowerCase().contains("compress items") || sign.getLine(i).toLowerCase().contains("compress")) {
                            sign.setLine(i, "[Compress Items]");
                            //TODO handling for custom recipies?
                            final Material[][] compressable = { { Material.IRON_INGOT, Material.GOLD_INGOT, Material.INK_SACK, Material.DIAMOND, Material.CLAY_BALL, Material.SNOW_BALL }, { Material.IRON_BLOCK, Material.GOLD_BLOCK, Material.LAPIS_BLOCK, Material.DIAMOND_BLOCK, Material.CLAY, Material.SNOW_BLOCK } };
                            int n = 0;
                            for (final Material m : compressable[0]) {
                                final ItemStack masItem = new ItemStack(m.getId(), (Material.INK_SACK == m) ? 4 : 0);
                                final int amtPerBlock = getNumItemsInBlock(m);
                                int amt = 0;
                                int slot = 0;
                                for (final ItemStack item : minecart.getContents()) {
                                    if ((item != null) && (item.getTypeId() == masItem.getTypeId()) && (item.getDurability() == masItem.getDurability())) {
                                        amt += item.getAmount();
                                        minecart.setItem(slot, null);
                                    }
                                    slot++;
                                }
                                int compressedAmt = amt / amtPerBlock;
                                final int left = amt % amtPerBlock;
                                while (compressedAmt > 0) {
                                    minecart.addItem(compressable[1][n].getId(), Math.min(64, compressedAmt));
                                    compressedAmt -= Math.min(64, compressedAmt);
                                }
                                if (left > 0) {
                                    minecart.addItem(compressable[0][n].getId(), left);
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
