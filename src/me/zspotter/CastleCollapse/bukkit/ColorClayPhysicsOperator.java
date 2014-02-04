package me.zspotter.CastleCollapse.bukkit;

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
	
	@Override
	public boolean doesGravityEffect(Material material) {
		return (material == GRAV_MATERIAL);
	}
	
	@Override
	public void makeGravityBlock(Block block) {
		block.setType(GRAV_MATERIAL);
		applyGravity(block);
	}

	@Override
	public void applyGravity(Block block) throws IllegalArgumentException {
		if (!doesGravityEffect(block.getType())) {
			throw new IllegalArgumentException("Cannot apply gravity to "+block);
		}
		
		int stability = calculateStability(block);
		
		if (stability < STABILITY_MIN || stability > STABILITY_MAX) {
			throw new IllegalStateException("Got invalid stability "+stability+" for "+block);
		}
		
		if (stability == STABILITY_MIN) {
			// Block will fall due to instability
			block.setType(Material.AIR);
			block.getWorld().spawnFallingBlock(block.getLocation(), 
					GRAV_MATERIAL, STABILITY_DATA[STABILITY_MIN])
					.setDropItem(false);
			
		} else {
			block.setData(STABILITY_DATA[stability]);
		}
	}
	
	private int calculateStability(Block block) {
		// toPlace will take the highest stability value of the following
		// For horizontally adjacent and overhead blocks, this stability will be other.stability - 1
		// For block underneath, this stability will be other.stability
		int stabUnder = stabilityOf(block.getRelative(BlockFace.DOWN));
		
		int stabOther = -1;
		int stabTmp;
		
		stabTmp = stabilityOf(block.getRelative(BlockFace.NORTH));
		if (stabTmp > stabOther) stabOther = stabTmp;
		
		stabTmp = stabilityOf(block.getRelative(BlockFace.SOUTH));
		if (stabTmp > stabOther) stabOther = stabTmp;
		
		stabTmp = stabilityOf(block.getRelative(BlockFace.EAST));
		if (stabTmp > stabOther) stabOther = stabTmp;
		
		stabTmp = stabilityOf(block.getRelative(BlockFace.WEST));
		if (stabTmp > stabOther) stabOther = stabTmp;
		
		stabTmp = stabilityOf(block.getRelative(BlockFace.UP));
		if (stabTmp > stabOther) stabOther = stabTmp;
		
		if (stabUnder < STABILITY_MIN && stabOther <= STABILITY_MIN) {
			return STABILITY_MIN;
		} else if (stabUnder > STABILITY_MAX || stabOther > STABILITY_MAX) {
			return STABILITY_MAX;
		}
		
		if (stabOther > stabUnder) {
			return (stabOther - 1);
		} else {
			return stabUnder;
		}
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
		if (!block.getType().isSolid() || block.getType().isTransparent()) {
			return (STABILITY_MIN - 1);
		}
		
		if (!doesGravityEffect(block.getType())) {
			return STABILITY_MAX + 1 + block.getType().getMaxDurability();
		}

		for (int s = STABILITY_DATA.length - 1; s >= 0; s--) {
			if (block.getData() == STABILITY_DATA[s]) {
				return s;
			}
		}

		return (STABILITY_MIN - 1);
	}

}
