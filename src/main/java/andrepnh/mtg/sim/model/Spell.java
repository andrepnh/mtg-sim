package andrepnh.mtg.sim.model;

import lombok.Value;

@Value
public class Spell implements Card {
  String name;
  ManaCost cost;

}
