package net.stormdev.mario.shop;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.stormdev.mario.mariokart.MarioKart;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.useful.ucarsCommon.StatValue;

public class IconMenu implements Listener {

	private String name;
	private int size;
	private OptionClickEventHandler handler;
	private Plugin plugin;

	private String[] optionNames;
	private ItemStack[] optionIcons;
	private Boolean enabled = true;
	private String metaData;
	private boolean destroyOnExit = false;

	public IconMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin) {
		this.name = name;
		this.size = size;
		this.handler = handler;
		this.plugin = plugin;
		this.optionNames = new String[size];
		this.optionIcons = new ItemStack[size];
		this.metaData = "menu." + UUID.randomUUID().toString();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	public IconMenu(String name, int size, OptionClickEventHandler handler,
			Plugin plugin, boolean destroyOnExit) {
		this(name, size, handler, plugin);
		this.destroyOnExit = destroyOnExit;
	}

	public IconMenu setOption(int position, ItemStack icon, String name,
			String... info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public IconMenu setOption(int position, ItemStack icon, String name,
			List<String> info) {
		optionNames[position] = name;
		optionIcons[position] = setItemNameAndLore(icon, name, info);
		return this;
	}

	public void open(Player player) {
		Inventory inventory = Bukkit.createInventory(player, size, name);
		for (int i = 0; i < optionIcons.length; i++) {
			if (optionIcons[i] != null) {
				inventory.setItem(i, optionIcons[i]);
			}
		}
		player.openInventory(inventory);
		player.setMetadata(metaData, new StatValue(null, MarioKart.plugin));
	}

	public void destroy() {
		HandlerList.unregisterAll(this);
		handler = null;
		plugin = null;
		optionNames = null;
		optionIcons = null;
		enabled = false;
		metaData = null;
	}
	
	@EventHandler
	void onClose(InventoryCloseEvent event){
		if(event.getPlayer().hasMetadata("metaData")){
			event.getPlayer().removeMetadata(metaData, plugin);
			if(destroyOnExit){
				destroy();
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onInventoryClick(InventoryClickEvent event) {
		if (event.getView().getTitle().equals(name) && enabled
				&& event.getWhoClicked().hasMetadata(metaData)) {
			event.setCancelled(true);
			int slot = event.getRawSlot();
			if (slot >= 0 && slot < size && optionNames[slot] != null) {
				Plugin plugin = this.plugin;
				OptionClickEvent e = new OptionClickEvent(
						(Player) event.getWhoClicked(), slot, optionNames[slot]);
				handler.onOptionClick(e);
				if (e.willClose()) {
					final Player p = (Player) event.getWhoClicked();
					Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
						@Override
						public void run() {
							p.closeInventory();
							p.removeMetadata(metaData, MarioKart.plugin);
						}
					}, 1);
				}
				if (e.willDestroy()) {
					destroy();
				}
			}
		}
	}

	public interface OptionClickEventHandler {
		public void onOptionClick(OptionClickEvent event);
	}

	public class OptionClickEvent {
		private Player player;
		private int position;
		private String name;
		private boolean close;
		private boolean destroy;

		public OptionClickEvent(Player player, int position, String name) {
			this.player = player;
			this.position = position;
			this.name = name;
			this.close = true;
			this.destroy = false;
		}

		public Player getPlayer() {
			return player;
		}

		public int getPosition() {
			return position;
		}

		public String getName() {
			return name;
		}

		public boolean willClose() {
			return close;
		}

		public boolean willDestroy() {
			return destroy;
		}

		public void setWillClose(boolean close) {
			this.close = close;
		}

		public void setWillDestroy(boolean destroy) {
			this.destroy = destroy;
		}
	}

	private ItemStack setItemNameAndLore(ItemStack item, String name,
			String[] lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(Arrays.asList(lore));
		item.setItemMeta(im);
		return item;
	}

	private ItemStack setItemNameAndLore(ItemStack item, String name,
			List<String> lore) {
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}

}
