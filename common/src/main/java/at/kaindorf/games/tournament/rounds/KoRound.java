package at.kaindorf.games.tournament.rounds;

import at.kaindorf.games.tournament.TourneyProperties;
import at.kaindorf.games.tournament.models.*;
import at.kaindorf.games.utils.Pair;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class KoRound {
  private List<TourneyKoMatch> matchesTodo;
  private List<TourneyKoMatch> matchesDone;
  private KoRound dependsOn;
  private boolean rematch;

  public KoRound(KoRound dependsOn, boolean rematch) {
    this.matchesTodo = new LinkedList<>();
    this.matchesDone = new LinkedList<>();
    this.dependsOn = dependsOn;
    this.rematch = rematch;
  }

  public void generateMatches(List<TourneyTeam> teams) {
    for (int i = 0; i < teams.size() / 4; i++) {
      TourneyKoMatch match = new TourneyKoMatch(Arrays.asList(teams.get(4 * i), teams.get(4 * i + 1), teams.get(4 * i + 2), teams.get(4 * i + 3)));
      matchesTodo.add(match);
      if (rematch)
        matchesTodo.add(new TourneyKoMatch(teams, match));
    }

    Collections.shuffle(this.matchesTodo);
  }

  public void generateFinal(List<TourneyTeam> teams) {
    TourneyKoMatch match = new TourneyKoMatch(teams);
    matchesTodo.add(match);
    if (rematch)
      matchesTodo.add(new TourneyKoMatch(teams, match));
  }

  public void matchPlayed(TourneyKoMatch match) {
    matchesTodo.remove(match);
    matchesDone.add(match);
  }

  public boolean isFinished() {
    return matchesTodo.size() == 0;
  }

  public List<TourneyTeam> getWinnersOfCurrentKoRound(int qualifiedForNextKoRound) {
    if (!isFinished()) {
      return null;
    }
    List<Pair<Integer, TourneyTeam>> teams = new LinkedList<>();

    List<TourneyKoMatch> matches = matchesDone.stream().filter(m -> m.getTeams() != null).collect(Collectors.toList());
    for (TourneyKoMatch match : matches) {
      match.getTeams().forEach(t -> {
        int points = calculatePointsForMatchAndTeam(match, t);
        if (rematch && match.getRematch() != null) {
          TourneyKoMatch rematch = matchesDone.stream().filter(match1 -> match.getRematch() == match1).findFirst().get();
          points += calculatePointsForMatchAndTeam(rematch, t);
        }

        teams.add(new Pair<>(points * -1, t));
      });
    }
    return teams.stream().sorted(Comparator.comparingInt(Pair::getFirst)).limit(qualifiedForNextKoRound).map(Pair::getSecond).collect(Collectors.toList());
  }

  private int calculatePointsForMatchAndTeam(TourneyMatch match, TourneyTeam t) {
    Optional<TourneyGameStatistic> optional = t.getStatistics().stream().filter(st -> st.getMatch().equals(match)).findFirst();
    if (optional.isPresent()) {
      TourneyGameStatistic statistics = optional.get();
      int points = statistics.getFinalKills() * TourneyProperties.pointsForFinalKill;
      points += statistics.getDestroyedBeds() * TourneyProperties.pointsForBed;
      points += (statistics.isWin() ? 1 : 0) * TourneyProperties.pointsForBed;
      points += statistics.getExtraPoints();
      return points;
    }
    return 0;
  }

  public void addToDoMatch(TourneyKoMatch match) {
    matchesTodo.add(match);
  }

  public void addDoneMatch(TourneyKoMatch match) {
    matchesDone.add(match);
  }

  public TourneyKoMatch getMatchPerId(int id) {
    Optional<TourneyKoMatch> match = matchesTodo.stream().filter(m -> m.getId() == id).findFirst();
    if (match.isPresent()) {
      return match.get();
    }

    match = matchesDone.stream().filter(m -> m.getId() == id).findFirst();
    return match.orElse(null);

  }
}
