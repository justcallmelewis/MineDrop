package net.alextwelshie.minedrop.timers;

import java.util.Random;

import net.alextwelshie.minedrop.Main;
import net.alextwelshie.minedrop.SettingsManager;
import net.alextwelshie.minedrop.runnables.LoadWorldInRunnable;
import net.alextwelshie.minedrop.runnables.TeleportInRunnable;
import net.alextwelshie.minedrop.statistics.StatisticsManager;
import net.alextwelshie.minedrop.utils.*;
import net.alextwelshie.minedrop.voting.VoteHandler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Scoreboard;

public class LobbyTimer implements Runnable {

	public static int	lobbyTimer	= Main.getPlugin().config.getInt("lobbytimer") + 1;

	Scoreboard			board		= Bukkit.getScoreboardManager().getMainScoreboard();

	OnePointEight onepointeight = OnePointEight.getInstance();
	StatisticsManager statistics = StatisticsManager.getInstance();
	GameTypeHelp gametypehelp = GameTypeHelp.getInstance();

	@SuppressWarnings({ "unchecked", "deprecation"})
	@Override
	public void run() {
		lobbyTimer--;
		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setLevel(lobbyTimer);
		}

		if (Bukkit.getOnlinePlayers().size() <= Main.getPlugin().neededToStart) {
			if (lobbyTimer % 60 == 0 && lobbyTimer != 0) {
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6Players waiting: §b"
						+ Bukkit.getOnlinePlayers().size());
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6Players needed to start: §b"
						+ Main.getPlugin().config.getInt("neededToStart"));
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6Time till start: §b"
						+ Main.getPlugin().getFormattedTime(lobbyTimer));
				if (!Main.getPlugin().voting && !Main.getPlugin().forcevoted) {
					Bukkit.broadcastMessage("");
					Bukkit.broadcastMessage(Main.getPlugin().prefix
							+ "§cVoting will be enabled when we get enough players.");
				}
			}
		} else {
			if (lobbyTimer % 20 == 0 && lobbyTimer != 0) {
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§6Time till start: §b"
						+ Main.getPlugin().getFormattedTime(lobbyTimer));
				for (Player all : Bukkit.getOnlinePlayers()) {
					VoteHandler.getInstance().sendVotingMessage(all);
				}
			}
		}

		switch (lobbyTimer) {
		case 20:
			if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().neededToStart) {
				for (Player all : Bukkit.getOnlinePlayers()) {
					onepointeight.sendTitleAndSubtitle(all, "§bChoose your block!",
							"§620 seconds remaining..", 15, 80, 15);
				}
			}
			break;
		case 10:
			String displayName = SettingsManager.getInstance().getData()
					.getString(Main.getPlugin().mapName + ".displayName");

			if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().neededToStart) {
				Main.getPlugin().voting = false;
				VoteHandler.getInstance().pickMap();
				Main.getPlugin().displayName = SettingsManager.getInstance().getData()
						.getString(Main.getPlugin().mapName + ".displayName");
				
				Main.getPlugin().setType(
						GameType.valueOf(VoteHandler.getInstance().mapGametype.get(Main.getPlugin().mapName)));
				Bukkit.getScheduler().callSyncMethod(Main.getPlugin(), new LoadWorldInRunnable());
				Bukkit.broadcastMessage(Main.getPlugin().prefix + "§eVoting has ended! §aThe map §b" + displayName
						+ " §ahas won!");
				DropAPI.getInstance().broadcastMapData();
			}
			break;
		case 5:
			gametypehelp.getHelp(Main.getPlugin().getType());
			break;
		case 0:
			DropAPI dropapi = DropAPI.getInstance();
			if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().neededToStart) {
				Main.getPlugin().setState(GameState.INGAME);
				lobbyTimer = 999;
				Main.getPlugin().board.getObjective("scoreboard").setDisplaySlot(DisplaySlot.SIDEBAR);
				Main.getPlugin().began = true;

				board.getObjective("scoreboard").setDisplayName("§6#1 §7" + Main.getPlugin().displayName);

				for (Player all : Bukkit.getOnlinePlayers()) {
					statistics.addGamePlayed(all);
					statistics.points.put(all.getName(), 0);
					statistics.failedDrops.put(all.getName(), 0);
					statistics.successDrops.put(all.getName(), 0);
					onepointeight.sendTitle(all, "§aHere.. §bwe.. §cgo!");
					all.getInventory().clear();
					if (!Main.getPlugin().blocks.containsKey(all.getName())
							&& !Main.getPlugin().blockData.containsKey(all.getName())) {
						byte random = (byte) (new Random().nextInt(14) + 1);
						Main.getPlugin().blocks.put(all.getName(), Material.STAINED_CLAY);
						Main.getPlugin().blockData.put(all.getName(), random);
					}

					dropapi.notHadTurn.add(all.getName());

					if (Main.getPlugin().blockData.get(all.getName()) == 0) {
						Material material = Main.getPlugin().blocks.get(all.getName());
						Byte data = 0;

						all.getInventory().setHelmet(new ItemStack(material, 1, data));
					} else {
						Material material = Main.getPlugin().blocks.get(all.getName());
						Byte data = Main.getPlugin().blockData.get(all.getName());

						all.getInventory().setHelmet(new ItemStack(material, 1, data));
					}
				}

				Bukkit.getScheduler().scheduleAsyncDelayedTask(Main.getPlugin(), new Runnable() {
					@Override
					public void run() {
						Player player = Bukkit.getPlayerExact(dropapi.notHadTurn.get(Main.getPlugin().turns));
						dropapi.setupPlayer(player);
					}
				}, 120L);
				Bukkit.getScheduler().callSyncMethod(Main.getPlugin(), new TeleportInRunnable());
				Bukkit.getScheduler().cancelTask(Main.getPlugin().lobbyTimer);

			} else {
				lobbyTimer = Main.getPlugin().config.getInt("lobbytimer");
			}
			break;
		}

	}

}
