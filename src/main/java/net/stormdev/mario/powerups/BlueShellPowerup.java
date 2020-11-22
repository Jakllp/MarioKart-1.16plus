package net.stormdev.mario.powerups;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import net.stormdev.mario.items.ItemStacks;
import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.players.User;
import net.stormdev.mario.races.Race;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlueShellPowerup extends TrackingShellPowerup {
	
	public BlueShellPowerup(){
		super.setItemStack(getBaseItem());
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}
	
	public static PowerupType getPowerupType(){
		return PowerupType.BLUE_SHELL;
	}
	
	private static final ItemStack getBaseItem(){
		String id = MarioKart.config.getString("mariokart.blueShell");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Targets and slows the leader");
		lore.add("*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(MarioKart.colors.getInfo()+"Blue shell");
		i.setItemMeta(im);
		
		return i;
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		if(user.isFinished()){
			return;
		}
		SortedMap<String, Double> sorted = race.getRaceOrder();
		Set<String> keys = sorted.keySet();
		Object[] pls = keys.toArray();
		if(pls.length < 1){
			return;
		}
		
		setTarget((String) pls[0]);
		inHand.setAmount(inHand.getAmount() - 1);
		spawn(carLoc, player);
		start(); //Start tracking target player
	}

	@Override
	public PowerupType getType() {
		return PowerupType.BLUE_SHELL;
	}
	
}
