package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class StoneMaterial implements GravityMaterial {
	
	@Override
	public boolean containsMaterial(Material material) {
		return (material == Material.SMOOTH_BRICK);
	}

	@Override
	public int weight() {
		return 2;
	}

	@Override
	public void makeFrom(Block block, int stability) {
		double s = ((double)stability) / ((double)MedievalPhysicsOperator.MAX_STABILITY);
		
		block.setType(Material.SMOOTH_BRICK);
		if(s >= 0.6) {
			block.setData((byte) 0); // Solid brick
		} else {
			block.setData((byte) 2); // Cracked brick 
		}
			
	}

}
