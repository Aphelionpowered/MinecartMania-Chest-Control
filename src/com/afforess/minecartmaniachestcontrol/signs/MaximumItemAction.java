package com.afforess.minecartmaniachestcontrol.signs;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.utils.ItemUtils;

public class MaximumItemAction implements SignAction {
    protected ItemMatcher[] matchers = null;
    
    public MaximumItemAction(Sign sign) {
        this.matchers = ItemUtils.getItemStringToMatchers(sign.getLines(), CompassDirection.NO_DIRECTION);
    }
    
    public boolean execute(MinecartManiaMinecart minecart) {
        if (minecart.isStorageMinecart()) {
            for (ItemMatcher matcher : matchers) {
                for (int i = 0; i < ((MinecartManiaStorageCart) minecart).size(); i++) {
                    ItemStack item = ((MinecartManiaStorageCart) minecart).getItem(i);
                    if (matcher.match(item)) {
                        ((MinecartManiaStorageCart) minecart).setMaximumItem(item.getTypeId(), item.getDurability(), matcher.getAmount());
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean async() {
        return true;
    }
    
    public boolean valid(Sign sign) {
        if (sign.getLine(0).toLowerCase().contains("max item")) {
            sign.addBrackets();
            return true;
        }
        return false;
    }
    
    public String getName() {
        return "maximumitemsign";
    }
    
    public String getFriendlyName() {
        return "Maximum Item Sign";
    }
    
}
