package com.afforess.minecartmaniachestcontrol;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.Material;

import com.afforess.minecartmaniachestcontrol.itemcontainer.ItemCollectionManager;
import com.afforess.minecartmaniachestcontrol.signs.MaximumItemAction;
import com.afforess.minecartmaniachestcontrol.signs.MinimumItemAction;
import com.afforess.minecartmaniacore.event.ChestPoweredEvent;
import com.afforess.minecartmaniacore.event.MinecartActionEvent;
import com.afforess.minecartmaniacore.event.MinecartDirectionChangeEvent;
import com.afforess.minecartmaniacore.event.MinecartManiaListener;
import com.afforess.minecartmaniacore.event.MinecartManiaSignFoundEvent;
import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.minecart.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.minecart.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.signs.SignAction;
import com.afforess.minecartmaniacore.utils.BlockUtils;
import com.afforess.minecartmaniacore.utils.ComparableLocation;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.world.MinecartManiaWorld;

public class MinecartManiaActionListener extends MinecartManiaListener {
    
    @Override
    public void onChestPoweredEvent(final ChestPoweredEvent event) {
        if (event.isPowered() && !event.isActionTaken()) {
            
            final MinecartManiaChest chest = event.getChest();
            final Material minecartType = SignCommands.getMinecartType(chest);
            Location spawnLocation = SignCommands.getSpawnLocationSignOverride(chest);
            if (spawnLocation == null) {
                spawnLocation = ChestStorage.getSpawnLocation(chest);
            }
            if ((spawnLocation != null) && chest.contains(minecartType)) {
                if (chest.canSpawnMinecart() && chest.removeItem(minecartType.getId())) {
                    final CompassDirection direction = SignCommands.getDirection(chest.getLocation(), spawnLocation);
                    final MinecartManiaMinecart minecart = MinecartManiaWorld.spawnMinecart(spawnLocation, minecartType, chest);
                    minecart.setMotion(direction, (Double) MinecartManiaWorld.getConfigurationValue("SpawnAtSpeed"));
                    event.setActionTaken(true);
                }
            }
        }
    }
    
    @Override
    public void onMinecartManiaSignFoundEvent(final MinecartManiaSignFoundEvent event) {
        final Sign sign = event.getSign();
        SignAction test = new MaximumItemAction(sign);
        if (test.valid(sign)) {
            sign.addSignAction(test);
        }
        test = new MinimumItemAction(sign);
        if (test.valid(sign)) {
            sign.addSignAction(test);
        }
    }
    
    @Override
    public void onMinecartActionEvent(final MinecartActionEvent event) {
        if (!event.isActionTaken()) {
            final MinecartManiaMinecart minecart = event.getMinecart();
            
            boolean action = false;
            
            if (!action) {
                action = ChestStorage.doMinecartCollection(minecart);
            }
            if (!action) {
                action = ChestStorage.doCollectParallel(minecart);
            }
            if (!action && minecart.isStorageMinecart()) {
                
                ItemCollectionManager.processItemContainer((MinecartManiaStorageCart) event.getMinecart());
                final HashSet<ComparableLocation> locations = calculateLocationsInRange((MinecartManiaStorageCart) event.getMinecart());
                findSigns(locations);
                ItemCollectionManager.createItemContainers((MinecartManiaStorageCart) event.getMinecart(), locations);
                ChestStorage.doItemCompression((MinecartManiaStorageCart) minecart);
                ChestStorage.doCrafting((MinecartManiaStorageCart) minecart);
            }
            event.setActionTaken(action);
        }
    }
    
    @Override
    public void onMinecartDirectionChangeEvent(final MinecartDirectionChangeEvent event) {
        if (event.getMinecart().isStorageMinecart()) {
            ItemCollectionManager.updateContainerDirections((MinecartManiaStorageCart) event.getMinecart());
        }
    }
    
    private HashSet<ComparableLocation> calculateLocationsInRange(final MinecartManiaStorageCart minecart) {
        final HashSet<ComparableLocation> previousBlocks = toComparableLocation(BlockUtils.getAdjacentLocations(minecart.getPrevLocation(), minecart.getItemRange()));
        final HashSet<ComparableLocation> current = toComparableLocation(BlockUtils.getAdjacentLocations(minecart.minecart.getLocation(), minecart.getItemRange()));
        current.removeAll(previousBlocks);
        return current;
    }
    
    private static HashSet<ComparableLocation> toComparableLocation(final HashSet<Location> set) {
        final HashSet<ComparableLocation> newSet = new HashSet<ComparableLocation>(set.size());
        for (final Location loc : set) {
            newSet.add(new ComparableLocation(loc));
        }
        return newSet;
    }
    
    private void findSigns(final Collection<ComparableLocation> locations) {
        final Iterator<ComparableLocation> i = locations.iterator();
        while (i.hasNext()) {
            final Location temp = i.next();
            if (!(temp.getBlock().getState() instanceof org.bukkit.block.Sign)) {
                i.remove();
            }
        }
    }
    
}
