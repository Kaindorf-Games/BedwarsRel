package at.kaindorf.games.commands.Tournament;

import at.kaindorf.games.BedwarsRel;
import at.kaindorf.games.commands.BaseCommand;
import at.kaindorf.games.commands.ICommand;
import at.kaindorf.games.tournament.Tournament;
import at.kaindorf.games.tournament.models.TourneyTeam;
import at.kaindorf.games.utils.ChatWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeamPauseCommand extends BaseCommand implements ICommand {

  public TeamPauseCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("tourney." + this.getPermission())) {
      ChatWriter.wrongPermissionMessage(sender);
      return false;
    }

    Bukkit.getLogger().info(sender.getName());
    Optional<TourneyTeam> team = Optional.empty();
    String err = "";

    if (sender.hasPermission("tourney.manage") && args.size() > 0) {
      String teamName = args.stream().reduce((p1, p2) -> p1 + " " + p2).get();
      team = Tournament.getInstance().getTeamPerName(teamName);
      err = "Team name is invalid";
    } else if (!sender.hasPermission("tourney.manage") && args.size() > 0) {
      err = "you are not allowed to do that";
    } else {
      Player player = BedwarsRel.getInstance().getServer().getPlayer(sender.getName());
      team = Tournament.getInstance().getTourneyTeamOfPlayer(player);
      err = "you are not in any team";
    }

    if (!team.isPresent()) {
      sender.sendMessage(ChatColor.RED + err);
      return true;
    }

    if (pauseValue()) sender.sendMessage(ChatColor.GREEN + "The team is paused");
    else sender.sendMessage(ChatColor.GREEN + "The team is not paused anymore");

    team.get().setPaused(pauseValue());

    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[0];
  }

  @Override
  public String getCommand() {
    return "pause";
  }

  @Override
  public String getDescription() {
    return "Pauses the team";
  }

  @Override
  public String getName() {
    return "pause";
  }

  @Override
  public String getPermission() {
    return "player";
  }

  @Override
  public List<BedwarsRel.Mode> blockDuringMode() {
    return Arrays.asList(BedwarsRel.Mode.NORMAL);
  }

  protected boolean pauseValue() {
    return true;
  }
}
