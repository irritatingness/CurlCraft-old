package com.irritatingness;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static spark.Spark.*;

public class CurlCraft extends JavaPlugin {
	
	private static Logger logger;
	private static Plugin plugin;
	
	public void onEnable() {
		// Grab the server logger
		logger = this.getServer().getLogger();
		
		// Copy a reference of this plugin
		plugin = this;
		
		logger.info("CurlCraft has been enabled!");
		// Save the default config, if it doesn't exist on the server
		this.saveDefaultConfig();
		
		final FileConfiguration config = this.getConfig();
		
		// Generate REST endpoints for each endpoint found in the config file
		for (final String key : config.getConfigurationSection("endpoints").getKeys(false)) {
			List<String> actions = config.getStringList("endpoints." + key + ".actions");
			if (actions.isEmpty()) {
				logger.warning("Actions list for endpoint with name " + key + " was empty and will not be registered!");
			} else {
				String endpoint = "/" + key;
				post(endpoint, (req, res) -> {
					
					// Body if it exists
					final String body = req.body();					
						
					Bukkit.getScheduler().runTask(this, new Runnable() {
						@Override
						public void run() {
							
							String target = null;
							
							// Determine if we have a target
							if (body != null && !body.isEmpty()) {
								JSONObject json;
								try {
									// TODO: Fix NPE here
									json = (JSONObject) new JSONParser().parse(body);
									target = json.get("target").toString();
								} catch (ParseException e) {
									// The JSON we got in the was wrong
									logger.severe("JSON body for request with endpoint of " + key + " was malformed, no target found.");
								}								
							}
							
							for (String action : actions) {
								try {
									
									// Get all methods and match on method name
									// Arguably cleaner than catching the NoSuchMethodException and retrying for 0 args.
									Method[] methods = plugin.getClass().getMethods();
									
									for (Method m : methods) {
										if (m.getName().equalsIgnoreCase(action)) {
											if (m.getParameterCount() > 0) {
												// Invoke with the given target
												if (target != null && !target.isEmpty()) {
													m.invoke(plugin, target);
												} else {
													logger.warning("Action named " + action + " for endpoint named " + key + " was not executed because the target was null.");
												}
											} else {
												// Invoke without arguments
												m.invoke(plugin);
											}
											// Did what we needed to do, exit this loop
											break;
										}
									}
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
										| SecurityException e) {
									e.printStackTrace();
									logger.severe("No action named " + action + " is available in CurlCraft currently.");
								}
							}
						}
					});
					return "OK";
				});
			}
		}
	}
	
	public void onDisable() {
		
	}
	
	/*
	 * DEFINE NEW ACTIONS BELOW
	 * TODO: Eventually break these out into another class and have a reference class to load methods from.
	 */
	
	public void electrocute(String target) {
		Player p = this.getServer().getPlayerExact(target);
		if (p != null) {
			p.getWorld().strikeLightning(p.getLocation());
		} else {
			logger.warning("electrocute with target " + target + " was not exocuted because the target was not found!");
		}
	}
	
	public void electrocuteAll() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.getWorld().strikeLightning(p.getLocation());
		}
	}
	
	public void fireworks(String target) {
		Player p = this.getServer().getPlayerExact(target);
		if (p != null) {
			p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
		} else {
			logger.warning("fireworks with target " + target + " was not exocuted because the target was not found!");
		}
	}
	
	public void fireworksAll() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
		}
	}
	
	public void spawnCreeper(String target) {
		Player p = this.getServer().getPlayerExact(target);
		if (p != null) {
			Random r = new Random();
			Location pLoc = p.getLocation();
			// Add x and z of 0-10 to randomize the spawn location
			pLoc.add(r.nextDouble() * 10, 0, r.nextDouble() * 10);
			// Make sure the location is safe, no wasted creepers...
			pLoc.setY(pLoc.getWorld().getHighestBlockYAt(pLoc));
			p.getWorld().spawnEntity(pLoc, EntityType.CREEPER);
		} else {
			logger.warning("spawnCreeper with target " + target + " was not exocuted because the target was not found!");
		}
	}
	
	public void test1() {
		logger.info("Test 1 executed.");
	}
	
	public void test2() {
		logger.info("Test 2 executed.");
	}
	
	public void test3() {
		logger.info("Test 3 executed.");
	}
	
	public void test4(String target) {
		logger.info("Test 4 recieved target: " + target);
	}

}
