package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class CCEventListener implements Listener {
	
	private CCPlugin plug;
	private PhysicsOperator phys;
	
	public CCEventListener(CCPlugin plugin) {
		plug = plugin;
		phys = plug.getPhysicsOperator();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.canBuild() && phys.doesGravityEffect(event.getBlock().getType())) {
			phys.applyGravity(event.getBlock());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockUpdate(BlockPhysicsEvent event) {
		//plug.getLogger().info("onBlockPhysics for "+event.getBlock());
		if (phys.doesGravityEffect(event.getBlock().getType())) {
			phys.applyGravity(event.getBlock());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityChangeBlock(EntityChangeBlockEvent event) {
		//plug.getLogger().info("onEntityChangeBlock for "+event.getBlock());
		if (phys.doesGravityEffect(event.getTo())) {
			// FallingBlock is landing
			event.setCancelled(true);
			phys.makeGravityBlock(event.getBlock());
		}
	}

}
