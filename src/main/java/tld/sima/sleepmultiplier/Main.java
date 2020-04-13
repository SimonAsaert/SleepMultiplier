package tld.sima.sleepmultiplier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import tld.sima.sleepmultiplier.commands.CommandManager;
import tld.sima.sleepmultiplier.events.EventsManager;
import tld.sima.sleepmultiplier.files.SettingsManager;
import tld.sima.sleepmultiplier.utils.Utils;
import tld.sima.sleepmultiplier.utils.WorldData;

public class Main extends JavaPlugin {
	public double multiplierMax;
	private HashSet<UUID> currentWorldList;
	public HashMap<UUID, WorldData> worldTimeSkip;

	@Override
	public void onEnable() {
		CommandManager cmgr = new CommandManager();
		this.getCommand(cmgr.cmd1).setExecutor(cmgr);
		this.getCommand(cmgr.cmd2).setExecutor(cmgr);
		this.getCommand(cmgr.cmd3).setExecutor(cmgr);
		this.getCommand(cmgr.cmd4).setExecutor(cmgr);

		worldTimeSkip = new HashMap<UUID, WorldData>();

		SettingsManager smgr = new SettingsManager();
		smgr.setup();
		currentWorldList = smgr.getWorldList();
		multiplierMax = smgr.getMaxMultiplier();

		for (World world : Bukkit.getWorlds()) {
			if(currentWorldList.contains(world.getUID())){
				addWorld(world.getUID());
			}
		}

		BukkitRunnable run1 = new BukkitRunnable() {
			public void run() {
				for (UUID uuid : worldTimeSkip.keySet()) {
					World world = Bukkit.getWorld(uuid);
					world.setFullTime(world.getFullTime() + (long)(worldTimeSkip.get(world.getUID()).getMP()));
				}
			}
		};
		run1.runTaskTimer(this, 20, 1);

		BukkitRunnable run2 = new BukkitRunnable() {
			public void run() {
				for (UUID worlduuid : worldTimeSkip.keySet()) {
					World world = Bukkit.getWorld(worlduuid);

					Set<UUID> sleepers = worldTimeSkip.get(world.getUID()).getSet();
					if (!sleepers.isEmpty()) {
						int hours = (int)(world.getTime()/1000);
						int minutes = (int) ((world.getTime() - hours*1000) * (60.0/1000.0));
						hours = hours + 6;
						if (hours >= 24) {
							hours -=24;
						}
						String title = hours + ":" + minutes;

						for (UUID uuid : sleepers) {
							Utils.sendTitle(uuid, title, worldTimeSkip.get(world.getUID()).getSubtitle1(), worldTimeSkip.get(world.getUID()).getSubtitle2());
						}
					}
				}
			}
		};

		run2.runTaskTimerAsynchronously(this, 20, 4);
		
		getServer().getPluginManager().registerEvents(new EventsManager(), this);	
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Sleep Multipler enabled");
	}

	// Events
	@Override
	public void onDisable() {
		SettingsManager smgr = new SettingsManager();
		smgr.setup();
		smgr.saveAll(worldTimeSkip.keySet());
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Sleep Multipler disabled");
	}

	@EventHandler
	public void onLoad(WorldLoadEvent event) {
		if(currentWorldList.contains(event.getWorld().getUID())) {
			addWorld(event.getWorld().getUID());
		}
	}
	
	public boolean addWorld(UUID worldUUID) {
		if(worldTimeSkip.containsKey(worldUUID)) {
			return false;
		}

		Set<UUID> sleepers = new HashSet<UUID>();
		List<Player> players = Bukkit.getWorld(worldUUID).getPlayers();
		int numSleeping = 0;
		int numPeople = 0;
		for(Player player : players) { 
			if (!player.isSleepingIgnored()) {
				numPeople++;
			}else if(player.isSleeping()) {
				numSleeping++;
				numPeople++;
				sleepers.add(player.getUniqueId());
			}
		}
		WorldData wd = new WorldData(numPeople, numSleeping, sleepers, multiplierMax);
		worldTimeSkip.put(worldUUID, wd);
		return true;
	}
	
	public WorldData getWorldData(UUID uuid) {
		return worldTimeSkip.get(uuid);
	}

	public Set<UUID> getWorlds(){
		return worldTimeSkip.keySet();
	}
}
