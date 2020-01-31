package tld.sima.sleepmultiplier;

import java.util.HashSet;
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
	String cmd4 = "WorldStats";
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase(cmd1)) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					UUID worldUUID = player.getWorld().getUID();
					if(plugin.addWorld(worldUUID)) {
						player.sendMessage(ChatColor.GOLD + "Added world " + ChatColor.WHITE + player.getWorld().getName());
					}else {
						player.sendMessage(ChatColor.RED + "World already loaded!");
					}
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
		}else if (cmd.getName().equalsIgnoreCase(cmd4)) {
			UUID worldUUID;
			if(args.length > 0) {
				World world = Bukkit.getWorld(args[0]);
				if(world.equals(null)) {
					sender.sendMessage(ChatColor.RED + "Unable to find World!");
					return true;
				}
				worldUUID = world.getUID();
			}else {
				if(sender instanceof Player) {
					worldUUID = ((Player) sender).getWorld().getUID();
				}else {
					sender.sendMessage(ChatColor.RED + "You must give a world name!");
					return true;
				}
			}
			WorldData data = plugin.worldTimeSkip.get(worldUUID);
			if(data == null) {
				sender.sendMessage(ChatColor.RED + "World is not being scanned");
				return true;
			}
			sender.sendMessage(ChatColor.GRAY + "Number of Players being Scanned: " + ChatColor.WHITE + data.getPlayers());
			sender.sendMessage(ChatColor.GRAY + "Number of Players sleeping: " + ChatColor.WHITE + data.getSleepers());
			sender.sendMessage(ChatColor.GRAY + "Get Multiplayer: " + ChatColor.WHITE + data.getMP());
			StringBuilder names = new StringBuilder();
			names.append(ChatColor.GRAY).append("Player Names: ").append(ChatColor.WHITE);
			for(UUID uuid : data.getSet()) {
				names.append(Bukkit.getPlayer(uuid).getName()).append(" ");
			}
			sender.sendMessage(names.toString());
		}
		return true;
	}
	
	private void recalculate(World world) {
		HashSet<UUID> sleepers = new HashSet<UUID>();
		int numPlayers = 0;
		for(Player player : world.getPlayers()) {
			if(player.isSleeping() && !player.isSleepingIgnored()) {
				numPlayers++;
				sleepers.add(player.getUniqueId());
			}else if (!player.isSleepingIgnored()) {
				numPlayers++;
			}
		}

		plugin.worldTimeSkip.get(world.getUID()).setSet(sleepers);
		plugin.worldTimeSkip.get(world.getUID()).setPlayers(numPlayers);
		plugin.worldTimeSkip.get(world.getUID()).recalculate();
	}
}
