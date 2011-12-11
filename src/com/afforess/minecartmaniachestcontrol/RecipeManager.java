package com.afforess.minecartmaniachestcontrol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.CraftingManager;
import net.minecraft.server.ShapedRecipes;
import net.minecraft.server.ShapelessRecipes;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.SpecificMaterial;

public class RecipeManager {
    public static class RecipeData {
        public List<ItemStack> ingredients = new ArrayList<ItemStack>();
        public ItemStack results = null;
    }
    
    private static List<RecipeData> recipes = new ArrayList<RecipeData>();
    
    public static void init() {
        recipes.clear();
        //Iterate through recipes
        for (Object or : CraftingManager.getInstance().b()) {
            // Create new recipe structure
            RecipeData recipeData = new RecipeData();
            if (or instanceof ShapedRecipes) {
                // Load shaped recipe
                ShapedRecipes recipe = (ShapedRecipes) or;
                recipeData.results = new CraftItemStack(recipe.b());
                recipeData.ingredients = extractIngredients(recipe);
                
            } else if (or instanceof ShapelessRecipes) {
                // Load shapeless recipe
                ShapelessRecipes recipe = (ShapelessRecipes) or;
                recipeData.results = new CraftItemStack(recipe.b());
                recipeData.ingredients = extractIngredients(recipe);
            }
            
            if (recipeData.ingredients != null && recipeData.ingredients.size() > 0) {
                recipes.add(recipeData);
                //Logger.getLogger("Minecraft").info("[RecipeManager] Recipe for " + recipeData.results.getType().name() + " (" + recipeData.results.getDurability() + "):");
                //for (ItemStack ingredient : recipeData.ingredients) {
                //    Logger.getLogger("Minecraft").info(" * " + ingredient.getAmount() + "x " + ingredient.getType().name() + " (" + ingredient.getDurability() + ")");
                //}
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<ItemStack> extractIngredients(ShapelessRecipes recipe) {
        List<net.minecraft.server.ItemStack> stuff;
        try {
            stuff = new ArrayList<net.minecraft.server.ItemStack>((List<net.minecraft.server.ItemStack>) getPrivateField(recipe, "b"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        List<ItemStack> ingredients = new ArrayList<ItemStack>();
        for (net.minecraft.server.ItemStack item : stuff) {
            boolean found = false;
            for (int j = 0; j < ingredients.size(); j++) {
                ItemStack b_item = ingredients.get(j);
                if (b_item.getTypeId() == item.id && b_item.getDurability() == item.getData()) {
                    b_item.setAmount(b_item.getAmount() + 1);
                    if (b_item.getAmount() > 9)
                        b_item.setAmount(9);
                    ingredients.set(j, b_item);
                    found = true;
                    break;
                }
            }
            if (!found) {
                ItemStack is = new CraftItemStack(item.cloneItemStack());
                if (is.getAmount() > 9)
                    is.setAmount(9);
                ingredients.add(is);
            }
        }
        for (int j = 0; j < ingredients.size(); j++) {
            ItemStack citem = ingredients.get(j);
            if (citem.getAmount() > 9)
                citem.setAmount(9);
            ingredients.set(j, citem);
        }
        return ingredients;
    }
    
    private static List<ItemStack> extractIngredients(ShapedRecipes recipe) {
        net.minecraft.server.ItemStack[] ingredients;
        
        try {
            ingredients = (net.minecraft.server.ItemStack[]) getPrivateField(recipe, "d");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        List<ItemStack> r = new ArrayList<ItemStack>();
        
        for (int i = 0; i < ingredients.length; i++) {
            net.minecraft.server.ItemStack item = ingredients[i];
            if (item != null) {
                boolean found = false;
                for (int j = 0; j < r.size(); j++) {
                    ItemStack b_item = r.get(j);
                    if (b_item.getTypeId() == item.id && b_item.getDurability() == item.getData()) {
                        b_item.setAmount(b_item.getAmount() + 1);
                        if (b_item.getAmount() > 9)
                            b_item.setAmount(9);
                        r.set(j, b_item);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ItemStack is = new CraftItemStack(item.cloneItemStack());
                    if (is.getAmount() > 9)
                        is.setAmount(9);
                    r.add(is);
                }
            }
        }
        for (int j = 0; j < r.size(); j++) {
            ItemStack citem = r.get(j);
            if (citem.getAmount() > 9)
                citem.setAmount(9);
            r.set(j, citem);
        }
        return r;
    }
    
    private static Object getPrivateField(Object object, String field)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        // Get Recipe contents via reflection
        Class<?> srClass = object.getClass();
        Field fD = srClass.getDeclaredField(field);
        fD.setAccessible(true);
        return fD.get(object);
    }
    
    public static RecipeData findRecipe(SpecificMaterial item) {
        for (RecipeData recipe : recipes) {
            if (item.getId() == recipe.results.getTypeId()) {
                // Okay, correct type.  Now, let's see if data matters.
                if (item.durability==-1) {
                    // Nope!  We've found the recipe we're looking for.
                    return recipe;
                } else {
                    if (item.durability == recipe.results.getDurability())
                        return recipe;
                }
            }
        }
        return null;
    }
}
