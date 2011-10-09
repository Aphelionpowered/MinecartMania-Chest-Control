package com.afforess.minecartmaniachestcontrol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.minecraft.server.CraftingManager;
import net.minecraft.server.ShapedRecipes;
import net.minecraft.server.ShapelessRecipes;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class RecipeManager {
    public static class RecipeData {
        public List<ItemStack> ingredients = new ArrayList<ItemStack>();
        public ItemStack results=null;
    }
    
    private static Map<Integer,RecipeData> recipes = new HashMap<Integer,RecipeData>();
    
    public static void init() {
        recipes.clear();
        for(Object or : CraftingManager.getInstance().b()) {
            RecipeData recipeData = new RecipeData();
            if(or instanceof ShapedRecipes) {
                ShapedRecipes recipe = (ShapedRecipes) or;
                recipeData.results = new CraftItemStack(recipe.b());
                recipeData.ingredients = extractIngredients(recipe);
                
            }
            else if(or instanceof ShapelessRecipes) {
                ShapelessRecipes recipe = (ShapelessRecipes) or;
                recipeData.results = new CraftItemStack(recipe.b());
                recipeData.ingredients = extractIngredients(recipe);
            }
            
            if(recipeData.ingredients!=null) {
                recipes.put(recipeData.results.getTypeId(),recipeData);
                Logger.getLogger("Minecraft").info("[RecipeManager] Recipe for "+recipeData.results.getType().name()+":");
                for(ItemStack ingredient : recipeData.ingredients) {
                    Logger.getLogger("Minecraft").info(" * "+ingredient.getAmount()+"x "+ingredient.getType().name());
                }
            }
        }
    }
    
    private static List<ItemStack> extractIngredients(ShapelessRecipes recipe) {
        List<net.minecraft.server.ItemStack> stuff;
        try {
            stuff = new ArrayList<net.minecraft.server.ItemStack>((List) getPrivateField(recipe,"b"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        Map<net.minecraft.server.ItemStack,Integer> amt = new HashMap<net.minecraft.server.ItemStack,Integer>();
        
        List<ItemStack> ingredients = new ArrayList<ItemStack>();
        for(net.minecraft.server.ItemStack item : stuff) {
            org.bukkit.inventory.ItemStack itemStack = new CraftItemStack(item);
            ingredients.add(itemStack);
        }
        return ingredients;
    }
    
    
    
    private static List<ItemStack> extractIngredients(ShapedRecipes recipe) {
        int sX,sY;
        
        try {
            sX = (Integer) getPrivateField(recipe,"b");
            sY=(Integer) getPrivateField(recipe,"c");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        Map<net.minecraft.server.ItemStack,Integer> amt = new HashMap<net.minecraft.server.ItemStack,Integer>();
        
        for(int x=0;x<3;x++){
            for(int y=0;y<3;y++) {
                net.minecraft.server.ItemStack item = getRecipeIngredient(recipe,x,y,true);
                if(item != null) {
                    item.count=1;
                    if(amt.containsKey(item)) {
                        amt.put(item,amt.get(item)+1);
                    }
                }
            }
        }
        
        List<ItemStack> ingredients = new ArrayList<ItemStack>();
        for(Entry<net.minecraft.server.ItemStack, Integer> item : amt.entrySet()) {
            org.bukkit.inventory.ItemStack itemStack = new CraftItemStack((net.minecraft.server.ItemStack)item.getKey());
            itemStack.setAmount(item.getValue());
            ingredients.add(itemStack);
        }
        return ingredients;
    }
    
    private static net.minecraft.server.ItemStack getRecipeIngredient(ShapedRecipes recipe, int x, int y, boolean var4) {
        int sX,sY;
        net.minecraft.server.ItemStack[] ingredients;
        
        try {
            ingredients = (net.minecraft.server.ItemStack[]) getPrivateField(recipe,"d");
            sX = (Integer) getPrivateField(recipe,"b");
            sY=(Integer) getPrivateField(recipe,"c");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
        // Get useful stuff out of the recipe
        net.minecraft.server.ItemStack ingredient = null;
        if(x >= 0 && y >= 0 && x < sX && y < sY) {
            if(var4) {
                ingredient = ingredients[sX - x - 1 + y * sX];
            } else {
                ingredient = ingredients[x + y * sX];
            }
        }
        
        return ingredient;
    }
    
    private static Object getPrivateField(Object object, String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Get Recipe contents via reflection
        Class<?> srClass = object.getClass();
        Field fD = srClass.getDeclaredField(field);
        fD.setAccessible(true);
        return fD.get(object);
    }
    
    public static RecipeData findRecipe(String line) {
        for(Integer itemID:recipes.keySet()) {
            if(line.equals(itemID) || line.equals("["+itemID+"]")) 
                return recipes.get(itemID);
            Material mat = Material.getMaterial(line);
            if(mat!=null)
                return recipes.get(mat.getId());
        }
        return null;
    }
}
