package tld.sima.sleepmultiplier;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

public class WorldData {
	private int numPlayers;
//	private int numSleeping;
	private double multiplier;
	private double multiplierMax;
	private Set<UUID> sleepers;
	private String subtitle1;
	private String subtitle2;
	
	public WorldData(int P, int S, Set<UUID> players, double multiplierMax) {
		this.sleepers = players;
		numPlayers = P;
//		numSleeping = S;
		this.multiplierMax = multiplierMax;
		recalculate();
	}
	
	public WorldData(int P, int S) {
		sleepers = new HashSet<UUID>();
		numPlayers = P;
//		numSleeping = S;
		recalculate();
	}
	
	public WorldData() {
		sleepers = new HashSet<UUID>();
		numPlayers = 0;
//		numSleeping = 0;
		multiplier = 0;
	}
	
	public void addToSet(UUID uuid) {
		sleepers.add(uuid);
	}
	
	public void removeFromSet(UUID uuid) {
		sleepers.remove(uuid);
	}
	
	public Set<UUID> getSet(){
		return sleepers;
	}
	
	public int getSleepers() {
		return sleepers.size();
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
		if (numPlayers == 0 || sleepers.size() == 0) {
			multiplier = 0;
		}else {
			multiplier = multiplierMax * ((double)sleepers.size()/(double)numPlayers);
		}
		subtitle1 = ChatColor.AQUA + "" + sleepers.size() + "/" + numPlayers;
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
	
	public void setSet(Set<UUID> sleepers) {
		this.sleepers = sleepers;
	}
	
//	public void incS() {
//		numSleeping++;
//		recalculate();
//	}
//	
//	public void decS() {
//		numSleeping--;
//		recalculate();
//	}
	
	public void setPlayers(int num) {
		numPlayers = num;
	}
	
//	public void setSleeping(int num) {
//		numSleeping = num;
//	}
	
	public double getMP() {
		return multiplier;
	}
}
