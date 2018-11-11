package tld.sima.sleepmultiplier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

public class WorldData {
	private int numPlayers;
	private int numSleeping;
	private double multiplier;
	private double multiplierMax;
	private Set<UUID> players;
	private String subtitle1;
	private String subtitle2;
	
	public WorldData(int P, int S, Set<UUID> players, double multiplierMax) {
		this.players = players;
		numPlayers = P;
		numSleeping = S;
		this.multiplierMax = multiplierMax;
		recalculate();
	}
	
	public WorldData(int P, int S) {
		players = new HashSet<UUID>();
		numPlayers = P;
		numSleeping = S;
		recalculate();
	}
	
	public WorldData() {
		players = new HashSet<UUID>();
		numPlayers = 0;
		numSleeping = 0;
		multiplier = 0;
	}
	
	public void addToSet(UUID uuid) {
		players.add(uuid);
	}
	
	public void removeFromSet(UUID uuid) {
		players.remove(uuid);
	}
	
	public Set<UUID> getSet(){
		return players;
	}
	
	public int getSleepers() {
		return numSleeping;
	}
	
	public int getPlayers() {
		return numPlayers;
	}
	
	public String getSubtitle1() {
		return subtitle1;
	}
	
	public String getSubtitle2() {
		return subtitle2;
	}
	
	public void recalculate() {
		if (numPlayers == 0) {
			multiplier = 0;
		}else {
			multiplier = multiplierMax * ((double)numSleeping/(double)numPlayers);
		}
		subtitle1 = ChatColor.AQUA + "" + numSleeping + "/" + numPlayers;
		subtitle2 = ChatColor.GREEN + "(" + multiplier + "x speed)";
	}
	
	public void incP() {
		numPlayers++;
		recalculate();
	}
	
	public void decP() {
		numPlayers--;
		recalculate();
	}
	
	public void incS() {
		numSleeping++;
		recalculate();
	}
	
	public void decS() {
		numSleeping--;
		recalculate();
	}
	
	public void setPlayers(int num) {
		numPlayers = num;
	}
	
	public void setSleeping(int num) {
		numSleeping = num;
	}
	
	public double getMP() {
		return multiplier;
	}
}
