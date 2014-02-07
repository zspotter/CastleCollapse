package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface PhysicsOperator {
	
	/**
	 * @param material A Material to test
	 * @return True if the Material should be affected by CC's graviy
	 */
	public boolean doesGravityEffect(Material material);
	
	/**
	 * Applies CC's gravity to a gravity affected block and updates adjacent blocks
	 * @param block The Block to apply gravity to
	 * @throws IllegalArgumentException Thrown if the Block's material is not able 
	 * 	to be affected by CC's gravity
	 */
	public void applyGravity(Block block);
	
}
