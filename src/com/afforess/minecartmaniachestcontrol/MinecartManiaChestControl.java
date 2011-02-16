package com.afforess.minecartmaniachestcontrol;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;

import com.afforess.minecartmaniacore.Configuration;
import com.afforess.minecartmaniacore.MinecartManiaWorld;

public class MinecartManiaChestControl extends JavaPlugin {
	
	public MinecartManiaChestControl(PluginLoader pluginLoader,
			Server instance, PluginDescriptionFile desc, File folder,
			File plugin, ClassLoader cLoader) {
		super(pluginLoader, instance, desc, folder, plugin, cLoader);
		server = instance;
		description = desc;
	}

	public static Logger log;
	public static Server server;
	public static PluginDescriptionFile description;
	public static MinecartManiaActionListener listener = new MinecartManiaActionListener();
	
	

	public void onEnable(){
		log = Logger.getLogger("Minecraft");
		Configuration.loadConfiguration(description, SettingList.config);
	
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, listener, Priority.High, this);

        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
	}
	
	public void onDisable(){
		
	}
	
	public static boolean storageCartsStoreNearbyItems() {
		return MinecartManiaWorld.getConfigurationValue("Storage Carts Store Nearby Items") instanceof Boolean && 
		((Boolean)MinecartManiaWorld.getConfigurationValue("Storage Carts Store Nearby Items")).booleanValue();
	}
}
