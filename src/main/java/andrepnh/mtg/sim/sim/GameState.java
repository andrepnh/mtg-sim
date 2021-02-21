package andrepnh.mtg.sim.sim;

import static andrepnh.mtg.sim.util.Util.cat;

import andrepnh.mtg.sim.model.Battlefield;
import andrepnh.mtg.sim.model.Card;
import andrepnh.mtg.sim.model.Hand;
import andrepnh.mtg.sim.model.Land;
import andrepnh.mtg.sim.model.Library;
import andrepnh.mtg.sim.model.Mana;
import andrepnh.mtg.sim.model.Spell;
import com.google.common.collect.ImmutableList;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import java.util.Collection;
import lombok.Value;

@Value
public class GameState {
  int turn;
  Hand hand;
  Library library;
  Battlefield battlefield;
  Line line;

  public static GameState theOpening(Library library) {
    Tuple2<ImmutableList<Card>, Library> postDrawn = library.draw(7);
    return new GameState(0,
        Hand.empty().add(postDrawn._1),
        postDrawn._2,
        Battlefield.empty(),
        Line.empty());
  }

  public GameState next(PlayStrategy playStrategy) {
    Tuple2<ImmutableList<Card>, Library> postDraw = library.draw(1);
    Hand newHand = hand.add(postDraw._1);
    var line = playStrategy
        .choose(newHand, battlefield.getLands());
    newHand = newHand.remove(cat(line.getLandsPlayed(), line.getSpellsPlayed()));
    return new GameState(
        this.turn + 1,
        newHand,
        postDraw._2,
        battlefield.add(line.getLandsPlayed(), line.getSpellsPlayed()),
        line);
  }

  public Event toEvent() {
    return new Event(turn, battlefield, line);
  }

  public ImmutableList<Land> getLands() {
    return battlefield.getLands();
  }

  public Collection<Card> getPlayed() {
    return line.getPlayed();
  }
}
