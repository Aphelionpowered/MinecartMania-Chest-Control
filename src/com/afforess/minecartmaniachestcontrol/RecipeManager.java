package com.afforess.minecartmaniachestcontrol;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            
            if(recipeData.ingredients!=null && recipeData.ingredients.size()>0) {
                recipes.put(recipeData.results.getTypeId(),recipeData);
                Logger.getLogger("Minecraft").info("[RecipeManager] Recipe for "+recipeData.results.getType().name()+" ("+recipeData.results.getDurability()+"):");
                for(ItemStack ingredient : recipeData.ingredients) {
                    Logger.getLogger("Minecraft").info(" * "+ingredient.getAmount()+"x "+ingredient.getType().name()+" ("+ingredient.getDurability()+")");
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
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
            boolean found = false;
            for(int j = 0;j<ingredients.size();j++) {
                ItemStack b_item = ingredients.get(j);
                if(b_item.getTypeId() == item.id && b_item.getDurability() == item.damage) {
                    b_item.setAmount(b_item.getAmount()+1);
                    ingredients.set(j, b_item);
                    found=true;
                    break;
                }
            }
            if(!found) {
                ingredients.add(new CraftItemStack(item));
            }
        }
        return ingredients;
    }
    
    
    
    private static List<ItemStack> extractIngredients(ShapedRecipes recipe) {
        net.minecraft.server.ItemStack[] ingredients;
        
        try {
            ingredients = (net.minecraft.server.ItemStack[]) getPrivateField(recipe,"d");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        

        
        List<ItemStack> r = new ArrayList<ItemStack>();
        
        for(int i=0;i<ingredients.length;i++){
            net.minecraft.server.ItemStack item = ingredients[i];
            if(item != null) {
                boolean found = false;
                for(int j = 0;j<r.size();j++) {
                    ItemStack b_item = r.get(j);
                    if(b_item.getTypeId() == item.id && b_item.getDurability() == item.damage) {
                        b_item.setAmount(b_item.getAmount()+1);
                        r.set(j, b_item);
                        found=true;
                        break;
                    }
                }
                if(!found) {
                    r.add(new CraftItemStack(item));
                }
            }
        }
        return r;
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
