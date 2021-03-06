package net.alextwelshie.minedrop.listeners;

import java.util.Random;

import net.alextwelshie.minedrop.Main;
import net.alextwelshie.minedrop.SettingsManager;
import net.alextwelshie.minedrop.achievements.AchievementAPI;
import net.alextwelshie.minedrop.achievements.AchievementMenu;
import net.alextwelshie.minedrop.ranks.PlayerManager;
import net.alextwelshie.minedrop.statistics.StatisticsManager;
import net.alextwelshie.minedrop.timers.LobbyTimer;
import net.alextwelshie.minedrop.utils.BlockChooserGUI;
import net.alextwelshie.minedrop.utils.DropAPI;
import net.alextwelshie.minedrop.utils.GameState;
import net.alextwelshie.minedrop.utils.GameType;
import net.alextwelshie.minedrop.utils.OnePointEight;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

@SuppressWarnings("deprecation")
public class Listeners implements Listener {

	Scoreboard			board			= Bukkit.getScoreboardManager().getMainScoreboard();

	OnePointEight		onepointeight	= OnePointEight.getInstance();
	SettingsManager		settings		= SettingsManager.getInstance();
	StatisticsManager	statistics		= StatisticsManager.getInstance();
	DropAPI				dropapi			= DropAPI.getInstance();
	PlayerManager		pl				= PlayerManager.getInstance();
	AchievementAPI		Aapi			= AchievementAPI.getInstance();
	AchievementMenu		Amenu			= AchievementMenu.getInstance();

	private void newMapRotationUp() {
		if (Bukkit.getOnlinePlayers().size() >= 5 && Bukkit.getOnlinePlayers().size() < 7) {
			Main.getPlugin().maxRounds = 10;
			Main.getPlugin().resetVoting();
			Main.getPlugin().fillMapsLarge();
			Main.getPlugin().fillVotes();
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§aMore players detected!");
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New max rounds: §b" + Main.getPlugin().maxRounds);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New map rotation: §blarge maps.");
		} else if (Bukkit.getOnlinePlayers().size() >= 7) {
			Main.getPlugin().maxRounds = 12;
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6More players detected!");
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New max rounds: §b" + Main.getPlugin().maxRounds);
		}
	}

	private void newMapRotationDown() {
		if (Bukkit.getOnlinePlayers().size() < 5 && Main.getPlugin().getState() == GameState.LOBBY) {
			Main.getPlugin().maxRounds = Main.getPlugin().config.getInt("maxRounds");
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§cLess players detected!");
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New max rounds: §b" + Main.getPlugin().maxRounds);
		} else if (Bukkit.getOnlinePlayers().size() < 7 && Main.getPlugin().getState() == GameState.LOBBY) {
			Main.getPlugin().maxRounds = 10;
			Main.getPlugin().resetVoting();
			Main.getPlugin().fillMaps();
			Main.getPlugin().fillVotes();
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§aMore players detected!");
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New max rounds: §b" + Main.getPlugin().maxRounds);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6New map rotation: §bdefault maps.");
		}
	}

	private void successfullDrop(Player player, Location loc, Block block) {
		if (Main.getPlugin().whosDropping == null) {
		} else if (Main.getPlugin().whosDropping.equalsIgnoreCase(player.getName())) {
			if (loc.getY() < settings.getData().getDouble(Main.getPlugin().mapName + ".jump.y")) {
				Bukkit.getScheduler().cancelTask(dropapi.timerTask);
				dropapi.timer = 21;
			}

			if (block.getType() == Material.STATIONARY_WATER) {
				Material type = Main.getPlugin().blocks.get(player.getName());
				byte data = Main.getPlugin().blockData.get(player.getName());

				if (Main.getPlugin().getType() == GameType.Enhanced) {
					countBlocks(player, loc);
				} else {
					Main.getPlugin().increaseScore(player);
					Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
							+ player.getName() + "§a" + dropapi.pickSuccessMessage());
					onepointeight.sendActionBarText(player, "§b§l+5 §6Points");
				}
				player.playSound(loc, Sound.LEVEL_UP, 5, 1);

				statistics.points.put(player.getName(), (statistics.points.get(player.getName()) + 5));
				statistics.successDrops.put(player.getName(), statistics.successDrops.get(player.getName()) + 1);
				dropapi.launchFirework("success", loc);
				FallingBlock fallingBlock = block.getWorld().spawnFallingBlock(block.getLocation().add(0, 2, 0),
						type, data);
				fallingBlock.setDropItem(false);
				dropapi.finishDrop(player);
				dropapi.setupNextTurn();
			}
		}
	}

	private void failedDrop(Player player, Location loc, EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL || event.getCause() == DamageCause.VOID
				|| event.getCause() == DamageCause.LIGHTNING) {
			Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (Main.getPlugin().getState() == GameState.INGAME) {
				if (block.getType() != Material.STATIONARY_WATER || block.getType() != Material.WATER) {
					if (Main.getPlugin().whosDropping == null) {
					} else if (Main.getPlugin().whosDropping.equalsIgnoreCase(player.getName())) {
						event.setCancelled(true);

						if (event.getCause() == DamageCause.LIGHTNING) {
							player.setFireTicks(0);
						}

						dropapi.launchFirework("fail", loc);
						Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
								+ player.getName() + "§c" + dropapi.pickFailMessage());

						if (Main.getPlugin().getType() == GameType.Elimination) {
							dropapi.eliminatePlayer(player);
						}
						player.playSound(loc, Sound.HORSE_DEATH, 5, 1);
						Bukkit.getScheduler().cancelTask(dropapi.timerTask);
						dropapi.timer = 21;
						dropapi.finishDrop(player);
						//api.grantAchievement(player, Achievement.FIRST_LAND_FAIL);
						statistics.failedDrops.put(player.getName(), statistics.failedDrops.get(player.getName()) + 1);
						dropapi.setupNextTurn();
					}
				}

				if (event.getCause() == DamageCause.FALL) {
					event.setCancelled(true);
				}
			}
		}
	}

	private void countBlocks(Player player, Location loc) {
		int count = 0;
		BlockFace[] faces = new BlockFace[] { BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH };

		for (BlockFace bf : faces) {
			Location block1 = loc.getBlock().getRelative(bf).getLocation();

			if (Main.getPlugin().mapName == "Chamber") {
				if (block1.getBlock().getType() != Material.STATIONARY_WATER
						&& block1.getBlock().getType() != Material.OBSIDIAN) {
					count++;
				}
			} else {
				if (block1.getBlock().getType() != Material.STATIONARY_WATER
						&& block1.getBlock().getType() != Material.COAL_BLOCK) {
					count++;
				}
			}
		}

		switch (count) {
		case 1:
			Main.getPlugin().updateScore(player, 2);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
					+ player.getName() + " §alanded in the water and earned §b§l1 Bonus Points.");
			onepointeight.sendActionBarText(player, Main.getPlugin().prefix + "§b§l+6 §6points!");
			StatisticsManager.getInstance().points.put(player.getName(), (StatisticsManager.getInstance().points.get(player.getName()) + 6));
			break;
		case 2:
			Main.getPlugin().updateScore(player, 3);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
					+ player.getName() + " §alanded in the water and earned §b§l2 Bonus Points.");
			onepointeight.sendActionBarText(player, Main.getPlugin().prefix + "§b§l+7 §6points!");
			StatisticsManager.getInstance().points.put(player.getName(), (StatisticsManager.getInstance().points.get(player.getName()) + 7));
			break;
		case 3:
			Main.getPlugin().updateScore(player, 4);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
					+ player.getName() + " §alanded in the water and earned §b§l3 Bonus Points.");
			onepointeight.sendActionBarText(player, Main.getPlugin().prefix + "§b§l+8 §6points!");
			StatisticsManager.getInstance().points.put(player.getName(), (StatisticsManager.getInstance().points.get(player.getName()) + 8));
			break;
		case 4:
			Main.getPlugin().updateScore(player, 5);
			Bukkit.broadcastMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix()
					+ player.getName() + " §alanded in the water and earned §b§l4 Bonus Points.");
			onepointeight.sendActionBarText(player, Main.getPlugin().prefix + "§b§l+9 §6points!");
			StatisticsManager.getInstance().points.put(player.getName(), (StatisticsManager.getInstance().points.get(player.getName()) + 9));
			break;
		default:
			dropapi.pickSuccessMessage();
			onepointeight.sendActionBarText(player, Main.getPlugin().prefix + "§b§l+5 §6points!");
			Main.getPlugin().increaseScore(player);
			StatisticsManager.getInstance().points.put(player.getName(), (StatisticsManager.getInstance().points.get(player.getName()) + 5));
			break;
		}
	}

	private void givePlayerItems(Player player) {
		ItemStack clay = new ItemStack(Material.STAINED_CLAY, 1, (short) new Random().nextInt(15));
		ItemMeta claymeta = clay.getItemMeta();
		claymeta.setDisplayName("§bBlock §cChooser");
		clay.setItemMeta(claymeta);
		player.getInventory().setItem(0, clay);

		/*ItemStack achieve = new ItemStack(Material.BEACON, 1);
		ItemMeta achievemeta = achieve.getItemMeta();
		achievemeta.setDisplayName("§aAchievement §6Menu");
		achieve.setItemMeta(achievemeta);
		player.getInventory().setItem(4, achieve);*/

		ItemStack quartz = new ItemStack(Material.QUARTZ, 1);
		ItemMeta quartzmeta = quartz.getItemMeta();
		quartzmeta.setDisplayName("§6Return to Hub");
		quartz.setItemMeta(quartzmeta);
		player.getInventory().setItem(8, quartz);
	}

	private void reducedTimeBroadcast() {
		if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().neededToStart
				&& Main.getPlugin().getState() == GameState.LOBBY) {
			if (!Main.getPlugin().shortened) {
				Main.getPlugin().shortened = true;
				LobbyTimer.lobbyTimer = 46;
				for (Player all : Bukkit.getOnlinePlayers()) {
					all.sendMessage(Main.getPlugin().prefix + "§6We have all the droppers we need!");
					all.sendMessage(Main.getPlugin().prefix + "§6Shortening timer to "
							+ (LobbyTimer.lobbyTimer - 1) + " seconds..");
				}

				Main.getPlugin().voting = true;
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§aVoting is now enabled!");
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6Use /vote or /v to vote.");
			}
		}
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		if (Main.getPlugin().getState() == GameState.LOBBY) {
			event.setFormat("§e" + StatisticsManager.getInstance().chatPoints.get(player.getName()) + " §8\u2759 "
					+ statistics.getChatRank(player) + " "
					+ Main.getPlugin().board.getPlayerTeam(player).getPrefix() + "%s" + ChatColor.DARK_GRAY
					+ " » " + ChatColor.WHITE + "%s");
		} else {
			if (!dropapi.eliminated.contains(player.getName())) {
				event.setFormat(statistics.getChatRank(player) + " " + board.getPlayerTeam(player).getPrefix() + "%s" + ChatColor.DARK_GRAY + " » "
						+ ChatColor.WHITE + "%s");
			} else {
				event.setFormat("§cEliminated §8\u2759 " + statistics.getChatRank(player) + " " + board.getPlayerTeam(player).getPrefix() + "%s"
						+ ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%s");
			}
		}

	}

	@EventHandler
	public void onCmd(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage().toLowerCase();
		switch (message.toLowerCase()) {
		case "/list":
			if (Main.getPlugin().getState() == GameState.INGAME) {
				String players = "";
				for (Player all : Bukkit.getOnlinePlayers()) {
					String pl = board.getPlayerTeam(player).getPrefix() + all.getName();

					if (Main.getPlugin().whosDropping.equalsIgnoreCase(all.getName())) {
						pl = board.getPlayerTeam(player).getPrefix() + all.getName() + " §d(Currently Dropping)";
					}
					if (players.isEmpty()) {
						players = pl;
					} else {
						pl += ", ";
						players += pl;
					}
				}
				event.setCancelled(true);
				player.sendMessage(Main.getPlugin().prefix + "§3Currently online:");
				player.sendMessage(Main.getPlugin().prefix + players);
			} else {
				String players = "";
				for (Player all : Bukkit.getOnlinePlayers()) {
					String pl = board.getPlayerTeam(player).getPrefix() + all.getName();

					if (players.isEmpty()) {
						players = pl + ".";
					} else {
						pl += ", ";
						players += pl;
					}
				}
				event.setCancelled(true);
				player.sendMessage(Main.getPlugin().prefix + "§3Currently online:");
				player.sendMessage(Main.getPlugin().prefix + players);
			}
			break;
		}
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		if (Main.getPlugin().getState() == GameState.LOBBY) {
			if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().maxPlayers) {
				event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The game is full! Sorry :(");
			} else {
				event.allow();
			}
		} else {
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "The game has started. Please come back later.");
		}
	}

	@EventHandler
	public void onFoodLevel(FoodLevelChangeEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemThrow(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemThrow(PlayerPickupItemEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		Main.getPlugin().registerPlayerTeam(player);
		Main.getPlugin().registerPlayerOnScoreboard(player);
		event.setJoinMessage(Main.getPlugin().prefix + board.getPlayerTeam(player).getPrefix() + player.getName()
				+ " §6has joined the game");

		onepointeight.sendTitleAndSubtitle(player, "§6Welcome to §6MineDrop!", "§bBrought to you by SurvivalMC",
				40, 80, 40);

		player.teleport(new Location(Bukkit.getWorld("world"), -1386.5, 10, 941.5, 0, 0));

		givePlayerItems(player);
		
		StatisticsManager.getInstance().chatPoints.put(player.getName(), statistics.getPoints(player));

		reducedTimeBroadcast();

		newMapRotationUp();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		event.setQuitMessage(Main.getPlugin().prefix + "§6Player §6" + player.getName() + " §6has left us!");
		pl.removeFromArrayLists(player);
		Main.getPlugin().removePlayerFromScoreboard(player);

		if (Bukkit.getOnlinePlayers().size() < Main.getPlugin().config.getInt("neededToStart")) {
			Main.getPlugin().voting = false;
		}

		if (Main.getPlugin().getState() == GameState.INGAME) {
			statistics.addPoints(player, StatisticsManager.getInstance().points.get(player.getName()));
		}

		newMapRotationDown();
	}

	@EventHandler
	public void onWeather(WeatherChangeEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onRedstone(BlockRedstoneEvent e) {
		if (e.getBlock().getType() == Material.REDSTONE_LAMP_ON) {
			e.setNewCurrent(100);
		}
	}

	@EventHandler
	public void onBlock(BlockIgniteEvent event) {
		if (event.getCause() == IgniteCause.LIGHTNING) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		Block block = loc.getWorld().getBlockAt(loc);
		if (block.getType() != Material.AIR) {
			if (Main.getPlugin().getState() == GameState.INGAME) {
				successfullDrop(player, loc, block);
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
			ItemStack mat = event.getItem();

			if (mat == null) {
				if (!player.hasPermission("srv.build")) {
					event.setCancelled(true);
				} else {
					event.setCancelled(false);
				}
				return;
			}

			switch (mat.getType()) {
			case STAINED_CLAY:
				if (Main.getPlugin().getState() == GameState.INGAME) {
					player.sendMessage(Main.getPlugin().prefix + "§cYou can't change your block ingame silly!");
				} else {
					player.openInventory(BlockChooserGUI.getInventory(player));
				}
				break;
			case BEACON:
				player.sendMessage(Main.getPlugin().prefix
						+ "§cAchievements aren't enabled at the moment due to certain reasons beyond our control. Please check back later.");
				break;
			case QUARTZ:
				ByteArrayDataOutput quartzout = ByteStreams.newDataOutput();
				quartzout.writeUTF("Connect");
				quartzout.writeUTF("hub");
				player.sendPluginMessage(Main.getPlugin(), "BungeeCord", quartzout.toByteArray());
				break;
			default:
				return;
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.PHYSICAL) {
			event.setCancelled(true);
			return;
		}
	}

	@EventHandler
	public void onBlock(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("srv.build")) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onBlock(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("srv.build")) {
			event.setCancelled(true);
		} else {
			event.setCancelled(false);
		}
	}

	@EventHandler
	public void onInvClick(InventoryClickEvent event) {
		if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
			event.setCancelled(!(event.getWhoClicked().getGameMode() == GameMode.CREATIVE));
		} else if (event.getInventory() instanceof PlayerInventory) {
			event.setCancelled(!(event.getWhoClicked().getGameMode() == GameMode.CREATIVE));
		} else {
			Player player = (Player) event.getWhoClicked();
			ItemStack clicked = event.getCurrentItem();
			Inventory inventory = event.getInventory();
			if (inventory.getName().equals(BlockChooserGUI.getInventory(player).getName())) {
				if (clicked.getType() != Material.AIR) {
					event.setCancelled(true);
					Material material = clicked.getType();
					byte data = 0;
					if (clicked.getData().getData() != 0) {
						data = clicked.getData().getData();
					}
					player.closeInventory();

					if (inventory.contains(clicked)) {
						if (player.getItemInHand().getType() == Material.STAINED_CLAY
								&& !player.getItemInHand().containsEnchantment(Enchantment.DURABILITY)) {
							player.getItemInHand().getItemMeta().addEnchant(Enchantment.DURABILITY, 1, true);
						}
						Main.getPlugin().blocks.put(player.getName(), material);
						Main.getPlugin().blockData.put(player.getName(), data);
						//api.grantAchievement(player, Achievement.PICKBLOCK);
						player.sendMessage(Main.getPlugin().prefix + "§6Block chosen.");
					}
				}
			} else if (inventory.getName().equals(Amenu.getInventory(player).getName())) {
				event.setCancelled(true);
				if (clicked == null) {
					player.closeInventory();
				} else if (clicked.getType() == Material.AIR) {
					player.closeInventory();
				}
			}
		}

	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		Player player = null;
		Location loc = null;
		if (event.getEntity() instanceof Player) {
			player = (Player) event.getEntity();
			loc = player.getLocation();
		}

		failedDrop(player, loc, event);
	}

}
