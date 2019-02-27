package tld.sima.sleepmultiplier;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandManager implements CommandExecutor{
	Main plugin = Main.getPlugin(Main.class);

	String cmd1 = "AddWorld";
	String cmd2 = "RemoveWorld";
	String cmd3 = "ResetWorld";
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase(cmd1)) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					UUID worldUUID = player.getWorld().getUID();
					plugin.getWorlds().add(worldUUID);
					player.sendMessage(ChatColor.GOLD + "Added world " + ChatColor.WHITE + player.getWorld().getName());
					return true;
				}else {
					Bukkit.getServer().getConsoleSender().sendMessage("Adds world that is effected by sleep multiplier");
					return false;
				}
			}else if (args.length == 1) {
				String name = args[0];
				World world = Bukkit.getWorld(name);
				if (world == null) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						player.sendMessage(ChatColor.RED + "Unable to find world with that name!");
					}else {
						Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "unable to find world with that name!");
					}
					return false;
				}
				UUID worldUUID = world.getUID();
				plugin.getWorlds().add(worldUUID);
				if(sender instanceof Player) {
					Player player = (Player) sender;
					player.sendMessage(ChatColor.GOLD + "Added world " + ChatColor.WHITE + world.getName());
				}else {
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "Added world " + ChatColor.WHITE + world.getName());
				}
				return true;
			}else {
				sender.sendMessage("Adds world that is effected by sleep multiplier");
				return false;
			}
		}else if (cmd.getName().equalsIgnoreCase(cmd2)) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					UUID worldUUID = player.getWorld().getUID();
					plugin.getWorlds().remove(worldUUID);
					player.sendMessage(ChatColor.GOLD + "Removed world " + ChatColor.WHITE + player.getWorld().getName());
					return true;
				}else {
					Bukkit.getServer().getConsoleSender().sendMessage("Removes world that is effected by sleep multiplier");
					return false;
				}
			}else if (args.length == 1) {
				String name = args[0];
				World world = Bukkit.getWorld(name);
				if (world == null) {
					if(sender instanceof Player) {
						Player player = (Player) sender;
						player.sendMessage(ChatColor.RED + "Unable to find world with that name!");
					}else {
						Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "unable to find world with that name!");
					}
					return false;
				}
				UUID worldUUID = world.getUID();
				plugin.getWorlds().remove(worldUUID);
				if(sender instanceof Player) {
					Player player = (Player) sender;
					player.sendMessage(ChatColor.GOLD + "Removed world " + ChatColor.WHITE + world.getName());
				}else {
					Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "Removed world " + ChatColor.WHITE + world.getName());
				}
				return true;
			}else {
				sender.sendMessage("Removes world that is effected by sleep multiplier");
				return false;
			}
		}else if (cmd.getName().equalsIgnoreCase(cmd3)) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					World world = player.getWorld();
					if(plugin.getWorlds().contains(world.getUID())) {
						recalculate(world);
						player.sendMessage(ChatColor.GREEN + "Recalculated time.");
						return true;
					}else {
						player.sendMessage(ChatColor.RED + "World is not registered with this plugin. Use " + ChatColor.WHITE + "/" + cmd1 + ChatColor.RED + " to add world to list");
						return true;
					}
				}else {
					World world = Bukkit.getWorld(args[0]);
					if (world == null) {
						player.sendMessage(ChatColor.RED + "World not found!");
						return true;
					}else if (plugin.getWorlds().contains(world.getUID())) {
						recalculate(world);
						player.sendMessage(ChatColor.GREEN + "Recalculated time.");
						return true;
					}else {
						player.sendMessage(ChatColor.RED + "World is not registered with this plugin. Use " + ChatColor.WHITE + "/" + cmd1 + ChatColor.RED + " to add world to list");
						return true;
					}
				}
			}else {
				if(args.length == 0) {
					plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "You must be a player to use this command without a referred world!");
					return true;
				}else {
					World world = Bukkit.getWorld(args[0]);
					if (world == null) {
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "World not found!");
						return true;
					}else if (plugin.getWorlds().contains(world.getUID())) {
						recalculate(world);
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Recalculated time.");
						return true;
					}else {
						plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + "World is not registered with this plugin. Use " + ChatColor.WHITE + "/" + cmd1 + ChatColor.RED + " to add world to list");
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void recalculate(World world) {
		int numSleeping = 0;
		for(Player player : world.getPlayers()) {
			if(player.isSleeping()) {
				numSleeping++;
			}
		}
		
		plugin.worldTimeSkip.get(world.getUID()).setSleeping(numSleeping);
		plugin.worldTimeSkip.get(world.getUID()).recalculate();
	}
}
