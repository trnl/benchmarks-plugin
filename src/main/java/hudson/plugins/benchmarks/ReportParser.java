package hudson.plugins.benchmarks;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import hudson.model.Describable;
import hudson.model.Hudson;
import hudson.model.TaskListener;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Parses benchmarks result files into {@link hudson.plugins.benchmarks.BenchmarksReport}s.
 * This object is persisted with {@link hudson.plugins.benchmarks.BenchmarksPublisher} into the project configuration.
 *
 * <p>
 * Subtypes can define additional parser-specific parameters as instance fields.
 *
 */
public abstract class ReportParser implements
    Describable<ReportParser>, ExtensionPoint {
  /**
   * GLOB patterns that specify the benchmarks report.
   */
  public final String glob;

  @DataBoundConstructor
  protected ReportParser(String glob) {
    this.glob = (glob == null || glob.length() == 0) ? getDefaultGlobPattern()
        : glob;
  }

  public BenchmarksReportParserDescriptor getDescriptor() {
    return (BenchmarksReportParserDescriptor) Hudson.getInstance().getDescriptorOrDie(
        getClass());
  }

  /**
   * Parses the specified reports into {@link hudson.plugins.benchmarks.BenchmarksReport}s.
   */
  public abstract Collection<BenchmarksReport> parse(
      AbstractBuild<?, ?> build, Collection<File> reports, TaskListener listener)
      throws IOException;

  public abstract String getDefaultGlobPattern();

  /**
   * All registered implementations.
   */
  public static ExtensionList<ReportParser> all() {
    return Hudson.getInstance().getExtensionList(ReportParser.class);
  }

  public String getReportName() {
    return this.getClass().getName().replaceAll("^.*\\.(\\w+)Parser.*$", "$1");
  }
}
