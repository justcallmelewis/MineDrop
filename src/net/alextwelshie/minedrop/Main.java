package net.alextwelshie.minedrop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import net.alextwelshie.minedrop.commands.*;
import net.alextwelshie.minedrop.listeners.Listeners;
import net.alextwelshie.minedrop.ranks.PlayerManager;
import net.alextwelshie.minedrop.timers.LobbyTimer;
import net.alextwelshie.minedrop.utils.BlockChooserGUI;
import net.alextwelshie.minedrop.utils.DropAPI;
import net.alextwelshie.minedrop.utils.GameState;
import net.alextwelshie.minedrop.utils.GameType;
import net.alextwelshie.minedrop.voting.VoteHandler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

@SuppressWarnings("deprecation")
public class Main extends JavaPlugin {

	Random					random			= new Random();

	public String			prefix			= "§3MineDrop §7| ";
	public Scoreboard		board;
	public int				lobbyTimer		= 23;
	public String			mapName;
	public String			displayName;
	public int				randomMap		= random.nextInt(1);
	public World			mapWorld		= null;
	public Integer			neededToStart	= null;
	public Integer			maxPlayers		= null;
	public Integer			maxVotes		= 4;
	public String			whosDropping	= null;
	public int				turns			= 0;
	public int				round			= 1;
	public Integer			maxRounds		= null;
	public boolean			began			= false;
	public boolean			ended			= false;
	public boolean			shortened		= false;
	public boolean			voting			= false;
	public boolean			forcevoted		= false;

	public Configuration	config;

	public GameState		state;
	public GameType			type;

	PlayerManager			pl				= PlayerManager.getInstance();

	public static Main getPlugin() {
		return JavaPlugin.getPlugin(Main.class);
	}

	public HashMap<String, Material>	blocks		= new HashMap<>();
	public HashMap<String, Byte>		blockData	= new HashMap<>();

	@Override
	public void onEnable() {
		setupConfig();
		setupScoreboards();
		setupMechanics();
		fillErrorMessages();
		fillSuccessMessages();
		fillBlockChooser();
		fillMaps();
		fillVotes();
		registration();
	}

	public String getFormattedTime(int seconds) {
		int remainder = seconds % 3600;
		int mins = remainder / 60;

		if (mins == 1) {
			return String.valueOf(mins) + " min";
		} else {
			return String.valueOf(mins) + " mins";
		}
	}

	private void setupScoreboards() {
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		board = manager.getMainScoreboard();
		board.registerNewObjective("scoreboard", "dummy");
	}

	public void fillMaps() {
		ArrayList<String> maps = new ArrayList<>();

		maps.add(0, "Brickwork");
		maps.add(1, "Chamber");
		maps.add(2, "AquaticDepths");
		maps.add(3, "Rainbow");
		maps.add(4, "Cake");
		maps.add(5, "HighDive");
		maps.add(6, "Icy");

		for (int i = 1; i <= maxVotes; i++) {
			if (i == 1) {
				VoteHandler.getInstance().maps.clear();
			}
			int random = new Random().nextInt((maps.size() - 1));
			VoteHandler.getInstance().maps.add(maps.get(random));

			Random randomVar = new Random();
			String gametype = "Elimination";
			switch (randomVar.nextInt(8)) {
			case 5:
			case 8:
			case 6:
				gametype = "Enhanced";
				break;
			case 2:
			case 3:
			case 7:
				gametype = "Elimination";
				break;
			case 4:
			case 1:
			case 0:
				gametype = "Normal";
				break;
			}
			VoteHandler.getInstance().mapGametype.put(maps.get(random), gametype);
			maps.remove(random);
		}
	}

	public void fillMapsLarge() {
		ArrayList<String> maps = new ArrayList<>();
		maps.add(0, "AquaticDepths");
		maps.add(1, "HighDive");
		maps.add(2, "Valley");
		maps.add(3, "Factory");

		for (int i = 0; i <= (maps.size() - 1); i++) {
			int random = i;
			if (i == 0) {
				VoteHandler.getInstance().maps.clear();
			}
			VoteHandler.getInstance().maps.add(maps.get(random));

			Random randomVar = new Random();
			String gametype = "Elimination";
			switch (randomVar.nextInt(8)) {
			case 5:
			case 8:
			case 6:
				gametype = "Enhanced";
				break;
			case 2:
			case 3:
			case 7:
				gametype = "Elimination";
				break;
			case 4:
			case 1:
			case 0:
				gametype = "Normal";
				break;
			}
			VoteHandler.getInstance().mapGametype.put(maps.get(random), gametype);
		}
	}

	public void resetVoting() {
		VoteHandler.getInstance().maps.clear();
		VoteHandler.getInstance().voted.clear();
		VoteHandler.getInstance().votes.clear();
		VoteHandler.getInstance().mapGametype.clear();
	}

	public void fillVotes() {
		for (String map : VoteHandler.getInstance().maps) {
			VoteHandler.getInstance().votes.put(map, 0);
		}
	}

	private void setupConfig() {
		config = getConfig();
		saveDefaultConfig();
	}

	public boolean isPremium(Player player) {
		return PlayerManager.getInstance().getRank(player).equalsIgnoreCase("Hive")
				|| PlayerManager.getInstance().getRank(player).equalsIgnoreCase("Special");
	}

	public boolean isStaff(Player player) {
		return PlayerManager.getInstance().getRank(player).equalsIgnoreCase("Mod")
				| PlayerManager.getInstance().getRank(player).equalsIgnoreCase("Admin")
				|| PlayerManager.getInstance().getRank(player).equalsIgnoreCase("Owner");
	}

	private String getRankTeam(Player p) {
		switch (pl.getRank(p)) {
		case "Regular":
			return "E-Regular";
		case "Special":
			return "E-Regular";
		case "Hive":
			return "D-Vip";
		case "Mod":
			return "C-Mod";
		case "Admin":
			return "B-Admin";
		case "Owner":
			return "A-Owner";
		default:
			return null;
		}
	}

	public void registerPlayerTeam(Player player) {
		board.getTeam(getRankTeam(player)).addPlayer(player);
	}

	public void registerPlayerOnScoreboard(Player player) {
		Score score = board.getObjective("scoreboard").getScore(player.getDisplayName());
		score.setScore(0);

		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setScoreboard(board);
		}
	}

	public void registerFakePlayer(String player, int initialScore) {
		Score score = board.getObjective("scoreboard").getScore(player);
		score.setScore(initialScore);

		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setScoreboard(board);
		}
	}

	public GameState getState() {
		return state;
	}

	public void setState(GameState state) {
		this.state = state;
	}

	public GameType getType() {
		return type;
	}

	public void setType(GameType type) {
		this.type = type;
	}

	public void removePlayerTeam(Player player) {
		board.getTeam(getRankTeam(player)).removePlayer(player);
	}

	public void removePlayerFromScoreboard(Player player) {
		board.resetScores(player.getDisplayName());

		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setScoreboard(board);
		}
	}

	public Integer getScore(Player player) {
		Score score = board.getObjective("scoreboard").getScore(player.getDisplayName());
		return score.getScore();
	}

	public void updateScore(Player player, int amount) {
		Score score = board.getObjective("scoreboard").getScore(player.getDisplayName());
		score.setScore(getScore(player) + amount);

		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setScoreboard(board);
		}
	}

	public void increaseScore(Player player) {
		Score score = board.getObjective("scoreboard").getScore(player.getDisplayName());
		score.setScore(getScore(player) + 1);

		for (Player all : Bukkit.getOnlinePlayers()) {
			all.setScoreboard(board);
		}
	}

	public void fillSuccessMessages() {
		DropAPI drop = DropAPI.getInstance();

		drop.successMessages.add(" landed like a cat!");
		drop.successMessages.add(" splooshed successfully into the water.");
		drop.successMessages.add(" pooped out a block. Yaay.");
		drop.successMessages.add(" wedi glanio yn y ddŵr.");
		drop.successMessages.add(" cheated.. probably.");
		drop.successMessages.add(" landed and the crowd went wild.");
		drop.successMessages.add(" just got wet.");
	}

	public void fillErrorMessages() {
		DropAPI drop = DropAPI.getInstance();

		drop.failMessages.add(" did a Sherlock Holmes.");
		drop.failMessages.add(" did the flop.");
		drop.failMessages.add(" failed to become Tom Daley.");
		drop.failMessages.add("'s face became the floor.");
		drop.failMessages.add(" suicided. Maybe on purpose?");
		drop.failMessages.add(" failed and cried like a baby.");
		drop.failMessages.add(" thought he was Buzz Lightyear.");
	}

	private void registration() {
		getCommand("forcestart").setExecutor(new ForceStart());
		getCommand("endgame").setExecutor(new EndGame());
		getCommand("setconfig").setExecutor(new SetConfig());
		getCommand("shortstart").setExecutor(new ShortStart());
		getCommand("setspawn").setExecutor(new SetSpawn());
		getCommand("vote").setExecutor(new Vote());
		getCommand("forcevote").setExecutor(new ForceVote());
		Bukkit.getPluginManager().registerEvents(new Listeners(), this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		SettingsManager.getInstance().setup(this);
	}

	private void setupMechanics() {
		lobbyTimer = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new LobbyTimer(), 0L, 20L);

		setState(GameState.LOBBY);

		int needed = config.getInt("neededToStart");
		int max = config.getInt("maxPlayers");
		int maxrounds = config.getInt("maxRounds");
		int lobbytimer = config.getInt("lobbytimer");
		String gametype = config.getString("gametype");

		this.neededToStart = needed;
		this.maxPlayers = max;
		this.maxRounds = maxrounds;
		LobbyTimer.lobbyTimer = lobbytimer + 1;

		switch (gametype) {
		case "Enhanced":
			setType(GameType.Enhanced);
			break;
		case "Normal":
			setType(GameType.Normal);
			break;
		case "Elimination":
			setType(GameType.Elimination);
			break;
		case "Auto":
			Random random = new Random();
			int Chance = random.nextInt(5);
			if (Chance == 0) {
				setType(GameType.Normal);
			} else if (Chance == 1) {
				setType(GameType.Enhanced);
			} else if (Chance == 2) {
				setType(GameType.Elimination);
			} else if (Chance == 3) {
				setType(GameType.Elimination);
			} else if (Chance == 4) {
				setType(GameType.Enhanced);
			} else if (Chance == 5) {
				setType(GameType.Normal);
			}
			break;
		}

		if (this.neededToStart == null) {
			neededToStart = 2;
		}

		if (this.maxPlayers == null) {
			maxPlayers = 16;
		}

		if (this.maxRounds == null) {
			maxRounds = 7;
		}

		if (getType() == null) {
			setType(GameType.Normal);
		}

		if (LobbyTimer.lobbyTimer == 999) {
			LobbyTimer.lobbyTimer = 181;
		}
	}

	private void fillBlockChooser() {
		for (int i = 0; i < 16; i++) {
			BlockChooserGUI.normal.put((byte) i, Material.STAINED_CLAY);
		}

		BlockChooserGUI.premium.put(Material.TNT, (byte) 0);
		BlockChooserGUI.premium.put(Material.IRON_BLOCK, (byte) 0);
		BlockChooserGUI.premium.put(Material.GOLD_BLOCK, (byte) 0);
		BlockChooserGUI.premium.put(Material.EMERALD_BLOCK, (byte) 0);
		BlockChooserGUI.premium.put(Material.DIAMOND_BLOCK, (byte) 0);
		BlockChooserGUI.premium.put(Material.PUMPKIN, (byte) 0);
		BlockChooserGUI.premium.put(Material.STONE, (byte) 0);
		BlockChooserGUI.premium.put(Material.BRICK, (byte) 0);
		BlockChooserGUI.premium.put(Material.SANDSTONE, (byte) 0);

		BlockChooserGUI.staff.put(Material.COMMAND, (byte) 0);
		BlockChooserGUI.staff.put(Material.BEACON, (byte) 0);
	}

}
