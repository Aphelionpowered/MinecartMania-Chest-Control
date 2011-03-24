package com.afforess.minecartmaniachestcontrol;

import org.bukkit.Location;
import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaChest;
import com.afforess.minecartmaniacore.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.event.ChestPoweredEvent;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartNearEntityEvent;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.DirectionUtils;

public class MinecartManiaActionListener extends MinecartManiaListener{
	
	public void onChestPoweredEvent(ChestPoweredEvent event) {
		if (event.isPowered() && !event.isActionTaken()) {

			MinecartManiaChest chest = event.getChest();
			Item minecartType = ChestUtils.getMinecartType(chest);
			Location spawnLocation = ChestUtils.getSpawnLocationSignOverride(chest);
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX() - 1, chest.getY(), chest.getZ(), 2, DirectionUtils.CompassDirection.NORTH)){
				spawnLocation = new Location(chest.getWorld(), chest.getX() - 1, chest.getY(), chest.getZ());
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX() + 1, chest.getY(), chest.getZ(), 2, DirectionUtils.CompassDirection.SOUTH)){
				spawnLocation = new Location(chest.getWorld(), chest.getX() + 1, chest.getY(), chest.getZ());
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() - 1, 2, DirectionUtils.CompassDirection.EAST)){
				spawnLocation = new Location(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() - 1);
			}
			if (spawnLocation == null && MinecartUtils.validMinecartTrack(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() + 1, 2, DirectionUtils.CompassDirection.WEST)){
				spawnLocation = new Location(chest.getWorld(), chest.getX(), chest.getY(), chest.getZ() + 1);
			}
			if (spawnLocation != null && chest.contains(minecartType)) {
				if (chest.removeItem(minecartType.getId())) {
					CompassDirection direction = ChestUtils.getDirection(chest.getLocation(), spawnLocation);
					MinecartManiaMinecart minecart = MinecartManiaWorld.spawnMinecart(spawnLocation, minecartType, chest);
					minecart.setMotion(direction, MinecartManiaWorld.getDoubleValue(MinecartManiaWorld.getConfigurationValue("SpawnAtSpeed")));
					event.setActionTaken(true);
				}
			}
		}
	}
	
	public void onMinecartNearEntityEvent(MinecartNearEntityEvent event) {
		if (event.isActionTaken()) {
			return;
		}
		if (event.getEntity() instanceof org.bukkit.entity.Item) {
			org.bukkit.entity.Item item = (org.bukkit.entity.Item)event.getEntity();
			if (event.getMinecart().isStorageMinecart()) {
				MinecartManiaStorageCart minecart = (MinecartManiaStorageCart) event.getMinecart();
				if (minecart.addItem(item.getItemStack())) {
					item.remove();
					event.setActionTaken(true);
				}
			}
		}
	}
	
	public void onMinecartActionEvent(MinecartActionEvent event) {
		if (!event.isActionTaken()) {
			MinecartManiaMinecart minecart = event.getMinecart();
			
			boolean action = false;
			
			if (!action) {
				action = ChestStorage.doMinecartCollection(minecart);
			}
			if (!action) {
				action = ChestStorage.doCollectParallel(minecart);
			}
			if (!action && minecart.isStorageMinecart()) {
				
				//Efficiency. A faster way than pruning list of old blocks
				int interval = 1;
				if (minecart.getDataValue("Storage Interval") != null) {
					interval = (Integer)minecart.getDataValue("Storage Interval");
				}
				interval--;
				if (interval > 0) {
					minecart.setDataValue("Storage Interval", interval);
				}
				else {
					minecart.setDataValue("Storage Interval", minecart.getEntityDetectionRange());
					ChestStorage.doChestStorage((MinecartManiaStorageCart) minecart);
					ChestStorage.doFurnaceStorage((MinecartManiaStorageCart) minecart);
					ChestStorage.doItemCompression((MinecartManiaStorageCart) minecart);
				}

				ChestStorage.doEmptyChestInventory((MinecartManiaStorageCart) minecart);
				ChestStorage.setMaximumItems((MinecartManiaStorageCart) minecart);
				ChestStorage.setMinimumItems((MinecartManiaStorageCart) minecart);
			}
			event.setActionTaken(action);
		}
	}

}
