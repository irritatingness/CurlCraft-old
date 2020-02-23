package com.irritatingness;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
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
									json = (JSONObject) new JSONParser().parse(body);
									target = json.get("target").toString();
									logger.info("Got target: " + target);
								} catch (ParseException e) {
									// The JSON we got in the was wrong
									logger.severe("JSON body for request with endpoint of " + key + " was malformed, no target found.");
								}								
							}
							
							for (String action : actions) {
								try {
									
									// Get the action method
									Method m = plugin.getClass().getMethod(action);
									
									// TODO: The getParameterCount will never return > 0, because the getMethod doesn't include String.class....
									// We need to be able to support methods that take args AND methods that do NOT take args to support optional targets.
									
									// If the method accepts a parameter, and we have a target, call method with target
									if (m.getParameterCount() > 0) {
										logger.info("Found a method with param count > 0: " + m.getName());
										if (target != null) {
											m.invoke(plugin, target);
										} else {
											logger.warning("Action named " + action + " for endpoint named " + key + " was not executed because the target was null.");
										}
									} else {
										m.invoke(plugin);
									}
								} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
										| NoSuchMethodException | SecurityException e) {
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
	
	
	// DEFINE ACTIONS BELOW, TODO: MOVE TO THEIR OWN FILE(S)?
	public void electrocuteAll() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.getWorld().strikeLightning(p.getLocation());
		}
	}
	
	public void fireworksAll() {
		for (Player p : this.getServer().getOnlinePlayers()) {
			p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
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
