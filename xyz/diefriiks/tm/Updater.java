package xyz.diefriiks.tm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Updater implements Listener{
	private Throwmoney plugin;
	private int version = 1;

	@SuppressWarnings("deprecation")
	public Updater(Throwmoney plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		check();
		
		if(plugin.update) {
		    for (Player p : plugin.getServer().getOnlinePlayers()) {
		    	if(p.isOp() || p.hasPermission("tm.update")) {
		    		plugin.tell(p);
		    	}
		    }
		}
	}

	@EventHandler
	public void login(PlayerJoinEvent e) {
		if(plugin.update) {
	    	if(e.getPlayer().isOp() || e.getPlayer().hasPermission("tm.update")) {
	    		plugin.tell(e.getPlayer());
	    	}
		}
	}

	private void check() {
		String sourceLine = null;
        try {
	        URL url = new URL("http://www.pokemon-online.xyz/plugin");
	        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	        String str;
	        while ((str = in.readLine()) != null) {
	            if(str.startsWith("throwmoney:")) {
	            	sourceLine = str.split(":")[1];
	            	break;
	            }
	        }
        } catch (IOException e) {}
        
	    if(sourceLine != null && Integer.parseInt(sourceLine) != version  ){
	    	plugin.update  = true;
	    	plugin.say("-------------------------------------------------");
	    	plugin.say("ThrowMoney is outdated. Get the new Version here:");
	    	plugin.say("http://www.pokemon-online.xyz/plugin/");
	    	plugin.say("-------------------------------------------------");
	    }
	}

}
