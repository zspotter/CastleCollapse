package me.zspotter.CastleCollapse.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.plugin.PluginManager;

/**
 * Inspired by King Arthur's Gold physics
 * Building materials are Stone and Wood
 *
 */
public class MedievalPhysicsOperator implements PhysicsOperator {
	
	public static final int MAX_STABILITY = 10;

	private static ArrayList<GravityMaterial> GRAVITY_MATERIALS = new ArrayList<GravityMaterial>(
			Arrays.asList(new GravityMaterial[] {
					new StoneMaterial(),
					new WoodMaterial()
			}));
	
	// Materials that support gravity-affected materials but are not gravity-affected themselves
	private static final HashSet<Material> SOLID_MATERIALS = new HashSet<Material>(
			Arrays.asList(new Material[] {
					Material.BEDROCK,
					Material.STONE,
					Material.DIRT,
					Material.GRASS
			}));
	
	private static final int CACHE_SIZE = 500;
	private Map<Location, Integer> stabilityCache;

	private CCPlugin plugin;	
	
	@Override
	public boolean doesGravityEffect(Material material) {
		for (GravityMaterial gm : GRAVITY_MATERIALS) {
			if (gm.containsMaterial(material)) return true;
		}
		return false;
	}
	
	@SuppressWarnings("serial")
	public MedievalPhysicsOperator(CCPlugin plug) {
		plugin = plug;
		
		stabilityCache = new LinkedHashMap<Location, Integer>(CACHE_SIZE+1, .75F, true) {
		    public boolean removeEldestEntry(Map.Entry<Location, Integer> eldest) {
		        if (size() > CACHE_SIZE) {
		        	plugin.getLogger().warning("Removing eldest entry from cache");
		        	return true;
		        }
		    	return false;
		    }
		};
	}
	
	@Override
	public void applyGravity(Block block) {
		//plugin.getLogger().info("Apply gravity to "+block);
		
		int cachedStability = (isStabilityCached(block))? stabilityOf(block) : -1;
		invalidateStability(block);
		int stability = stabilityOf(block);
		
		if (stability < 0 || stability > MAX_STABILITY) {
			throw new IllegalStateException("Got invalid stability "+stability+" for "+block);
		}
		
		if (stability == 0) {
			// Block will fall due to instability
			Material mat = block.getType();
			byte dat = block.getData();
			block.setType(Material.AIR); // Set block to air now, replace it with falling sand after the next if-statement
			// If block beneath isn't a gravity affected block and isn't a stable material, break it
			Block beneath = block.getRelative(BlockFace.DOWN);
			if (beneath != null && !beneath.isEmpty() && 
					!doesGravityEffect(beneath.getType()) && !SOLID_MATERIALS.contains(beneath.getType())) {
				beneath.breakNaturally();
			}
			// Make the block fall
			block.getWorld().spawnFallingBlock(block.getLocation(), mat, dat);

		} else {
			// Set new block type and data value
			for (GravityMaterial gm : GRAVITY_MATERIALS) {
				if (gm.containsMaterial(block.getType())) {
					gm.makeFrom(block, stability);
					break;
				}
			}
			if (cachedStability > 0 && cachedStability != stability) {
				// Block has not fallen, but stability has changed. 
				// Notify adjacent blocks
				PluginManager pm = plugin.getServer().getPluginManager();
				int id = block.getTypeId();
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.UP), id));
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.DOWN), id));
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.NORTH), id));
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.SOUTH), id));
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.EAST), id));
				pm.callEvent(new BlockPhysicsEvent(block.getRelative(BlockFace.WEST), id));
			}
		}
	}
	
	private int stabilityOf(Block block) {
		if (!doesGravityEffect(block.getType())) {
			if (!SOLID_MATERIALS.contains(block.getType())) {
				return -1;
			} else {
				return MAX_STABILITY + 1;
			}
		}

		int stability;
		if (!stabilityCache.containsKey(block.getLocation())
				|| stabilityCache.get(block.getLocation()) < 0) {
			// Put in temporary unstable value to prevent infinite recursion in stability calculation
			Location loc = block.getLocation().clone(); // Original location may mutate
			stabilityCache.put(loc, 0);
			// Calculate stability
			stability = calculateStability(block);
			stabilityCache.put(loc, stability);
			//plugin.getLogger().info("Cached "+block+" stability = "+stability);
		} else {
			stability = stabilityCache.get(block.getLocation());
			//plugin.getLogger().info("Recall "+block+" stability = "+ stability);
		}
		
		return stability;
	}
	
	private boolean isStabilityCached(Block block) {
		return stabilityCache.containsKey(block.getLocation());
	}
	
	private void invalidateStability(Block block) {
		if (stabilityCache.containsKey(block.getLocation())) {
			stabilityCache.put(block.getLocation(), -1);
		}
	}
	
	private int calculateStability(Block block) {	
		// This block will get the highest stability value of the following options
		// 	1. The same stability value as the block beneath
		//	2. The value of the most stable adjacent block minus this block's weight
		
		int weight = 0;
		for (GravityMaterial gm : GRAVITY_MATERIALS) {
			if (gm.containsMaterial(block.getType())) {
				weight = gm.weight();
				break;
			}
		}
		
		int stability = stabilityOf(block.getRelative(BlockFace.DOWN));
		
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.NORTH)) - weight);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.SOUTH)) - weight);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.EAST)) - weight);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.WEST)) - weight);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.UP)) - weight);
		
		// Make sure stability >= 0 and <= MAX_STABILITY
		stability = Math.max(stability, 0);
		stability = Math.min(stability, MAX_STABILITY);
		
		//plugin.getLogger().info("Stability of "+block+" = "+stability);
		
		return stability;
	}

}
