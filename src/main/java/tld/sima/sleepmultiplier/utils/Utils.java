package tld.sima.sleepmultiplier.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
	
	// Send display packet to player 
	public static void sendTitle(UUID uuid, String title, String subtitle1, String subtitle2) {
		Player player = Bukkit.getPlayer(uuid);
		assert player != null;
		player.sendTitle(ChatColor.DARK_AQUA + title, ChatColor.AQUA + subtitle1 + ChatColor.GREEN + " " + subtitle2, 1, 4, 1);
	}
}
