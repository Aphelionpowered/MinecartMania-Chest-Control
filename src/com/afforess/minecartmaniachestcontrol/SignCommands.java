package com.afforess.minecartmaniachestcontrol;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.afforess.minecartmaniacore.inventory.MinecartManiaChest;
import com.afforess.minecartmaniacore.signs.MinecartTypeSign;
import com.afforess.minecartmaniacore.signs.Sign;
import com.afforess.minecartmaniacore.utils.DirectionUtils.CompassDirection;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;

public class SignCommands {
    
    static Logger _log = Logger.getLogger("Minecraft");
    
    public static boolean isNoCollection(final MinecartManiaChest chest) {
        final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
        for (final Sign sign : signList) {
            for (int i = 0; i < sign.getNumLines(); i++) {
                if (sign.getLine(i).toLowerCase().contains("no collection")) {
                    sign.setLine(i, "[No Collection]");
                    return true;
                }
            }
        }
        return false;
    }
    
    public static Material getMinecartType(final MinecartManiaChest chest) {
        final ArrayList<com.afforess.minecartmaniacore.signs.Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
        for (final com.afforess.minecartmaniacore.signs.Sign sign : signList) {
            if (sign instanceof MinecartTypeSign) {
                final MinecartTypeSign type = (MinecartTypeSign) sign;
                if (type.canDispenseMinecartType(Material.MINECART)) {
                    if (chest.contains(Material.MINECART))
                        return Material.MINECART;
                }
                if (type.canDispenseMinecartType(Material.POWERED_MINECART)) {
                    if (chest.contains(Material.POWERED_MINECART))
                        return Material.POWERED_MINECART;
                }
                if (type.canDispenseMinecartType(Material.STORAGE_MINECART)) {
                    if (chest.contains(Material.STORAGE_MINECART))
                        return Material.STORAGE_MINECART;
                }
            }
        }
        
        //Returns standard minecart by default
        return Material.MINECART;
    }
    
    // NOTE: Bukkit getRelative() still seems to follow old directionality. As a quick fix,
    //       all calls to getAdjacentTrack() have been changed into "wrong" looking ones!
    public static Location getSpawnLocationSignOverride(final MinecartManiaChest chest) {
        final ArrayList<Sign> signList = SignUtils.getAdjacentMinecartManiaSignList(chest.getLocation(), 2);
        final Location spawn = chest.getLocation();
        Location result = null;
        final Block neighbor = chest.getNeighborChest() != null ? chest.getNeighborChest().getLocation().getBlock() : null;
        
        for (final Sign sign : signList) {
            for (int i = 0; i < sign.getNumLines(); i++) {
                if (sign.getLine(i).toLowerCase().contains("spawn north")) {
                    sign.setLine(i, "[Spawn North]");
                    result = getAdjacentTrack(spawn.getBlock(), BlockFace.EAST);
                    if ((result == null) && (neighbor != null))
                        return getAdjacentTrack(neighbor, BlockFace.EAST);
                    else
                        return result;
                }
                if (sign.getLine(i).toLowerCase().contains("spawn east")) {
                    sign.setLine(i, "[Spawn East]");
                    result = getAdjacentTrack(spawn.getBlock(), BlockFace.SOUTH);
                    if ((result == null) && (neighbor != null))
                        return getAdjacentTrack(neighbor, BlockFace.SOUTH);
                    else
                        return result;
                }
                if (sign.getLine(i).toLowerCase().contains("spawn south")) {
                    sign.setLine(i, "[Spawn South]");
                    result = getAdjacentTrack(spawn.getBlock(), BlockFace.WEST);
                    if ((result == null) && (neighbor != null))
                        return getAdjacentTrack(neighbor, BlockFace.WEST);
                    else
                        return result;
                }
                if (sign.getLine(i).toLowerCase().contains("spawn west")) {
                    sign.setLine(i, "[Spawn West]");
                    result = getAdjacentTrack(spawn.getBlock(), BlockFace.NORTH);
                    if ((result == null) && (neighbor != null))
                        return getAdjacentTrack(neighbor, BlockFace.NORTH);
                    else
                        return result;
                }
            }
        }
        
        return null;
    }
    
    private static Location getAdjacentTrack(final Block center, final BlockFace dir) {
        if (MinecartUtils.isTrack(center.getRelative(dir)))
            return center.getRelative(dir).getLocation();
        if ((center.getRelative(dir).getTypeId() == Material.CHEST.getId()) && MinecartUtils.isTrack(center.getRelative(dir).getRelative(dir)))
            return center.getRelative(dir).getRelative(dir).getLocation();
        return null;
    }
    
    public static CompassDirection getDirection(final Location loc1, final Location loc2) {
        if ((loc1.getBlockX() - loc2.getBlockX()) > 0)
            return CompassDirection.WEST;
        if ((loc1.getBlockX() - loc2.getBlockX()) < 0)
            return CompassDirection.EAST;
        if ((loc1.getBlockZ() - loc2.getBlockZ()) > 0)
            return CompassDirection.NORTH;
        if ((loc1.getBlockZ() - loc2.getBlockZ()) < 0)
            return CompassDirection.SOUTH;
        
        return CompassDirection.NO_DIRECTION;
    }
    
}
