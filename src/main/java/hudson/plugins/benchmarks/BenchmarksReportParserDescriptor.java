package hudson.plugins.benchmarks;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class BenchmarksReportParserDescriptor extends
    Descriptor<ReportParser> {

  /**
   * Internal unique ID that distinguishes a parser.
   */
  public final String getId() {
    return getClass().getName();
  }

  /**
   * Returns all the registered {@link BenchmarksReportParserDescriptor}s.
   */
  public static DescriptorExtensionList<ReportParser, BenchmarksReportParserDescriptor> all() {
    return Hudson.getInstance().getDescriptorList(ReportParser.class);
  }

  public static BenchmarksReportParserDescriptor getById(String id) {
    for (BenchmarksReportParserDescriptor d : all())
      if (d.getId().equals(id))
        return d;
    return null;
  }
}
