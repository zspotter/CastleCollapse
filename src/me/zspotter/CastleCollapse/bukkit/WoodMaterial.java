package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class WoodMaterial implements GravityMaterial {
	
	@Override
	public boolean containsMaterial(Material material) {
		return (material == Material.WOOD || material == Material.LOG);
	}

	@Override
	public int weight() {
		return 1;
	}

	@Override
	public void makeFrom(Block block, int stability) {
		double s = ((double)stability) / ((double)MedievalPhysicsOperator.MAX_STABILITY);
		
		if(s >= 0.9) {
			block.setType(Material.LOG);
			block.setData((byte) 12); // Oak log
		} else {
			block.setType(Material.WOOD);
		 	
			if (s >= 0.7) {
				block.setData((byte) 5); // Dark oak plank
			} else if (s >= 0.5) {
				block.setData((byte) 1); // Spruce plank
			} else if (s >= 0.3) {
				block.setData((byte) 0); // Oak plank
			} else {
				block.setData((byte) 3); // Jungle plank 
			}
		}
			
	}

}
