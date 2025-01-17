package at.kaindorf.games.commands.Bedwars;

import at.kaindorf.games.BedwarsRel;
import at.kaindorf.games.commands.BaseCommand;
import at.kaindorf.games.commands.ICommand;
import at.kaindorf.games.game.Game;
import at.kaindorf.games.utils.ChatWriter;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class SetBuilderCommand extends BaseCommand implements ICommand {

  public SetBuilderCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String builder = args.get(1).toString();

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    game.setBuilder(builder);
    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.builderset")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "builder"};
  }

  @Override
  public String getCommand() {
    return "setbuilder";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.setbuilder.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.setbuilder.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
