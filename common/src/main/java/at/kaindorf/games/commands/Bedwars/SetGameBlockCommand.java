package at.kaindorf.games.commands.Bedwars;

import at.kaindorf.games.BedwarsRel;
import at.kaindorf.games.commands.BaseCommand;
import at.kaindorf.games.commands.ICommand;
import at.kaindorf.games.game.Game;
import at.kaindorf.games.game.GameState;
import at.kaindorf.games.utils.ChatWriter;
import at.kaindorf.games.utils.Utils;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

public class SetGameBlockCommand extends BaseCommand implements ICommand {

  public SetGameBlockCommand(BedwarsRel plugin) {
    super(plugin);
  }

  @Override
  public boolean execute(CommandSender sender, ArrayList<String> args) {
    if (!sender.hasPermission("bw." + this.getPermission())) {
      return false;
    }

    Game game = this.getPlugin().getGameManager().getGame(args.get(0));
    String material = args.get(1).toString();

    if (game == null) {
      sender.sendMessage(ChatWriter.pluginMessage(ChatColor.RED
          + BedwarsRel
          ._l(sender, "errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
      return false;
    }

    if (game.getState() == GameState.RUNNING) {
      sender.sendMessage(
          ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
              ._l(sender, "errors.notwhilegamerunning")));
      return false;
    }

    Material targetMaterial = Utils.parseMaterial(material);
    if (targetMaterial == null && !"DEFAULT".equals(material)) {
      sender
          .sendMessage(
              ChatWriter.pluginMessage(ChatColor.RED + BedwarsRel
                  ._l(sender, "errors.novalidmaterial")));
      return true;
    }

    if ("DEFAULT".equalsIgnoreCase(material)) {
      game.setTargetMaterial(null);
    } else {
      game.setTargetMaterial(targetMaterial);
    }

    sender.sendMessage(
        ChatWriter.pluginMessage(ChatColor.GREEN + BedwarsRel._l(sender, "success.materialset")));
    return true;
  }

  @Override
  public String[] getArguments() {
    return new String[]{"game", "blocktype"};
  }

  @Override
  public String getCommand() {
    return "setgameblock";
  }

  @Override
  public String getDescription() {
    return BedwarsRel._l("commands.setgameblock.desc");
  }

  @Override
  public String getName() {
    return BedwarsRel._l("commands.setgameblock.name");
  }

  @Override
  public String getPermission() {
    return "setup";
  }

}
