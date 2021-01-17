package andrepnh.mtg.sim.analytics;

import com.google.common.base.Strings;
import java.util.List;
import java.util.stream.Collectors;

public interface Report {
  String getDataLabel();

  String getName();

  /**
   * A simple format mostly for debugging purposes
   */
  List<String> toHumanReadableText();

  default List<String> toHumanReadableText(int indentLevel) {
    String extraIndentation = Strings.repeat("  ", indentLevel);
    return toHumanReadableText()
        .stream()
        .map(line -> extraIndentation + line)
        .collect(Collectors.toList());
  }
}
