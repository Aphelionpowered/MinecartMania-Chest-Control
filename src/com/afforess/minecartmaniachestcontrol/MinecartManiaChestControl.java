package com.afforess.minecartmaniachestcontrol;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.afforess.minecartmaniacore.MinecartManiaCore;
import com.afforess.minecartmaniacore.config.MinecartManiaConfigurationParser;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;

public class MinecartManiaChestControl extends JavaPlugin {
    public static final MinecartManiaLogger log = MinecartManiaLogger.getInstance();
    public static MinecartManiaChestControl instance;
    public static Server server;
    public static PluginDescriptionFile description;
    public static final MinecartManiaActionListener listener = new MinecartManiaActionListener();
    
    public void onEnable() {
        MinecartManiaChestControl.performStartup(this);
    }
    
    public MinecartManiaChestControl getInstance() {
        return instance;
    }
    
    private static void performStartup(final MinecartManiaChestControl instance) {
        setInstance(instance);
        description = instance.getDescription();
        server = Bukkit.getServer();
        MinecartManiaConfigurationParser.read(description.getName() + "Configuration.xml", MinecartManiaCore.getDataDirectoryRelativePath(), new ChestControlSettingParser());
        Bukkit.getServer().getPluginManager().registerEvents(listener, instance);
        //        Bukkit.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, listener, Priority.High, instance);
        log.info(description.getName() + " version " + description.getVersion() + " is enabled!");
        
        RecipeManager.init();
    }
    
    private static void setInstance(final MinecartManiaChestControl newInstance) {
        instance = newInstance;
    }
    
    public void onDisable() {
        
    }
}
