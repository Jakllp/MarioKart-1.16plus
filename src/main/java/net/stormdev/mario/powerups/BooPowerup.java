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
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.useful.ucarsCommon.StatValue;

public class BooPowerup extends PowerupBase {
	
	public BooPowerup(){
		super.setItemStack(getBaseItem());
	}

	@Override
	public void doRightClickAction(User user, Player player, Minecart car,
			Location carLoc, final Race race, ItemStack inHand) {
		if(!user.isInRace()){
			return;
		}
		
		inHand.setAmount(inHand.getAmount() - 1);
		PotionEffect effect = new PotionEffect(
				PotionEffectType.INVISIBILITY, 120, 10);
		SortedMap<String, Double> sorted = race.getRaceOrder();
		Set<String> keys = sorted.keySet();
		final Object[] pls = keys.toArray();
		int pppos = 0;
		for (int i = 0; i < pls.length; i++) {
			if (pls[i].equals(player.getName())) {
				pppos = i;
			}
		}
		int pos = pppos - 1;
		if (!(pos < 0)) {
			final Player pla = MarioKart.plugin.getServer().getPlayer(
					(String) pls[pos]);
			if(!MarioKart.powerupManager.isPlayerImmune(pla) && !race.getUser(pla).isFinished()){ //Only players in cars
				pla.setMetadata("kart.rolling", new StatValue(true, MarioKart.plugin));
				pla.getInventory().clear();
				MarioKart.plugin.hotBarManager.updateHotBar(pla);
				pla.getInventory().addItem(
						getNewItem());
				PotionEffect nausea = new PotionEffect(
						PotionEffectType.CONFUSION, 240, 10);
				pla.addPotionEffect(nausea);
				pla.getWorld().playSound(pla.getLocation(),
						Sound.AMBIENT_CAVE, 1, 1);
				pla.updateInventory();
				String msg = MarioKart.msgs.get("mario.hit");
				msg = msg.replaceAll("%name%", "ghost");
				pla.sendMessage(MarioKart.colors.getInfo() + msg);
				MarioKart.plugin.getServer().getScheduler()
						.runTaskLater(MarioKart.plugin, new Runnable() {

							@Override
							public void run() {
								User u = race.getUser(pla.getName());
								if(u != null && u.isInRace()){
									pla.removeMetadata("kart.rolling", MarioKart.plugin);
									pla.getInventory().clear();
									MarioKart.plugin.hotBarManager.updateHotBar(pla);
									pla.updateInventory();
								}
								else{ //They've already finished the race
									pla.removeMetadata("kart.rolling", MarioKart.plugin);
									pla.updateInventory();
								}
							}
						}, 240l);
			}
		}
		player.addPotionEffect(effect);
	}

	@Override
	public void doLeftClickAction(User user, Player player, Minecart car,
			Location carLoc, Race race, ItemStack inHand) {
		return; //Do nothing
	}
	
	private static final ItemStack getBaseItem(){
		String id = MarioKart.config.getString("mariokart.boo");
		ItemStack i = ItemStacks.get(id);
		
		List<String> lore = new ArrayList<String>();
		lore.add("+Invisible for 6s");
		lore.add("+Apply nausea to racer ahead");
		lore.add("*Right click to deploy");
		
		ItemMeta im = i.getItemMeta();
		im.setLore(lore);
		im.setDisplayName(MarioKart.colors.getInfo()+"Boo");
		i.setItemMeta(im);
		
		return i;
	}
	
	public static boolean isItemSimilar(ItemStack i){
		return getBaseItem().isSimilar(i);
	}

	@Override
	public PowerupType getType() {
		return PowerupType.BOO;
	}
	
	public static PowerupType getPowerupType() {
		return PowerupType.BOO;
	}

}
