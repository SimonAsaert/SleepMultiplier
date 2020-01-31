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
	private SettingsManager smgr;

	@Override
	public void onEnable() {
		CommandManager cmgr = new CommandManager();
		this.getCommand(cmgr.cmd1).setExecutor(cmgr);
		this.getCommand(cmgr.cmd2).setExecutor(cmgr);
		this.getCommand(cmgr.cmd3).setExecutor(cmgr);
		this.getCommand(cmgr.cmd4).setExecutor(cmgr);

		worldTimeSkip = new HashMap<UUID, WorldData>();

		smgr = new SettingsManager();
		smgr.setup();
		Set<UUID> worlds = smgr.getWorldList();
		multiplierMax = smgr.getMaxMultiplier();

		for (World world : Bukkit.getWorlds()) {
			if(worlds.contains(world.getUID())){
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
							sendTitle(uuid, title, worldTimeSkip.get(world.getUID()).getSubtitle1(), worldTimeSkip.get(world.getUID()).getSubtitle2());
						}
					}
				}
			}
		};

		run2.runTaskTimerAsynchronously(this, 20, 4);

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
		smgr.saveAll(worldTimeSkip.keySet());
		Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Sleep Multipler disabled");
	}

	@EventHandler
	public void onLoad(WorldLoadEvent event) {
		if(smgr.getWorldList().contains(event.getWorld().getUID())) {
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

	public Set<UUID> getWorlds(){
		return worldTimeSkip.keySet();
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		final UUID worldUID = player.getWorld().getUID();
		if(worldTimeSkip.containsKey(worldUID) && !event.getPlayer().isSleepingIgnored()){
			BukkitRunnable run = new BukkitRunnable() {
				public void run() {
					worldTimeSkip.get(worldUID).incP();
				}
			};
			run.runTaskLaterAsynchronously(this, 5);
		}
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		final UUID worldUID = player.getWorld().getUID();
		if(worldTimeSkip.containsKey(worldUID) && !event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).decP();
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		final UUID worldUID = player.getWorld().getUID();
		if(worldTimeSkip.containsKey(worldUID) && !event.getPlayer().isSleepingIgnored()){
			worldTimeSkip.get(event.getFrom().getUID()).decP();
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).incP();
		}
	}
	
	@EventHandler
	public void onBedLeave(PlayerBedLeaveEvent event) {
		Player player = event.getPlayer();
		final UUID worldUID = player.getWorld().getUID();
		if(worldTimeSkip.containsKey(worldUID) && !event.getPlayer().isSleepingIgnored()){
//			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).decS();
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).removeFromSet(event.getPlayer().getUniqueId());
			worldTimeSkip.get(event.getPlayer().getWorld().getUID()).recalculate();
		}
	}
	
	@EventHandler
	public void onBedJoin(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
		final UUID worldUID = player.getWorld().getUID();
		if(worldTimeSkip.containsKey(worldUID) && !event.getPlayer().isSleepingIgnored() && !event.isCancelled()){
			World world = event.getPlayer().getWorld();
			
//			worldTimeSkip.get(world.getUID()).setSleeping(numSleeping);
			worldTimeSkip.get(world.getUID()).addToSet(event.getPlayer().getUniqueId());
			worldTimeSkip.get(world.getUID()).recalculate();
		}
	}
}
