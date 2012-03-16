package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.ShapedRecipes;
import net.minecraft.server.ShapelessRecipes;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.afforess.minecartmaniacore.world.SpecificMaterial;

public class RecipeManager {
    public static class RecipeData {
        public Collection<ItemStack> ingredients = new ArrayList<ItemStack>();
        public ItemStack results = null;
    }
    
    private static List<RecipeData> recipes = new ArrayList<RecipeData>();
    
    public static void init() {
        recipes.clear();
        //Iterate through recipes
        Iterator<Recipe> ri = Bukkit.recipeIterator();
        while (ri.hasNext()) {
            Recipe rec = ri.next();
            // Create new recipe structure
            final RecipeData recipeData = new RecipeData();
            if (rec instanceof ShapedRecipes) {
                // Load shaped recipe
                final ShapedRecipes recipe = (ShapedRecipes) rec;
                recipeData.results = rec.getResult();
                recipeData.ingredients = extractIngredients(recipe);
                
            } else if (rec instanceof ShapelessRecipes) {
                // Load shapeless recipe
                final ShapelessRecipes recipe = (ShapelessRecipes) rec;
                recipeData.results = rec.getResult();
                recipeData.ingredients = extractIngredients(recipe);
            }
            
            if ((recipeData.ingredients != null) && (recipeData.ingredients.size() > 0)) {
                recipes.add(recipeData);
                Logger.getLogger("Minecraft").info("[RecipeManager] Recipe for " + recipeData.results.getType().name() + " (" + recipeData.results.getDurability() + "):");
                for (final ItemStack ingredient : recipeData.ingredients) {
                    Logger.getLogger("Minecraft").info(" * " + ingredient.getAmount() + "x " + ingredient.getType().name() + " (" + ingredient.getDurability() + ")");
                }
            }
        }
    }
    
    private static List<ItemStack> extractIngredients(final ShapelessRecipes recipe) {
        return recipe.toBukkitRecipe().getIngredientList();
    }
    
    private static Collection<ItemStack> extractIngredients(final ShapedRecipes recipe) {
        List<ItemStack> ingredients = new ArrayList<ItemStack>();
        Map<Character, ItemStack> map = recipe.toBukkitRecipe().getIngredientMap();
        slotloop: for (String slot : recipe.toBukkitRecipe().getShape()) {
            ItemStack is = map.get(slot);
            is.setAmount(1);
            for (int i = 0; i < ingredients.size(); i++) {
                if (ingredients.get(i).getTypeId() == is.getTypeId() && ingredients.get(i).getData() == is.getData()) {
                    ItemStack ingredient = ingredients.get(i);
                    ingredient.setAmount(ingredient.getAmount() + 1);
                    ingredients.set(i, ingredient);
                    continue slotloop;
                }
            }
            ingredients.add(is);
        }
        return ingredients;
    }
    
    public static RecipeData findRecipe(final SpecificMaterial item) {
        for (final RecipeData recipe : recipes) {
            if (item.getId() == recipe.results.getTypeId()) {
                // Okay, correct type.  Now, let's see if data matters.
                if (item.durability == -1)
                    // Nope!  We've found the recipe we're looking for.
                    return recipe;
                else {
                    if (item.durability == recipe.results.getDurability())
                        return recipe;
                }
            }
        }
        return null;
    }
}
