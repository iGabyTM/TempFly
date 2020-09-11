package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.IslandManager;
import com.iridium.iridiumskyblock.User;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

/**
 * - Dev Notes -
 * IridiumSkyblock contains no events or api for island levels and challenges, therefore unless they are implemented
 * it is unreasonably difficult for me to performantly update flight requirements in real time when challenges or island
 * levels update. The player will need to leave and re-enter the island to update them.
 * 
 * IridiumSkyblock also does not include built in island tracking or events for player location and islands
 * IridiumHook will initiate the super tracker in SkyblockHook.
 * 
 * @author Kevin
 *
 */
public class IridiumHook extends SkyblockHook implements Listener {

	public static final String[] ROLES = new String[] {"OWNER", "COOWNER", "MODERATOR", "MEMBER", "COOP", "VISITOR"};
	
	public IridiumHook(TempFly tempfly) {
		super(tempfly);
	}
	
	@Override
	public boolean initializeHook() {
		// initial iridium hook stuff.
		
		//
		if (super.initializeHook()) {
			getTempFly().getServer().getPluginManager().registerEvents(this, getTempFly());
			startManualTracking();
			setEnabled(true);
			return true;
		}
		return false;
	}
	
	
	@Override
	public String getEmbeddedConfigName() {
		return "skyblock_preset_iridium";
	}
	
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(InventoryOpenEvent e) {
		if (!hasSettingsHook() || !(e.getPlayer() instanceof Player)) {
			return;
		}
		Console.debug(U.strip(IridiumSkyblock.getInventories().islandMenuGUITitle));
		Console.debug(U.strip(e.getView().getTitle()));
		if (!U.strip(e.getView().getTitle()).equals(U.strip(IridiumSkyblock.getInventories().islandMenuGUITitle))) {
			return;
		}
		Inventory inv = e.getInventory();
		inv.setItem(inv.getSize()-5, getSettingsButton());
	}
	
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = false)
	public void on(InventoryClickEvent e) {
		if (!hasSettingsHook() || !(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		if (!U.strip(e.getView().getTitle()).equals(U.strip(IridiumSkyblock.getInventories().islandMenuGUITitle))) {
			Console.debug("not inv");
			return;
		}
		ItemStack clicked = e.getCurrentItem();
		if (getSettingsButton().isSimilar(clicked)) {
			Console.debug("open inv");
			e.setCancelled(true);
			openIslandSettings(p);
		}
	}
	
	
	
	

	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		List<Player> online = new ArrayList<>();
		Island rawIsland = (Island) island.getRawIsland();
		for (String s: rawIsland.getMembers()) {
			Player p = Bukkit.getPlayer(UUID.fromString(s));
			if (p != null & p.isOnline()) {
				online.add(p);
			}
		}
		return online.toArray(new Player[online.size()]);
	}

	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		List<UUID> ids = new ArrayList<>();
		Island rawIsland = (Island) island.getRawIsland();
		for (String s: rawIsland.getMembers()) {
			ids.add(UUID.fromString(s));
		}
		return ids.toArray(new UUID[ids.size()]);
	}

	@Override
	public IslandWrapper getIslandOwnedBy(UUID u) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(u);
		User user = User.getUser(p);
		Island rawIsland = user.getIsland();
		if (rawIsland == null || !rawIsland.getOwner().equals(u.toString())) {
			return null;
		}
		return getIslandWrapper(rawIsland);
	}

	@Override
	public IslandWrapper getTeamIsland(UUID u) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(u);
		User user = User.getUser(p);
		Island rawIsland = user.getIsland();
		if (rawIsland == null) {
			return null;
		}
		return getIslandWrapper(rawIsland);
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		IslandManager islandManager;
		return getIslandWrapper((islandManager = IridiumSkyblock.getIslandManager()) == null ? null : islandManager.getIslandViaLocation(loc));
	}

	@Override
	public boolean isChallengeCompleted(UUID u, SkyblockChallenge challenge) {
		Console.debug("--|?> Is challenge completed: ");
		Island island = (Island) getTeamIsland(u).getRawIsland();
		if (island == null) {
			return false;
		}
		if (!island.getMissionLevels().containsKey(challenge.getName())) {
			Console.debug("--|!> island does not contain mission: " + challenge.getName());
			return false;
		}
		if (island.getMission(challenge.getName()) >= challenge.getRequiredProgress()) {
			Console.debug("--|> island meets challenge condition: " + challenge.getName());
			return true;
		}
		if (V.debug) {Console.debug("--|> island does not meet challenge condition: " + challenge.getName(), "--| current progress: " + island.getMission(challenge.getName()));	}
		return false;
	}

	@Override
	public boolean islandRoleExists(IslandWrapper island, String role) {
		return islandRoleExists(role);
	}

	@Override
	public boolean islandRoleExists(String role) {
		for (String s: ROLES) {
			if (s.equalsIgnoreCase(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getIslandRole(UUID u, IslandWrapper island) {
		Island rawIsland = (Island) island.getRawIsland();
		IslandWrapper playerIsland = getTeamIsland(u);
		if (playerIsland != null) {
			if (rawIsland.getCoop().contains(((Island)rawIsland).getId())) {
				return "COOP";
			}
		}
		
		if (isIslandMember(u, island)) {
			return User.getUser(Bukkit.getOfflinePlayer(u)).getRole().toString().toUpperCase();
		} else {
			return "VISITOR";
		}
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		return UUID.fromString(((Island)island.getRawIsland()).getOwner());
	}

	/**
	 * Each island in iridium skyblock has an int ID. this will be our identifier.
	 */
	@Override
	public String getIslandIdentifier(Object rawIsland) {
		if (rawIsland instanceof IslandWrapper) {
			rawIsland = ((IslandWrapper) rawIsland).getRawIsland();
		}
		return String.valueOf(((Island)rawIsland).getId());
	}
	
	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		return getIslandWrapper(IridiumSkyblock.getIslandManager().getIslandViaId(Integer.valueOf(identifier)));
	}
	

	@Override
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		return ((Island)island.getRawIsland()).getMembers().contains(u.toString());
	}

	@Override
	public double getIslandLevel(UUID u) {
		IslandWrapper island = getTeamIsland(u);
		return island == null ? 0 : ((Island)island.getRawIsland()).getValue();
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		return ((Island)island.getRawIsland()).getValue();
	}

	@Override
	public String[] getRoles() {
		return ROLES;
	}

	@Override
	public String getPluginName() {
		return "IridiumSkyblock";
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		// Try catches a benign error when using /reload because iridium isnt ready yet.
		try {return IridiumSkyblock.getIslandManager().isIslandWorld(loc);} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public boolean isInIsland(IslandWrapper island, Location loc) {
		
		return ((Island)island.getRawIsland()).isInIsland(loc) && isIslandWorld(loc);
	}

}
