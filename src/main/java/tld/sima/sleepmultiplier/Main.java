package tld.sima.sleepmultiplier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener{
	public double multiplierMax;
	public HashMap<UUID, WorldData> worldTimeSkip;
	public HashSet<UUID> worlds;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		CommandManager cmgr = new CommandManager();
		this.getCommand(cmgr.cmd1).setExecutor(cmgr);
		this.getCommand(cmgr.cmd2).setExecutor(cmgr);
		
		worldTimeSkip = new HashMap<UUID, WorldData>();
		
		SettingsManager smgr = new SettingsManager();
		smgr.setup();
		worlds = smgr.getWorldList();
		multiplierMax = smgr.getMaxMultiplier();
		
		for (World world : Bukkit.getServer().getWorlds()) {
			Set<UUID> sleepers = new HashSet<UUID>();
			int numPeople = world.getPlayers().size();
			int numSleeping = 0;
			for (Player player : world.getPlayers()) {
				if (player.isSleeping()) {
					numSleeping++;
					sleepers.add(player.getUniqueId());
				}
			}
			WorldData wd = new WorldData(numPeople, numSleeping, sleepers, multiplierMax);
			worldTimeSkip.put(world.getUID(), wd);
		}

		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new BukkitRunnable() {
			public void run() {
				for (UUID uuid : worlds) {
					World world = Bukkit.getWorld(uuid);
					world.setFullTime(world.getFullTime() + (long)(worldTimeSkip.get(world.getUID()).getMP()));
				}
			}
		}, 20, 1);

		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new BukkitRunnable() {
			public void run() {
				for (UUID worlduuid : worlds) {
					World world = Bukkit.getWorld(worlduuid);
					Set<UUID> sleepers = worldTimeSkip.get(world.getUID()).getSet();
					if (!sleepers.isEmpty()) {
						int hours = (int)(world.getTime()/1000);
						int minutes = (int) ((world.getTime() - hours*1000) * (60.0/1000.0));
						hours = hours + 6;
						if (hours > 24) {
							hours -=24;
						}
						String title = hours + ":" + minutes;
						
						for (UUID uuid : sleepers) {
							sendTitle(uuid, title, worldTimeSkip.get(world.getUID()).getSubtitle1(), worldTimeSkip.get(world.getUID()).getSubtitle2());
						}
					}
				}
			}
		}, 20, 2);
		

		getServer().getPluginManager().registerEvents(this, this);	
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "Sleep Multipler enabled");
	}
	// Send display packet to player
	private void sendTitle(UUID uuid, String title, String subtitle1, String subtitle2) {
		Player player = Bukkit.getPlayer(uuid);
		player.sendTitle(ChatColor.DARK_AQUA + title, ChatColor.AQUA + subtitle1 + ChatColor.GREEN + " " + subtitle2, 1, 4, 1);
	}
	
	// Events
	@Override
	public void onDisable() {
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Sleep Multipler disabled");
	}

	@EventHandler
	public void onLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		Set<UUID> sleepers = new HashSet<UUID>();
		int numPeople = world.getPlayers().size();
		int numSleeping = 0;
		for (Player player : world.getPlayers()) {
			if (player.isSleeping()) {
				numSleeping++;
				sleepers.add(player.getUniqueId());
			}
		}
		WorldData wd = new WorldData(numPeople, numSleeping, sleepers, multiplierMax);
		worldTimeSkip.put(world.getUID(), wd);
	}

	public HashSet<UUID> getWorlds(){
		return worlds;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		if(!event.getPlayer().isSleepingIgnored()){
			this.getServer().getScheduler().runTaskLater(this, new BukkitRunnable() {
	
				public void run() {
					worldTimeSkip.get(player.getWorld().getUID()).incP();
				}
			}, 5);
		}
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		if(!event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).decP();
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		if(!event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getFrom().getUID()).decP();
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).incP();
		}
	}
	
	@EventHandler
	public void onBedLeave(PlayerBedLeaveEvent event) {
		if(!event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).decS();
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).removeFromSet(event.getPlayer().getUniqueId());
		}
	}
	
	@EventHandler
	public void onBedJoin(PlayerBedEnterEvent event) {
		if(!event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).incS();
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).addToSet(event.getPlayer().getUniqueId());
		}
	}
}
