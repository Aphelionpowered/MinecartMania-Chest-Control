package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.world.SpecificMaterial;

public interface ItemContainer {
    
    public boolean hasDirectionCondition();
    
    public boolean hasAmountCondition();
    
    public SpecificMaterial[] getRawItemList();
    
    public ItemMatcher[] getMatchers(CompassDirection direction);
    
    public void addDirection(CompassDirection direction);
    
    public void doCollection(MinecartManiaInventory other);
    
}
