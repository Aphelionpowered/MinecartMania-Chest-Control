/**
 * 
 */
package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;

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
			AbstractItem[] list = getItemList(direction);
			for (AbstractItem item : list) {
				if (item != null) {
					if (item.isInfinite()) {
						item.setAmount(64);
					}
					short data = (short) (item.hasData() ? item.getData() : -1);
					//does not match the item already in the slot, continue
					if (stand.getItem(SLOT) != null && !item.equals(Item.getItem(stand.getItem(SLOT)))) {
						continue;
					}
					int toAdd = Math.min(item.getAmount(), withdraw.amount(item.type()));
					item.setAmount(toAdd);
					if (stand.getItem(SLOT) != null) {
						toAdd = Math.min(64 - stand.getItem(SLOT).getAmount(), toAdd);
						item.setAmount(stand.getItem(SLOT).getAmount() + toAdd);
					}
					if (withdraw.contains(item.type()) && withdraw.canRemoveItem(item.getId(), toAdd, data)) {
						if (stand.canAddItem(item.toItemStack())) {
							withdraw.removeItem(item.getId(), toAdd, data);
							stand.setItem(SLOT, item.toItemStack());
							return;
						}
					}
				}
			}
		}
	}

}
