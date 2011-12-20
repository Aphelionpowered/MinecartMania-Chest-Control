package com.afforess.minecartmaniachestcontrol.signs;

import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.utils.ItemUtils;

public class MinimumItemAction implements SignAction {
    protected ItemMatcher[] matchers = null;
    
    public MinimumItemAction(final Sign sign) {
        matchers = ItemUtils.getItemStringListToMatchers(sign.getLines(), CompassDirection.NO_DIRECTION);
    }
    
    public boolean execute(final MinecartManiaMinecart minecart) {
        if (minecart.isStorageMinecart()) {
            for (final ItemMatcher matcher : matchers) {
                if (matcher == null) {
                    continue;
                }
                ((MinecartManiaStorageCart) minecart).setMinimumItem(matcher);
                
            }
            return true;
        }
        return false;
    }
    
    public boolean async() {
        return true;
    }
    
    public boolean valid(final Sign sign) {
        if (sign.getLine(0).toLowerCase().contains("min item")) {
            sign.addBrackets();
            return true;
        }
        return false;
    }
    
    public String getName() {
        return "minimumitemsign";
    }
    
    public String getFriendlyName() {
        return "Minimum Item Sign";
    }
    
}
