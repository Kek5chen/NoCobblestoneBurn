package me.kx.skyblockadditions;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public final class SkyBlockAdditions extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	public boolean isGenItem(Item item) {
		boolean foundLava = false, foundWater = false;
		for (int xRel = -1; xRel <= 1; xRel++) {
			for (int zRel = -1; zRel <= 1; zRel++) {
				if (item.getLocation().getBlock().getRelative(xRel, 0, zRel).getType() == Material.LAVA) {
					foundLava = true;
				} else if (item.getLocation().getBlock().getRelative(xRel, 0, zRel).getType() == Material.WATER) {
					foundWater = true;
				}
				if (foundLava && foundWater) {
					return true;
				}
			}
		}
		return false;
	}

	ArrayList<Item> genCobblestone = new ArrayList<>();

	@EventHandler
	public void onGenCobbleSpawn(ItemSpawnEvent e) {
		if (e.getEntity().getItemStack().getType() == Material.COBBLESTONE && isGenItem(e.getEntity())) {
			genCobblestone.add(e.getEntity());
			e.getEntity().setGravity(false);
			e.getEntity().setVelocity(new Vector(0, 0.03, 0));
			e.getEntity().getLocation().add(0, 2, 0);
		}
	}

	@EventHandler
	public void onGenCobbleBurn(ItemMergeEvent e) {
		if (genCobblestone.contains(e.getEntity()) && isGenItem(e.getTarget())) {
			genCobblestone.remove(e.getEntity());
		}
	}

	@EventHandler
	public void onGenCobblePickup(EntityPickupItemEvent e) {
		if (genCobblestone.contains(e.getItem()) && isGenItem(e.getItem())) {
			genCobblestone.remove(e.getItem());
		}
	}

	@EventHandler
	public void onEntityBurn(EntityCombustByBlockEvent e) {
		if (!(e.getEntity() instanceof Item)) {
			return;
		}

		Item item = (Item) e.getEntity();
		if (!genCobblestone.contains(item)) {
			return;
		}
		Block combuster = e.getCombuster();
		if (combuster == null || combuster.getType() != Material.LAVA) {
			return;
		}

		ItemStack itemStack = item.getItemStack();
		if (itemStack.getType() != Material.COBBLESTONE || itemStack.getType() != Material.STONE) {
			return;
		}
		e.setCancelled(true);
	}

	@EventHandler
	public void onComposterUse(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		Block b = e.getClickedBlock();
		if (b == null || b.getType() != Material.COMPOSTER) {
			return;
		}

		Levelled levelled = (Levelled) b.getBlockData();
		if (levelled.getLevel() != levelled.getMaximumLevel()) {
			return;
		}

		b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.DIRT));
	}
}
