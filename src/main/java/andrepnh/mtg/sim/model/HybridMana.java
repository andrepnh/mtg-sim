package andrepnh.mtg.sim.model;

import static andrepnh.mtg.sim.model.Mana.B;
import static andrepnh.mtg.sim.model.Mana.G;
import static andrepnh.mtg.sim.model.Mana.R;
import static andrepnh.mtg.sim.model.Mana.U;
import static andrepnh.mtg.sim.model.Mana.W;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.vavr.control.Either;
import lombok.Getter;

/**
 * For hybrid mana symbols
 */
@Getter
public enum HybridMana {
  WU(W, U), WB(W, B), WR(W, R), WG(W, G),
  UB(U, B), UR(U, R), UG(U, G),
  BR(B, R), BG(B, G),
  RG(R, G),

  _2W(W), _2U(U), _2B(B), _2R(R), _2G(G);

  private final Either<Integer, Mana> option1;
  private final Mana option2;

  HybridMana(Mana option1, Mana option2) {
    this.option1 = Either.right(option1);
    this.option2 = option2;
  }

  HybridMana(Mana option) {
    this.option1 = Either.left(2);
    this.option2 = option;
  }

  public static HybridMana parse(String raw) {
    checkArgument(!checkNotNull(raw).trim().isEmpty());
    if (raw.startsWith("2")) {
      switch (raw.charAt(1)) {
        case 'W': return _2W;
        case 'U': return _2U;
        case 'B': return _2B;
        case 'R': return _2R;
        case 'G': return _2G;
        default: throw new IllegalArgumentException("Invalid hybrid mana cost: " + raw);
      }
    } else {
      return valueOf(raw);
    }
  }

  @Override
  public String toString() {
    return name().replace("_", "");
  }
}
