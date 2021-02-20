package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.model.Mana.B;
import static andrepnh.mtg.sim.model.Mana.G;
import static andrepnh.mtg.sim.model.Mana.R;
import static andrepnh.mtg.sim.model.Mana.U;
import static andrepnh.mtg.sim.model.Mana.W;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import io.vavr.control.Either;
import lombok.Getter;

/**
 * For hybrid mana symbols
 */
public enum BicolorHybridMana implements HybridMana<Mana> {
  WU(W, U), WB(W, B), WR(W, R), WG(W, G),
  UB(U, B), UR(U, R), UG(U, G),
  BR(B, R), BG(B, G),
  RG(R, G);

  private final Mana option1;
  private final Mana option2;

  BicolorHybridMana(Mana option1, Mana option2) {
    this.option1 = option1;
    this.option2 = option2;
  }

  public static BicolorHybridMana parse(String raw) {
    checkArgument(!checkNotNull(raw).trim().isEmpty());
    return valueOf(raw);
  }

  @Override
  public Mana option1() {
    return option1;
  }

  @Override
  public Mana option2() {
    return option2;
  }

  @Override
  public String toString() {
    return name().charAt(0) + "/" + name().charAt(1);
  }
}
