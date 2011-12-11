package com.afforess.minecartmaniachestcontrol.signs;

import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.ItemMatcher;
import com.afforess.minecartmaniacore.utils.ItemUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;

public class MinimumItemAction implements SignAction{
	protected ItemMatcher[] matchers = null;
	public MinimumItemAction(Sign sign) {
        this.matchers = ItemUtils.getItemStringListToMatchers(sign.getLines(), CompassDirection.NO_DIRECTION);
	}

	public boolean execute(MinecartManiaMinecart minecart) {
        if (minecart.isStorageMinecart()) {
            for (ItemMatcher matcher : matchers) {
                for (int i = 0; i < ((MinecartManiaStorageCart) minecart).size(); i++) {
                    ItemStack item = ((MinecartManiaStorageCart) minecart).getItem(i);
                    if (matcher.match(item)) {
                        ((MinecartManiaStorageCart) minecart).setMinimumItem(item.getTypeId(), item.getDurability(), matcher.getAmount());
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
