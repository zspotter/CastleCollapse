package me.zspotter.CastleCollapse.bukkit;

import java.util.Arrays;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ColorClayPhysicsOperator implements PhysicsOperator {

	private static final Material GRAV_MATERIAL = Material.STAINED_CLAY;
	private static final byte[] STABILITY_DATA = 
		{ // Metadata values to apply to grav blocks
			7,	// Gray
			14,	// Red
			1,	// Orange
			4,	// Yellow
			13,	// Green
			3,	// Light Blue
		};
	
	private static final int STABILITY_MIN = 0;
	private static final int STABILITY_MAX = (STABILITY_DATA.length - 1);
	
	private static final HashSet<Material> STABLE_MATERIALS = new HashSet<Material>(Arrays.asList(new Material[]
		{
			Material.BEDROCK,
			Material.STONE,
			Material.DIRT,
			Material.GRASS,
			Material.SANDSTONE,
			Material.MYCEL,
			Material.COBBLESTONE,
			Material.OBSIDIAN,
			Material.MOSSY_COBBLESTONE,
			Material.SMOOTH_BRICK,
			Material.BRICK,
			Material.NETHER_BRICK,
			Material.NETHERRACK
		}));
	
	private CCPlugin plug;
	
	public ColorClayPhysicsOperator(CCPlugin plugin) {
		plug = plugin;
	}
	
	@Override
	public boolean doesGravityEffect(Material material) {
		return (material == GRAV_MATERIAL);
	}

	@Override
	public void applyGravity(Block block) {
		
		int stability = calculateStability(block);
		
		if (stability < STABILITY_MIN || stability > STABILITY_MAX) {
			throw new IllegalStateException("Got invalid stability "+stability+" for "+block);
		}
		
		if (stability == STABILITY_MIN) {
			// Block will fall due to instability
			block.setType(Material.AIR); // Set block to air now, replace it with falling sand after the next if-statement
			// If block beneath isn't grav block and isn't a stable material, break it
			Block beneath = block.getRelative(BlockFace.DOWN);
			if (beneath != null && !beneath.isEmpty() && 
					!doesGravityEffect(beneath.getType()) && !STABLE_MATERIALS.contains(beneath.getType())) {
				beneath.breakNaturally();
			}
			// Make the block fall
			block.getWorld().spawnFallingBlock(block.getLocation(), GRAV_MATERIAL, STABILITY_DATA[STABILITY_MIN]);
			return;
		} 
		
		// Set block stability metadata
		block.setData(STABILITY_DATA[stability]);
	}
	
	private int calculateStability(Block block) {
		// toPlace will take the highest stability value of the following
		// For horizontally adjacent and overhead blocks, this stability will be other.stability - 1
		// For block underneath, this stability will be other.stability
		int stability = stabilityOf(block.getRelative(BlockFace.DOWN));
		
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.NORTH)) - 1);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.SOUTH)) - 1);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.EAST)) - 1);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.WEST)) - 1);
		stability = Math.max(stability, stabilityOf(block.getRelative(BlockFace.UP)) - 1);
		
		// Make sure stability >= 0 and <= MAX_STABILITY
		stability = Math.max(stability, 0);
		stability = Math.min(stability, STABILITY_MAX);
		
		return stability;
	}
	
	/**
	 * Returns the 'cached' stability of a block assuming the blocks surrounding it have not been
	 * modified since the last time it was calculated.
	 * A return value less than STABILITY_MIN means the block doesn't have a valid stability value (ie, it isn't solid)
	 * A return value of STABILITY_MIN means the block should fall.
	 * A return value of STABILITY_MAX means the block is grav effected and very stable.
	 * A return value greater than STABILITY_MAX means the block is not effected by grav.
	 * @param block The Block to test stability of. Should exist in a world.
	 * @return A value from STABILIT_MIN to (STABILITY_MAX+1)
	 */
	private int stabilityOf(Block block) {
		if (!doesGravityEffect(block.getType())) {
			if (!STABLE_MATERIALS.contains(block.getType())) {
				return (STABILITY_MIN - 1);
			} else {
				return STABILITY_MAX + 1 + block.getType().getMaxDurability();
			}
		}

		for (int s = STABILITY_DATA.length - 1; s >= 0; s--) {
			if (block.getData() == STABILITY_DATA[s]) {
				return s;
			}
		}

		return (STABILITY_MIN - 1);
	}

}
