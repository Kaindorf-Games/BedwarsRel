package at.kaindorf.games.tournament;

import at.kaindorf.games.BedwarsRel;
import at.kaindorf.games.exceptions.TournamentEntityExistsException;
import at.kaindorf.games.tournament.models.TourneyGroup;
import at.kaindorf.games.tournament.models.TourneyPlayer;
import at.kaindorf.games.tournament.models.TourneyTeam;
import at.kaindorf.games.tournament.rounds.GroupStage;
import at.kaindorf.games.tournament.rounds.KoStage;
import at.kaindorf.games.utils.*;
import lombok.Data;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Data
public class Tournament {
  private static Tournament instance;

  private KoStage koStage;
  private GroupStage groupStage;

  private boolean softStop, hardStop;
  private int qualifiedForNextRound;
  private boolean rematchKo, rematchFinal;

  public static Tournament getInstance() {
    if (instance == null) {
      instance = new Tournament();
    }
    return instance;
  }

  private List<TourneyGroup> groups;
  private List<TourneyTeam> teams;
  private List<TourneyPlayer> players;

  private Tournament() {
    this.groups = new LinkedList<>();
    this.teams = new ArrayList<>();
    this.players = new ArrayList<>();

    loadSaves();
  }

  public void loadSaves() {
    Bukkit.getLogger().info("Loading Tournament Saves ...");
    try {
      // load Groups
      for (String group : Loader.loadSavedGroups()) {
        addGroup(group);
      }

      // load Teams
      for (Pair<String, String> p : Loader.loadSavedTeams()) {
        addTeam(p.getFirst(), p.getSecond());
      }

      // load Player
      for (Pair<TourneyPlayer, String> p : Loader.loadSavedPlayers()) {
        TourneyPlayer player = p.getFirst();
        addPlayer(player.getUuid(), p.getSecond(), player.getKills(), player.getDestroyedBeds());
      }
    } catch (TournamentEntityExistsException e) {
      Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
      this.clear();
    }

  }

  public void addGroup(String name) throws TournamentEntityExistsException {
    Optional<TourneyGroup> optional = groups.stream().filter(g -> g.getName().equals(name)).findFirst();
    if (!optional.isPresent()) {
      groups.add(new TourneyGroup(name));
    }
  }

  public void addTeam(String name, String groupName) throws TournamentEntityExistsException {
    Optional<TourneyTeam> optional = teams.stream().filter(t -> t.getName().equals(name)).findFirst();
    if (optional.isPresent()) {
      throw new TournamentEntityExistsException("Team " + name + " exists already");
    }
    TourneyTeam team = new TourneyTeam(name);
    teams.add(team);
    this.getGroup(groupName).addTeam(team);
  }

  public void addPlayer(String uuid, String teamName) throws TournamentEntityExistsException {
    addPlayer(uuid, teamName, 0, 0);
  }

  public void addPlayer(String uuid, String teamName, int kills, int destroyedBeds) throws TournamentEntityExistsException {
    Optional<TourneyPlayer> optional = players.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();
    if (optional.isPresent()) {
      throw new TournamentEntityExistsException("Player " + uuid + " exists already");
    }
    String username = UsernameFetcher.getUsernameFromUUID(uuid);
    TourneyPlayer player = new TourneyPlayer(uuid, username, kills, destroyedBeds);
    players.add(player);
    this.getTeam(teamName).addPlayer(player);
  }


  private TourneyGroup getGroup(String name) {
    return groups.stream().filter(g -> g.getName().equals(name)).findFirst().get();
  }

  private TourneyTeam getTeam(String name) {
    return teams.stream().filter(t -> t.getName().equals(name)).findFirst().get();
  }

  public void clear() {
    this.players.clear();
    this.teams.clear();
    this.groups.clear();
  }

  public void show() {
    String gs = groups.stream().map(TourneyGroup::getName).reduce((g1, g2) -> g1 + ", " + g2).orElse("-");
    String ts = teams.stream().map(TourneyTeam::getName).reduce((g1, g2) -> g1 + ", " + g2).orElse("-");
    String ps = players.stream().map(TourneyPlayer::getUsername).reduce((g1, g2) -> g1 + ", " + g2).orElse("-");

    Bukkit.getLogger().info("Groups: " + gs);
    Bukkit.getLogger().info("Teams: " + ts);
    Bukkit.getLogger().info("Players: " + ps);
  }

  @SneakyThrows
  public void save() {
    Saver.clear();
    Saver.saveGroups(this.groups);
    Saver.saveTeams(this.teams);
    Saver.savePlayers(this.players);
  }


  /*TODO: Generates Matches automatically. At the moment he gets the Matches from a File*/
  public boolean generateGroupMatches() {
    groupStage = new GroupStage();
    return groupStage.readGroupStageFromFile();
  }

  public void generateKoMatches(List<TourneyTeam> teams, int qualifiedForNextKoRound, boolean rematch, boolean rematchFinal) {
    koStage = new KoStage(qualifiedForNextKoRound, rematch, rematchFinal);

    koStage.generateKoStage(teams);
  }

  public void clearRunningTournament() {
    this.koStage = null;
    this.groupStage = null;
    if(BedwarsRel.getInstance().getGameLoopTask() != null) {
      BedwarsRel.getInstance().setGameLoopTask(null);
    }
  }

  public void announceWinner(TourneyTeam team) {
    BedwarsRel.getInstance().getGameLoopTask().cancel();
    BedwarsRel.getInstance().setGameLoopTask(null);

    try {
      for (Player p : Bukkit.getServer().getOnlinePlayers()) {

        Class<?> clazz = Class.forName("at.kaindorf.games.com."
            + BedwarsRel.getInstance().getCurrentVersion().toLowerCase() + ".Title");
        Method showTitle = clazz
            .getDeclaredMethod("showTitle", Player.class, String.class,
                double.class, double.class, double.class);

        double titleFadeIn =
            BedwarsRel.getInstance().getConfig()
                .getDouble("titles.win.title-fade-in", 1.5);
        double titleStay =
            BedwarsRel.getInstance().getConfig().getDouble("titles.win.title-stay", 5.0);
        double titleFadeOut =
            BedwarsRel
                .getInstance().getConfig().getDouble("titles.win.title-fade-out", 2.0);

        showTitle.invoke(null, p, ChatColor.GOLD+team.getName() + " has won", titleFadeIn, titleStay, titleFadeOut);
      }
    } catch (Exception e) {
      Bukkit.getLogger().log(Level.SEVERE, e.getMessage());
    }
    Bukkit.getLogger().info("We have Winner!!!");
  }

  public boolean isTournamentRunning() {
    return BedwarsRel.getInstance().getGameLoopTask() != null;
  }

  public void identifyPlayers() {
    players.stream().filter(p -> p.getPlayer() == null).forEach(TourneyPlayer::initPlayer);
  }

  public TourneyTeam getTeamOfPlayer(Player player) {
    return teams.stream().filter(t -> t.getPlayers().stream().anyMatch(p ->p.getPlayer().equals(player))).findFirst().orElse(null);
  }

  public Optional<TourneyTeam> getTourneyTeamOfPlayer(Player player) {
    return teams.stream().filter(t -> t.getPlayers().stream().map(TourneyPlayer::getPlayer).anyMatch(p -> p == player)).findFirst();
  }

  private void saveCurrentState() {
    if(!groupStage.isFinished()) {
      Saver.saveCurrentState(CurrentState.GROUP_STAGE,
          groupStage.getMatchesToDo().stream().map(t->(TourneyMatch)t).collect(Collectors.toList()),
          groupStage.getMatchesDone().stream().map(t->(TourneyMatch)t).collect(Collectors.toList()),
          qualifiedTeams, rematchKo, rematchFinal, teams, groups);
    } else if(!koStage.isFinished()) {
      Saver.saveCurrentState(CurrentState.KO_STAGE,
          koStage.currentKoRound().getMatchesTodo().stream().map(t->(TourneyMatch)t).collect(Collectors.toList()),
          koStage.currentKoRound().getMatchesDone().stream().map(t->(TourneyMatch)t).collect(Collectors.toList()),
          qualifiedTeams, rematchKo, rematchFinal, teams, null);
    }
  }

  public void cancel() {
    saveCurrentState();

    BedwarsRel.getInstance().getGameLoopTask().cancel();
    BedwarsRel.getInstance().setGameLoopTask(null);
  }
}
