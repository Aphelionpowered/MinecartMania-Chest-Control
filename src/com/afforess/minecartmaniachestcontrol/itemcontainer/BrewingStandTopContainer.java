/**
 * 
 */
package com.afforess.minecartmaniachestcontrol.itemcontainer;


import org.bukkit.inventory.ItemStack;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.ItemMatcher;

/**
 * @author Rob
 *
 */
public class BrewingStandTopContainer extends GenericItemContainer implements ItemContainer{

	private static final int SLOT = 3;
	private MinecartManiaBrewingStand stand;
	public BrewingStandTopContainer(MinecartManiaBrewingStand mmbs, String catalyst, CompassDirection direction) {
		super(catalyst, direction);
		this.stand=mmbs;
		if (catalyst.toLowerCase().contains("top")) {
			String[] split = catalyst.split(":");
			catalyst = "";
			for (String s : split) {
				if (!s.toLowerCase().contains("top")) {
					catalyst += s + ":";
				}
			}
		}
		this.line = catalyst;
	}
	public void doCollection(MinecartManiaInventory withdraw) {
		for (CompassDirection direction : directions) {
			ItemMatcher[] list = getMatchers(direction);
			for (ItemMatcher matcher : list) {
				if (matcher != null) {
					//does not match the item already in the slot, continue
					if (stand.getItem(SLOT) != null && !matcher.match(stand.getItem(SLOT))) {
						continue;
					}
					ItemStack item=null;
					int numMatching=0;
					for(ItemStack compare : withdraw.getContents()) {
					    if(matcher.match(compare)) {
					        numMatching+=compare.getAmount();
					        if(item==null)
					        item=compare;
					    }
					}
					if(numMatching==0) 
					    continue;
					int toAdd = Math.min(matcher.getAmount(), numMatching);
					item.setAmount(toAdd);
					if (stand.getItem(SLOT) != null) {
						toAdd = Math.min(1 - stand.getItem(SLOT).getAmount(), toAdd);
						item.setAmount(stand.getItem(SLOT).getAmount() + toAdd);
					}
					if (withdraw.contains(item.getTypeId()) && withdraw.canRemoveItem(item.getTypeId(), toAdd, item.getDurability())) {
						if (stand.canAddItem(item)) {
							withdraw.removeItem(item.getTypeId(), toAdd, item.getDurability());
							stand.setItem(SLOT, item);
							return;
						}
					}
				}
			}
		}
	}

}
