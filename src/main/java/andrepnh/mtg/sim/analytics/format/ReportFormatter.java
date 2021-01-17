package andrepnh.mtg.sim.analytics.format;

import java.util.List;

public interface ReportFormatter {
  String getFileExtension();

  List<String> format();
}
