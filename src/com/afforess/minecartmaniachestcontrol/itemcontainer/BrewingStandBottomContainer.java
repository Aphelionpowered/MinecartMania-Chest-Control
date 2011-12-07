package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;

public class BrewingStandBottomContainer extends GenericItemContainer implements
		ItemContainer {

	private MinecartManiaBrewingStand brewingStand;

	public BrewingStandBottomContainer(MinecartManiaBrewingStand bs, String line, CompassDirection direction) {
		super(line, direction);
		if (line.toLowerCase().contains("bottom")) {
			String[] split = line.split(":");
			line = "";
			for (String s : split) {
				if (!s.toLowerCase().contains("bottom")) {
					line += s + ":";
				}
			}
		}
		this.line=line;
		brewingStand=bs;
	}

	public void doCollection(MinecartManiaInventory withdraw) {
		for (CompassDirection direction : directions) {
		AbstractItem[] list = getItemList(direction);
		for (AbstractItem item : list) {
			if (item != null) {
				if (item.isInfinite()) {
					item.setAmount(1);
				}
				for(int slotId=0;slotId<3;slotId++) {
					short data = (short) (item.hasData() ? item.getData() : -1);
					//does not match the item already in the slotId, continue
					if (brewingStand.getItem(slotId) != null && !item.equals(Item.getItem(brewingStand.getItem(slotId)))) {
						continue;
					}
					int toAdd = Math.min(item.getAmount(), withdraw.amount(item.type()));
					item.setAmount(toAdd);
					if (brewingStand.getItem(slotId) != null) {
						toAdd = Math.min(1 - brewingStand.getItem(slotId).getAmount(), toAdd);
						item.setAmount(brewingStand.getItem(slotId).getAmount() + toAdd);
					}
					if (withdraw.contains(item.type()) && withdraw.canRemoveItem(item.getId(), toAdd, data)) {
						if (brewingStand.canAddItem(item.toItemStack())) {
							withdraw.removeItem(item.getId(), toAdd, data);
							brewingStand.setItem(slotId, item.toItemStack());
							return;
						}
					}
				}
			}
		}
	}
	}

}
