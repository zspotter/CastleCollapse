package me.zspotter.CastleCollapse.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

public class CCPlugin extends JavaPlugin {
	
	private PhysicsOperator physics;
		
	@Override
	public void onEnable() {
		physics = new ColorClayPhysicsOperator(this);
		
		getServer().getPluginManager().registerEvents(new CCEventListener(this), this);
	}

	public PhysicsOperator getPhysicsOperator() {
		return physics;
	}

}
