package hudson.plugins.benchmarks;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.util.StreamTaskListener;
import org.kohsuke.stapler.StaplerProxy;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BenchmarksBuildAction implements Action, StaplerProxy {
    private final AbstractBuild<?, ?> build;

    /**
     * Configured parsers used to parse reports in this build.
     * For compatibility reasons, this can be null.
     */
    private final List<ReportParser> parsers;

    private transient final PrintStream hudsonConsoleWriter;

    private transient WeakReference<BenchmarksReportMap> performanceReportMap;

    private static final Logger logger = Logger.getLogger(BenchmarksBuildAction.class.getName());

    public BenchmarksBuildAction(AbstractBuild<?, ?> pBuild, PrintStream logger,
                                 List<ReportParser> parsers) {
        build = pBuild;
        hudsonConsoleWriter = logger;
        this.parsers = parsers;
    }

    public ReportParser getParserByDisplayName(String displayName) {
        if (parsers != null)
            for (ReportParser parser : parsers)
                if (parser.getDescriptor().getDisplayName().equals(displayName))
                    return parser;
        return null;
    }

    public String getDisplayName() {
        return "Benchmarks";
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return "benchmarks";
    }

    public BenchmarksReportMap getTarget() {
        return getBenchmarksReportMap();
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    PrintStream getHudsonConsoleWriter() {
        return hudsonConsoleWriter;
    }

    public BenchmarksReportMap getBenchmarksReportMap() {
        BenchmarksReportMap reportMap = null;
        WeakReference<BenchmarksReportMap> wr = this.performanceReportMap;
        if (wr != null) {
            reportMap = wr.get();
            if (reportMap != null)
                return reportMap;
        }

        try {
            reportMap = new BenchmarksReportMap(this, new StreamTaskListener(
                    System.err));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating new BenchmarksReportMap()", e);
        }
        this.performanceReportMap = new WeakReference<BenchmarksReportMap>(
                reportMap);
        return reportMap;
    }

    public void setPerformanceReportMap(
            WeakReference<BenchmarksReportMap> performanceReportMap) {
        this.performanceReportMap = performanceReportMap;
    }
}
