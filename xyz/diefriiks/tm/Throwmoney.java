package xyz.diefriiks.tm;

import java.io.File;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Throwmoney extends JavaPlugin implements CommandExecutor, Listener{

	private FileConfiguration config;
	protected boolean update = false;
	private Updater updater;
	protected String prefix;
	private String[] msg;
	private Object[] list;
	private boolean debug;
	private Economy econ;
	private ItemStack money;

	public void onEnable() {		
		load();
		updater = new Updater(this);
		getCommand("tm").setExecutor(this);
		getCommand("tma").setExecutor(this);
		this.getServer().getPluginManager().registerEvents(this, this);
		
        if (!setupEconomy() ) {
            say("Vault is needed to hook into your Money Plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		if(debug) {
			config.set("debug", false);
				saveConfig();
				reloadConfig();
			updater.send();
		}
	}

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	@SuppressWarnings("unchecked")
	private void load() {
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		list = new Object[6];
			list[0] = Material.valueOf(config.getString("money.material"));
			list[1] = Enchantment.getByName(config.getString("money.enchant"));
			list[2] = ChatColor.translateAlternateColorCodes('&', config.getString("money.name"));
			list[3] = config.getInt("money.enchantlvl");
			list[4] = config.getInt("money.worth");
			list[5] = config.getStringList("money.lore");
			
		for(int i = 0; i < ((List<String>)list[5]).size(); i++) {
			((List<String>)list[5]).set(i, ChatColor.translateAlternateColorCodes('&', ((List<String>)list[5]).get(i)));
		}
		
		money = new ItemStack((Material) list[0], 1);
		ItemMeta meta = money.getItemMeta();
			meta.setDisplayName((String) list[2]);
			meta.setLore((List<String>)list[5]);
		money.setItemMeta(meta);
		money.addUnsafeEnchantment((Enchantment)list[1], (int)list[3]);
		
		debug = config.getBoolean("debug");
		
		msg = new String[7];
		msg[0] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.nopermission"));
		msg[1] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.disable"));
		msg[2] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reset"));
		msg[3] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reload"));
		msg[4] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.getmoney"));
		msg[5] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.notenough"));
		msg[6] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.sellmoney"));
		prefix = ChatColor.translateAlternateColorCodes('&', config.getString("msg.prefix"));
	}

	public void reset() {
		File configFile = new File(getDataFolder(), "config.yml");
	    configFile.delete();
	    saveDefaultConfig();
		reload();
	}

	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    return true;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] split) {
		boolean isplayer = false;
		Player p = null;
		
		if(sender instanceof Player) {
			p = (Player) sender;
			isplayer = true;
			
			if(update) {
				tell(p);
			}
		}
		if(cmd.getName().equalsIgnoreCase("tm")){
			if(split.length == 1) {
				if(isplayer) {
					if(p.hasPermission("tm.use") || p.isOp()) {
						if(isInteger(split[0])) {
							givemoney(p, Integer.parseInt(split[0]));
							return true;
						}
					}
				}else {
					say("Ingame only ...");
					return true;
				}
			}
		}
		else if(cmd.getName().equalsIgnoreCase("tma")){
				if(split.length == 1) {
					if(split[0].equalsIgnoreCase("reload")) {
						if(isplayer) {
							if(p.hasPermission("tm.reload") || p.isOp()) {
									reload();
								p.sendMessage(prefix + msg[3]);
								say(msg[3] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + msg[0]);
							}
						}else {
								reload();
							say(msg[3] + " by Console");
						}
						return true;
					}else if(split[0].equalsIgnoreCase("reset")) {
						if(isplayer) {
							if(p.hasPermission("tm.reset") || p.isOp()) {
									reset();
								p.sendMessage(prefix + msg[2]);
								say(msg[2] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + prefix + msg[0]);
							}
						}else {
								reset();
							say(msg[2] + " by Console");
						}
						return true;
					}else if(split[0].equalsIgnoreCase("disable")) {
						if(isplayer) {
							if(p.hasPermission("tm.disable") || p.isOp()) {
									this.setEnabled(false);
								p.sendMessage(prefix + msg[1]);
								say(msg[1] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + msg[0]);
							}
						}else {
								this.setEnabled(false);
							say(msg[1] + " by Console");
						}
						return true;
					}
				}
		}
		return false;
	}

	@EventHandler
	public void actionPerformed(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			if(e.hasItem()) {
				if(e.getPlayer().hasPermission("tm.use") || e.getPlayer().isOp()) {
					if(e.getItem().isSimilar(money)) {
						execute(e.getPlayer(), e.getItem());
					}
				}
			}
		}
	}
	
	private void execute(final Player player, ItemStack item) {
		if(item.getAmount() > 1) {
			ItemStack is = player.getItemInHand();
			is.setAmount(is.getAmount() - 1);
			player.setItemInHand(is);
		}else {
			player.setItemInHand(null);
		}
		econ.depositPlayer((OfflinePlayer)player, 1000);
		player.sendMessage(prefix + msg[6]);
	}
	
	private void givemoney(Player p, int amount) {
		if(hasMoney(p, amount)) {
			Inventory inv = p.getInventory();
			int j = 0;
			for(int i = amount; i >= (int)list[4]; i = i-(int)list[4]) {
				inv.addItem(money);
				j++;
			}
			String text = msg[4].replace("%amount%", ""+j);
			p.sendMessage(prefix + text);
			
		}else {
			p.sendMessage(prefix + msg[5]);
		}
	}

	private boolean hasMoney(Player p, int amount) {
		int k = 0;
		for(int l = amount; l >= (int)list[4]; l = l-(int)list[4]) {
			k = k + (int)list[4];
		}

		if(econ.getBalance((OfflinePlayer)p) >= k) {
			econ.withdrawPlayer((OfflinePlayer)p, k);
			return true;
		}else {
			return false;
		}
		
	}

	private void reload(){
 	   	try {
			reloadConfig();
			load();
 	   	} catch (Exception e) {}
	}

	public void say(String msg){
		System.out.println(ChatColor.stripColor(prefix + msg));
	}

	public void tell(Player p) {
		   	p.sendMessage(prefix + "-------------------------------------------------");
		   	p.sendMessage(prefix + "ThrowMoney is outdated. Get the new Version here:");
		   	p.sendMessage(prefix + "http://www.pokemon-online.xyz/plugin");
		   	p.sendMessage(prefix + "-------------------------------------------------");
	}
			
}
