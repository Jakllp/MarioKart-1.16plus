package net.stormdev.mario.races;

import java.util.Map;
import java.util.UUID;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.queues.RaceQueue;
import net.stormdev.mario.utils.ParticleEffects;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.ucarsCommon.StatValue;

public class RaceMethods {
	@SuppressWarnings("unused")
	private MarioKart plugin = null;

	public RaceMethods() {
		this.plugin = MarioKart.plugin;
	}
	
	public void createExplode(final Location loc){
		Runnable run = new Runnable(){

			@Override
			public void run() {
				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3f, 1f);
				//loc.getWorld().createExplosion(loc, 0);
				loc.getWorld().playEffect(loc, Effect.SMOKE, 3);
				ParticleEffects.sendToLocation(ParticleEffects.EXPLODE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.HUGE_EXPLODE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.LAVA_SPARK, loc, 0, 0, 0, 1, 10);
				ParticleEffects.sendToLocation(ParticleEffects.FIRE, loc, 0, 0, 0, 1, 5);
				ParticleEffects.sendToLocation(ParticleEffects.FIREWORK_SPARK, loc, 0, 0, 0, 1, 5);
				return;
			}};
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(MarioKart.plugin, run);
		}
	}
	
	public void createExplode(final Location loc, final int size){
		Runnable run = new Runnable(){

			@Override
			public void run() {
				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
				//loc.getWorld().createExplosion(loc, 0);
				loc.getWorld().playEffect(loc, Effect.SMOKE, size);
				ParticleEffects.sendToLocation(ParticleEffects.EXPLODE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.HUGE_EXPLODE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.LAVA_SPARK, loc, 0, 0, 0, 1, size*2);
				ParticleEffects.sendToLocation(ParticleEffects.FIRE, loc, 0, 0, 0, 1, size);
				ParticleEffects.sendToLocation(ParticleEffects.FIREWORK_SPARK, loc, 0, 0, 0, 1, size);
				return;
			}};
		
		
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(MarioKart.plugin, run);
		}
	}

	public synchronized Race inAGame(Player player, Boolean update) {
		return MarioKart.plugin.raceScheduler.inAGame(player, update);
	}

	public synchronized RaceQueue inGameQue(Player player) {
		Map<UUID, RaceQueue> queues = MarioKart.plugin.raceQueues.getAllQueues();
		for (UUID id : queues.keySet()) {
			try {
				RaceQueue queue = queues.get(id);
				if (queue.containsPlayer(player)) {
					return queue;
				}
			} catch (Exception e) {
				return null;
			}
		}
		return null;
	}
	
	public synchronized Minecart spawnKart(Location loc) {
		Minecart car = (Minecart) loc.getWorld().spawnEntity(
				loc, EntityType.MINECART);
		car.setMetadata("kart.racing", new StatValue(null, MarioKart.plugin));
		
		Jigsaw saw = (Jigsaw) Material.JIGSAW.createBlockData();
		
		int rand = MarioKart.plugin.random.nextInt(11); // 0-11 random
		switch(rand) {
			case 0:
				saw.setOrientation(Jigsaw.Orientation.UP_EAST);
				break;
			case 1:
				saw.setOrientation(Jigsaw.Orientation.DOWN_EAST);
				break;
			case 2:
				saw.setOrientation(Jigsaw.Orientation.UP_NORTH);
				break;
			case 3:
				saw.setOrientation(Jigsaw.Orientation.DOWN_NORTH);
				break;
			case 4:
				saw.setOrientation(Jigsaw.Orientation.UP_WEST);
				break;
			case 5:
				saw.setOrientation(Jigsaw.Orientation.DOWN_WEST);
				break;
			case 6:
				saw.setOrientation(Jigsaw.Orientation.UP_SOUTH);
				break;
			case 7:
				saw.setOrientation(Jigsaw.Orientation.DOWN_SOUTH);
				break;
			case 8:
				saw.setOrientation(Jigsaw.Orientation.NORTH_UP);
				break;
			case 9:
				saw.setOrientation(Jigsaw.Orientation.WEST_UP);
				break;
			case 10:
				saw.setOrientation(Jigsaw.Orientation.EAST_UP);
				break;
			case 11:
				saw.setOrientation(Jigsaw.Orientation.SOUTH_UP);
				break;
		}
		car.setDisplayBlockData(saw);
		car.setDisplayBlockOffset(0);
		
		return car;
	}
}
