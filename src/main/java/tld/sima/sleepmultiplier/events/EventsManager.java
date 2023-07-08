package tld.sima.sleepmultiplier.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import tld.sima.sleepmultiplier.Main;
import tld.sima.sleepmultiplier.utils.WorldData;

public class EventsManager implements Listener{
	
	Main plugin = Main.getPlugin(Main.class);
	
	// If console issues /op [player]
	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event) {
		String command = event.getCommand();
		String[] tokens = command.split("[ ]");
		
		if(tokens.length > 0 && tokens[0].equalsIgnoreCase("op") ) {
			parseCommand(tokens[1], true);
		}else if (tokens.length > 0 && tokens[0].equalsIgnoreCase("deop")) {
			parseCommand(tokens[1], false);
		}
	}
	
	@EventHandler 
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(!event.isCancelled()) {
			Player p = event.getPlayer();
			String command = event.getMessage();
			String[] tokens = command.split("[ ]");
			if(tokens.length > 0 && tokens[0].equalsIgnoreCase("/op") && p.hasPermission("minecraft.command.op")) {
				parseCommand(tokens[1], true);
			}else if (tokens.length > 0 && tokens[0].equalsIgnoreCase("/deop") && p.hasPermission("minecraft.command.deop")) {
				parseCommand(tokens[1], false);
			}
		}
	}
	
	private void parseCommand(String token, boolean toOp) {
		Player p = Bukkit.getPlayer(token);
		
		if(p != null) {
			WorldData data = plugin.getWorldData(p.getWorld().getUID());
			if(data != null) {
				if(toOp && !p.isOp()) {
					data.decP();
					if(p.isSleeping())	data.removeFromSet(p.getUniqueId());
				}else if (!toOp && p.isOp()) {
					data.incP();
					if(p.isSleeping())	data.addToSet(p.getUniqueId());
				}
			}
		}
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		final Player player = event.getPlayer();
		BukkitRunnable run = new BukkitRunnable() {
			public void run() {
				final WorldData data = plugin.getWorldData(player.getWorld().getUID());
				
				if(data != null && !player.isSleepingIgnored()){
					data.incP();
				}
			}
		};
		
		run.runTaskLater(plugin, 5);
	}
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		final WorldData data = plugin.getWorldData(player.getWorld().getUID());
		
		
		if(data != null && !player.isSleepingIgnored()){
			data.decP();
		}
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		
		if(!player.isSleepingIgnored()) {
			WorldData fromWorld = plugin.getWorldData(event.getFrom().getUID());
			if(fromWorld != null)	fromWorld.decP();
			WorldData toWorld = plugin.getWorldData(player.getWorld().getUID());
			if(toWorld != null)		toWorld.incP();
		}
	}
	
	@EventHandler
	public void onBedLeave(PlayerBedLeaveEvent event) {
		Player player = event.getPlayer();
		WorldData data = plugin.getWorldData(player.getWorld().getUID());
		
		if(data != null && !player.isSleepingIgnored()){
			data.removeFromSet(event.getPlayer().getUniqueId());
			data.recalculate();
		}
	}
	
	@EventHandler
	public void onBedJoin(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
		WorldData data = plugin.getWorldData(player.getWorld().getUID());
		
		if(data != null && !player.isSleepingIgnored() && !event.isCancelled()){
			
			data.addToSet(event.getPlayer().getUniqueId());
			data.recalculate();
		}
	}
}
