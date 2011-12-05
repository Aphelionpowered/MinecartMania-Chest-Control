package com.afforess.minecartmaniachestcontrol.itemcontainer;

import com.afforess.minecartmaniacore.inventory.MinecartManiaBrewingStand;
import com.afforess.minecartmaniacore.inventory.MinecartManiaInventory;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.AbstractItem;
import com.afforess.minecartmaniacore.world.Item;

public class BrewingStandDepositItemContainer extends GenericItemContainer
		implements ItemContainer {

	private MinecartManiaBrewingStand brewingStand;

	public BrewingStandDepositItemContainer(MinecartManiaBrewingStand brewingStand, String line,
			CompassDirection direction) {
		super(line, direction);
		this.brewingStand=brewingStand;
	}

	public void doCollection(MinecartManiaInventory deposit) {
		for (CompassDirection direction : directions) {
			AbstractItem[] list = getItemList(direction);
			for (AbstractItem item : list) {
				if (item != null) {
					short data = (short) (item.hasData() ? item.getData() : -1);
					for(int slotId=0;slotId<3;slotId++) {
						//does not match the item already in the slotId, continue
						if (brewingStand.getItem(slotId) == null || !item.equals(Item.getItem(brewingStand.getItem(slotId)))) {
							continue;
						}
						int toRemove = brewingStand.getItem(slotId).getAmount();
						if (!item.isInfinite() && item.getAmount() < toRemove) {
							toRemove = item.getAmount();
						}
						item.setAmount(toRemove);
						if (brewingStand.canRemoveItem(item.getId(), toRemove, data)) {
							if (deposit.canAddItem(item.toItemStack())) {
								if (deposit.addItem(item.toItemStack())) {
									brewingStand.setItem(slotId, null);
								}
								else {
									return;
								}
							}
						}
					}
				}
			}
		}
	}

}
