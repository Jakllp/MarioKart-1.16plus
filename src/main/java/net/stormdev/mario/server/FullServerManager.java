package net.stormdev.mario.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.stormdev.mario.mariokart.MarioKart;
import net.stormdev.mario.races.Race;
import net.stormdev.mario.races.RaceType;
import net.stormdev.mario.tracks.RaceTrack;
import net.stormdev.mario.utils.LocationStrings;
import net.stormdev.mario.utils.ObjectWrapper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FullServerManager {
	public static String BUILD_PERM = "mariokart.join.build";
	public static String BUNGEE_LOBBY_ID = "lobby";
	private static FullServerManager instance = null;
	private volatile ServerStage stage = ServerStage.WAITING;
	public VoteHandler voter = null;
	public Location lobbyLoc;
	
	private RaceType mode;
	private RaceTrack track;
	private volatile Race race;
	public SpectatorMode spectators = null;
	private boolean starting = false;
	
	public static final ItemStack item;
	
	static {
		item = new ItemStack(Material.BOOK);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(ChatColor.DARK_RED+"Exit to lobby");
		im.setLore(Arrays.asList(new String[]{ChatColor.GRAY+"Right click to use"}));
		
		item.setItemMeta(im);
	}
	
	public static FullServerManager get(){
		return instance;
	}
	
	public ServerStage getStage(){
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				if(stage.equals(ServerStage.PLAYING) && Bukkit.getOnlinePlayers().size() < 1){
					restart();
				}
				return;
			}}, 2l);
		
		return stage;
	}
	
	public String getMOTD(){
		return getStage().getMOTD();
	}
	
	public void changeServerStage(ServerStage now){
		stage = now;
		
		switch(stage){
		case PLAYING: {
			voter = null;
		}
			break;
		case RESTARTING: {
			voter = null;
			track = null;
			race = null;
			starting = false;
			mode = RaceType.RACE;
			spectators.endSpectating();
		}
			break;
		case STARTING: {
			voter = null;
			Collection<? extends Player> online = Bukkit.getOnlinePlayers();
			for(Player player:online){
				player.getInventory().clear();
			}
		}
			break;
		case WAITING: {
			if(voter == null){
				voter = new VoteHandler();
			}
			spectators.endSpectating();
			Collection<? extends Player> online = Bukkit.getOnlinePlayers();
			for(Player player:online){
				BossBar.removeBar(player);
				player.getInventory().clear();
				player.getInventory().addItem(item.clone());
				player.teleport(lobbyLoc);
				if(spectators.isSpectating(player)){
					spectators.stopSpectating(player);
				}
				player.setGameMode(GameMode.SURVIVAL);
			}
		}
		break;
		case BUILDING: {
			Collection<? extends Player> online = Bukkit.getOnlinePlayers();
			for(Player player:online){
				if(!player.hasPermission(BUILD_PERM)){
					player.kickPlayer("Server now closed, sorry!");
				}
				else {
					player.sendMessage(ChatColor.GRAY+"Server is now in build mode...");
				}
			}
			if(voter != null){
				voter.closeVotes();
				voter = null;
			}
		}
			break;
		default:
			break;
		}
	}
	
	public FullServerManager(){
		instance = this;
		try {
			Bukkit.getMessenger().registerOutgoingPluginChannel(MarioKart.plugin, "BungeeCord");
		} catch (Exception e) {
			//OH DEAR
			throw new RuntimeException("BungeeCord unable to work");
		}
		MarioKart.logger.info("Using full server mode!");
		BUNGEE_LOBBY_ID = MarioKart.config.getString("general.server.bungeelobby");
		MarioKart.logger.info("Using "+BUNGEE_LOBBY_ID+" as the game lobby!");
		Bukkit.getPluginManager().registerEvents(new ServerListener(), MarioKart.plugin);
		lobbyLoc = LocationStrings.getLocation(MarioKart.config.getString("general.server.gamelobby"));
		if(lobbyLoc == null || lobbyLoc.getWorld() == null){
			MarioKart.logger.info("INVALID Lobby location: "+MarioKart.config.getString("general.server.gamelobby"));
		}
		spectators = new SpectatorMode();
		Bukkit.getScheduler().runTaskTimerAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				if(stage.equals(ServerStage.PLAYING) && MarioKart.plugin.raceScheduler.getRacesRunning() < 1){
					changeServerStage(ServerStage.RESTARTING);
					Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

						@Override
						public void run() {
							restart();
							return;
						}}, 10*20l);
				}
				return;
			}}, 30*20l, 30*20l);
		changeServerStage(ServerStage.WAITING);
		tipsHandler();
	}
	
	private void tipsHandler(){
		final ObjectWrapper<Integer> count = new ObjectWrapper<Integer>(0);
		Bukkit.getScheduler().runTaskTimerAsynchronously(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				if(!getStage().equals(ServerStage.PLAYING)){
					count.setValue(0);
					return; //Don't show any tips...
				}
				//Tips bar
				int i = count.getValue();
				
				if(i < 1){ //Set the next tip...
					String next = Tips.random();
					final String tip = ChatColor.DARK_RED+"[TIP:] "+ChatColor.GOLD+next;
					Bukkit.getScheduler().runTask(MarioKart.plugin, new Runnable(){

						@Override
						public void run() {
							Collection<? extends Player> online = Bukkit.getOnlinePlayers();
							for(Player player:online){
								BossBar.setMessage(player, tip, 16);
							}
							return;
						}});
				}
				
				count.setValue(count.getValue()+1);
				if(count.getValue() > 3){
					//15s have passed since tip set
					count.setValue(0);
				}
				return;
			}}, 5*20l, 5*20l);
	}
	
	public void sendToLobby(Player player){
		player.setHealth(((Damageable)player).getMaxHealth());
		player.setFoodLevel(20);
		player.getInventory().clear();
		PlayerServerSender.sendToServer(player, BUNGEE_LOBBY_ID);
	}
	
	public void restart(){
		//Reset game
		changeServerStage(ServerStage.RESTARTING);
		//wait...
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				Collection<? extends Player> online = Bukkit.getOnlinePlayers();
				for(Player p:online){
					sendToLobby(p);
				}
				Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

					@Override
					public void run() {
						if(MarioKart.fullServerRestart){
							Bukkit.getServer().shutdown();
							System.exit(0);
							return;
						}
						else {
							System.gc();
							if(stage.equals(ServerStage.RESTARTING)){
								changeServerStage(ServerStage.WAITING);
							}
						}
						return;
					}}, 10*20l);
				return;
			}}, 10*20l);
	}
	
	public void trackSelected(final String trackName){
		if(stage == ServerStage.BUILDING){ //Ignore
			return;
		}
		if(starting){
			return;
		}
		starting = true;
		changeServerStage(ServerStage.STARTING);
		voter = null; //Stop voting stuff working
		
		int online = Bukkit.getOnlinePlayers().size();
		if(online < 2){
			mode = RaceType.TIME_TRIAL;
		}
		else {
			mode = RaceType.RACE;
		}
		
		track = MarioKart.plugin.trackManager.getRaceTrack(trackName);
		if(track == null){
			changeServerStage(ServerStage.WAITING);
			return;
		}
		
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Map: "+ChatColor.GOLD+trackName);
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Mode: "+ChatColor.GOLD+mode.name().toLowerCase());
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"------------------------------");
		
		Bukkit.broadcastMessage(ChatColor.BOLD+""+ChatColor.DARK_RED+"Please wait "+ChatColor.GOLD+"10s"+ChatColor.DARK_RED+" for the game to start!");
		
		
		Collection<? extends Player> o = Bukkit.getOnlinePlayers();
		for(Player player:o){
			BossBar.removeBar(player);
		}
		
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				Collection<? extends Player> o = Bukkit.getOnlinePlayers();
				for(Player player:o){
					BossBar.setMessage(player, ChatColor.RED+"Starting...", 9);
				}
				return;
			}}, 20l);
		
		
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				//Start the game!
				changeServerStage(ServerStage.PLAYING);
				
				Collection<? extends Player> players = Bukkit.getOnlinePlayers();
				if(players.size() < 1 || track == null || track.getTrackName() == null || mode == null){
					restart();
					return;
				}
				race = new Race(track,
						track.getTrackName(), mode);
				List<Player> q = new ArrayList<Player>(Bukkit.getOnlinePlayers());
				for (Player p : q) {
					if (p != null && p.isOnline()) {
						if(race.getUsers().size() < race.getTrack().getMaxPlayers()){
							race.join(p);
						}
						else {
							p.sendMessage(ChatColor.RED+"Sorry, there are not enough slots for you to join in with this race :(");
							sendToLobby(p);
						}
					}
				}
				if (race.getUsers().size() > 0) {
					MarioKart.plugin.raceScheduler.startRace(race.getTrackName(), race);
				}
				return;
			}}, 10*20l);
	}
	
	public void onEnd(final Player player, final boolean quit){
		if(quit){
			Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

				@Override
				public void run() {
					FullServerManager.get().sendToLobby(player);
					return;
				}}, 10l);
			return;
		}
		Bukkit.getScheduler().runTaskLater(MarioKart.plugin, new Runnable(){

			@Override
			public void run() {
				spectators.add(player);
				return;
			}}, 5l);
		
	}
}
