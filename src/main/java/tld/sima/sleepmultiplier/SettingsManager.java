package tld.sima.sleepmultiplier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class SettingsManager {
	private Main plugin = Main.getPlugin(Main.class);
	
	private FileConfiguration storagecfg;
	private File storagefile;
	
	public void setup() {
		// Create plugin folder if doesn't exist.
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdir();
		}

		// Create main file
		String FileLocation = plugin.getDataFolder().toString() + File.separator + "Storage" + ".yml";
		File tmp = new File(FileLocation);
		storagefile = tmp;
		// Check if file exists
		if (!storagefile.exists()) {
			try {
				storagefile.createNewFile();
			}catch(IOException e) {
				e.printStackTrace();
				Bukkit.getServer().getConsoleSender().sendMessage(net.md_5.bungee.api.ChatColor.RED + "Storage file unable to be created!");
			}
		}
		storagecfg = YamlConfiguration.loadConfiguration(storagefile);
		createStorageValues();
	}
	
	private void createStorageValues() {
		storagecfg.addDefault("Worlds.AffectedWorlds", new ArrayList<String>());
		storagecfg.addDefault("Worlds.Settings.MultiplierMax", 20);
		storagecfg.options().copyDefaults(true);
		savecfg();
	}

	private boolean savecfg() {
		try {
			storagecfg.save(storagefile);
		} catch (IOException e) {
			Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Unable to save storage file!" );
			return false;
		}
		return true;
	}
	
	public int getMaxMultiplier() {
		return storagecfg.getInt("Worlds.Settings.MultiplierMax");
	}
	
	public HashSet<UUID> getWorldList(){
		List<String> list = storagecfg.getStringList("Worlds.AffectedWorlds");
		HashSet<UUID> uuids = new HashSet<UUID>();
		for (String name : list) {
			UUID uuid = UUID.fromString(name);
			if (Bukkit.getWorld(uuid) == null) {
				continue;
			}
			uuids.add(uuid);
		}
		return uuids;
	}
	
	public void saveAll(Set<UUID> uuids) {
		saveWorlds(uuids);
		savecfg();
	}
	
	private void saveWorlds(Set<UUID> uuids) {
		ArrayList<String> list = new ArrayList<String>();
		for (UUID uuid : uuids) {
			list.add(uuid.toString());
		}
		storagecfg.set("Worlds.AffectedWorlds", list);
	}
}
