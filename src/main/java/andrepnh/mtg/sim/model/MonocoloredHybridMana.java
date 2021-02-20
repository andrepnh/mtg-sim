package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.model.Mana.B;
import static andrepnh.mtg.sim.model.Mana.G;
import static andrepnh.mtg.sim.model.Mana.R;
import static andrepnh.mtg.sim.model.Mana.U;
import static andrepnh.mtg.sim.model.Mana.W;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * For hybrid mana symbols
 */
public enum MonocoloredHybridMana implements HybridMana<Integer> {
  _2W(W), _2U(U), _2B(B), _2R(R), _2G(G);

  private final int option1;
  private final Mana option2;

  MonocoloredHybridMana(Mana option) {
    this.option1 = 2;
    this.option2 = option;
  }

  public static MonocoloredHybridMana parse(String raw) {
    checkArgument(!checkNotNull(raw).trim().isEmpty());
    switch (raw.charAt(1)) {
      case 'W': return _2W;
      case 'U': return _2U;
      case 'B': return _2B;
      case 'R': return _2R;
      case 'G': return _2G;
      default: throw new IllegalArgumentException("Invalid hybrid mana cost: " + raw);
    }
  }

  @Override
  public Integer option1() {
    return option1;
  }

  @Override
  public Mana option2() {
    return option2;
  }

  @Override
  public String toString() {
    return name().charAt(1) + "/" + name().charAt(2);
  }
}
