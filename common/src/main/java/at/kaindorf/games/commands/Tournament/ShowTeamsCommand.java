package at.kaindorf.games.commands.Tournament;

import at.kaindorf.games.BedwarsRel;
import at.kaindorf.games.commands.BaseCommand;
import at.kaindorf.games.commands.ICommand;
import at.kaindorf.games.tournament.Tournament;
import at.kaindorf.games.tournament.models.CurrentState;
import at.kaindorf.games.tournament.models.TourneyPlayer;
import at.kaindorf.games.tournament.models.TourneyTeam;
import at.kaindorf.games.utils.ChatWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShowTeamsCommand extends BaseCommand implements ICommand {
  public ShowTeamsCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("tourney." + this.getPermission())) {
      ChatWriter.wrongPermissionMessage(sender);
      return false;
    }

    if (Tournament.getInstance().getTeams().size() == 0) {
      sender.sendMessage(ChatColor.RED + "No Teams found");
      return true;
    }

    int page = 1;
    if (args.size() > 0) {
      try {
        page = Integer.parseInt(args.get(0));
      } catch (NumberFormatException ex) {
        sender.sendMessage(ChatColor.RED + "Not a number");
        return false;
      }
    }

    String output = buildChatOutput();
    ChatWriter.paginateOutput(sender, output, page);

    return true;
  }

  private String buildChatOutput() {
    List<TourneyTeam> teams = Tournament.getInstance().getTeams();
    for (TourneyTeam team : teams) {
      for (TourneyPlayer player : team.getPlayers()) {
        if (player.getPlayer() == null) {
          player.initPlayer();
        }
      }
    }
    StringBuilder sb = new StringBuilder();

    for (TourneyTeam team : teams) {
      String paused = "";
      if (team.isPaused()) paused = " (paused)";

      sb.append(ChatColor.YELLOW + team.getName() + " [" + team.getShortname() + "]" + paused + "\n");
      team.getPlayers().forEach(p -> {
        sb.append(ChatColor.YELLOW + "  " + (p.getPlayer() == null ? p.getUuid() : p.getPlayer().getName()) + "\n");
      });
      sb.append(ChatColor.YELLOW + "  Points group stage: " + team.calculatePoints(CurrentState.GROUP_STAGE) + "\n");
      sb.append(ChatColor.YELLOW + "  Points ko stage: " + team.calculatePoints(CurrentState.KO_STAGE) + "\n\n");
    }
    return sb.toString();
  }

  @Override
  public String[] getArguments() {
    return new String[]{};
  }

  @Override
  public String getCommand() {
    return "showteams";
  }

  @Override
  public String getDescription() {
    return "Print all teams into the chat";
  }

  @Override
  public String getName() {
    return "Show Teams";
  }

  @Override
  public String getPermission() {
    return "player";
  }

  @Override
  public List<BedwarsRel.Mode> blockDuringMode() {
    return Arrays.asList(BedwarsRel.Mode.NORMAL);
  }
}
