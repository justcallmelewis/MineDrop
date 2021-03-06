package net.alextwelshie.minedrop.commands;

import net.alextwelshie.minedrop.Main;
import net.alextwelshie.minedrop.timers.LobbyTimer;
import net.alextwelshie.minedrop.utils.DropAPI;
import net.alextwelshie.minedrop.utils.GameState;
import net.alextwelshie.minedrop.utils.GameType;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetConfig implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (Main.getPlugin().isStaff(player)) {
				switch (args.length) {
				case 0:
				case 1:
					player.sendMessage(Main.getPlugin().prefix
							+ "§cIncorrect syntax. Please use: /setconfig [variable] [value]");
					break;
				case 2:
					String variable = args[0];
					Object value = args[1];

					if (variable.equalsIgnoreCase("gametype")) {
						if (Main.getPlugin().getState() == GameState.INGAME) {
							player.sendMessage(Main.getPlugin().prefix
									+ "§cSorry, but you can't change the gametype in-game.");
						} else {
							if (value.equals("Enhanced")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().setType(GameType.valueOf(value.toString()));

								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								
								Main.getPlugin().saveConfig();
							} else if (value.equals("Elimination")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().setType(GameType.valueOf(value.toString()));

								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								
								Main.getPlugin().saveConfig();
							} else if (value.equals("Normal")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().setType(GameType.valueOf(value.toString()));

								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								Main.getPlugin().saveConfig();
							} else {
								player.sendMessage(Main.getPlugin().prefix + "§cGametype does not exist.");
							}
						}
					} else if (variable.equalsIgnoreCase("maxrounds")) {
						if (Main.getPlugin().getState() == GameState.INGAME) {
							player.sendMessage(Main.getPlugin().prefix
									+ "§cSorry, but you can't change the max rounds in-game.");
						} else {
							if (value.toString().matches("[0-9]+")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().maxRounds = Integer.parseInt(value.toString());

								if (Main.getPlugin().maxRounds <= Main.getPlugin().round) {
									DropAPI.getInstance().setupNextTurn();
									player.sendMessage(Main.getPlugin().prefix
											+ "§6Due to this change, max rounds is now less than current round.");
									player.sendMessage(Main.getPlugin().prefix + "§cEnding game..");
								}
								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								
								Main.getPlugin().saveConfig();
							} else {
								player.sendMessage(Main.getPlugin().prefix + "§cValue is not numeric.");
							}
						}
					} else if (variable.equalsIgnoreCase("needed")) {
						if (Main.getPlugin().getState() == GameState.INGAME) {
							player.sendMessage(Main.getPlugin().prefix
									+ "§cSorry, but you can't change the players needed to start in-game.");
						} else {
							if (value.toString().matches("[0-9]+")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().neededToStart = Integer.parseInt(value.toString());

								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								if (Bukkit.getOnlinePlayers().size() >= Main.getPlugin().neededToStart) {
									LobbyTimer.lobbyTimer = 1;
									player.sendMessage(Main.getPlugin().prefix
											+ "§6Due to this change, players needed is now less than online player count.");
									player.sendMessage(Main.getPlugin().prefix + "§aBeginning game..");
								}
								
								Main.getPlugin().saveConfig();
							} else {
								player.sendMessage(Main.getPlugin().prefix + "§cValue is not numeric.");
							}
						}
					} else if (variable.equalsIgnoreCase("maxplayers")) {
						if (Main.getPlugin().getState() == GameState.INGAME) {
							player.sendMessage(Main.getPlugin().prefix
									+ "§cSorry, but you can't change the max players in-game.");
						} else {
							if (value.toString().matches("[0-9]+")) {
								Main.getPlugin().config.set(variable, value);
								Main.getPlugin().maxPlayers = Integer.parseInt(value.toString());

								player.sendMessage(Main.getPlugin().prefix + "§6Value of " + variable
										+ " set to: " + value);
								
								Main.getPlugin().saveConfig();
							} else {
								player.sendMessage(Main.getPlugin().prefix + "§cValue is not numeric.");
							}
						}
					} else {
						player.sendMessage(Main.getPlugin().prefix + "§cVariable does not exist in config.");
					}
					break;
				}
			} else {
				player.sendMessage("§4No permission.");
			}
		} else {

		}
		return true;
	}

}
