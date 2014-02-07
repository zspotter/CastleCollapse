package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.Material;
import org.bukkit.block.Block;

public interface GravityMaterial {
	
	public boolean containsMaterial(Material material);
	
	public int weight();
	
	public void makeFrom(Block block, int stability);

}
