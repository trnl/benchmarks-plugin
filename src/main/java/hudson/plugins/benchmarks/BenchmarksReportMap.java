package hudson.plugins.benchmarks;

import hudson.model.AbstractBuild;
import hudson.model.ModelObject;
import hudson.model.TaskListener;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * Root object of a benchmarks report.
 */
public class BenchmarksReportMap implements ModelObject {

    /**
     * The {@link BenchmarksBuildAction} that this report belongs to.
     */
    private transient BenchmarksBuildAction buildAction;
    /**
     * {@link BenchmarksReport}s are keyed by {@link BenchmarksReport#reportFileName}
     * <p/>
     * Test names are arbitrary human-readable and URL-safe string that identifies an individual report.
     */
    private Map<String, BenchmarksReport> benchmarksReportMap = new LinkedHashMap<String, BenchmarksReport>();
    private static final String BENCHMARKS_REPORTS = "benchmarks-reports";

    /**
     * Parses the reports and build a {@link BenchmarksReportMap}.
     *
     * @throws IOException If a report fails to parse.
     */
    BenchmarksReportMap(final BenchmarksBuildAction buildAction, TaskListener listener)
            throws IOException {
        this.buildAction = buildAction;
        parseReports(getBuild(), listener, new BenchmarksReportCollector() {

            public void addAll(Collection<BenchmarksReport> reports) {
                for (BenchmarksReport r : reports) {
                    r.setBuildAction(buildAction);
                    benchmarksReportMap.put(r.getReportFileName(), r);
                }
            }
        }, null);
    }

    private void addAll(Collection<BenchmarksReport> reports) {
        for (BenchmarksReport r : reports) {
            r.setBuildAction(buildAction);
            benchmarksReportMap.put(r.getReportFileName(), r);
        }
    }

    public AbstractBuild<?, ?> getBuild() {
        return buildAction.getBuild();
    }

    BenchmarksBuildAction getBuildAction() {
        return buildAction;
    }

    public String getDisplayName() {
        return Messages.Report_DisplayName();
    }

    public List<BenchmarksReport> getBenchmarksListOrdered() {
        List<BenchmarksReport> listBenchmarks = new ArrayList<BenchmarksReport>(
                getBenchmarksReportMap().values());
        Collections.sort(listBenchmarks);
        return listBenchmarks;
    }

    public Map<String, BenchmarksReport> getBenchmarksReportMap() {
        return benchmarksReportMap;
    }

    /**
     * <p>
     * Give the Benchmarks report with the parameter for name in Bean
     * </p>
     *
     * @param benchmarksReportName
     * @return
     */
    public BenchmarksReport getBenchmarksReport(String benchmarksReportName) {
        return benchmarksReportMap.get(benchmarksReportName);
    }

    public String getUrlName() {
        return "benchmarksReportList";
    }

    void setBuildAction(BenchmarksBuildAction buildAction) {
        this.buildAction = buildAction;
    }

    public void setBenchmarksReportMap(
            Map<String, BenchmarksReport> benchmarksReportMap) {
        this.benchmarksReportMap = benchmarksReportMap;
    }

    public static String getBenchmarksReportFileRelativePath(
            String parserDisplayName, String reportFileName) {
        return getRelativePath(parserDisplayName, reportFileName);
    }

    public static String getBenchmarksReportDirRelativePath() {
        return getRelativePath();
    }

    private static String getRelativePath(String... suffixes) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(BENCHMARKS_REPORTS);
        for (String suffix : suffixes) {
            sb.append(File.separator).append(suffix);
        }
        return sb.toString();
    }

    /**
     * <p>
     * Verify if the BenchmarksReport exist the benchmarksReportName must to be like it
     * is in the build
     * </p>
     *
     * @param benchmarksReportName
     * @return boolean
     */
    public boolean isFailed(String benchmarksReportName) {
        return getBenchmarksReport(benchmarksReportName) == null;
    }

    public void doRespondingTimeGraph(StaplerRequest request,
                                      StaplerResponse response) throws IOException {
        String parameter = request.getParameter("benchmarksReportPosition");
        AbstractBuild<?, ?> previousBuild = getBuild();
        final Map<AbstractBuild<?, ?>, Map<String, BenchmarksReport>> buildReports = new LinkedHashMap<AbstractBuild<?, ?>, Map<String, BenchmarksReport>>();
        while (previousBuild != null) {
            final AbstractBuild<?, ?> currentBuild = previousBuild;
            parseReports(currentBuild, TaskListener.NULL, new BenchmarksReportCollector() {

                public void addAll(Collection<BenchmarksReport> parse) {
                    for (BenchmarksReport benchmarksReport : parse) {
                        if (buildReports.get(currentBuild) == null) {
                            Map<String, BenchmarksReport> map = new LinkedHashMap<String, BenchmarksReport>();
                            buildReports.put(currentBuild, map);
                        }
                        buildReports.get(currentBuild).put(benchmarksReport.getReportFileName(), benchmarksReport);
                    }
                }
            }, parameter);
            previousBuild = previousBuild.getPreviousBuild();
        }
        //Now we should have the data necessary to generate the graphs!
        DataSetBuilder<String, NumberOnlyBuildLabel> dataSetBuilder = new DataSetBuilder<String, NumberOnlyBuildLabel>();
        for (AbstractBuild<?, ?> currentBuild : buildReports.keySet()) {
            NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(currentBuild);
            BenchmarksReport report = buildReports.get(currentBuild).get(parameter);

            for (Benchmark b : report.getBenchmarks()) {
                dataSetBuilder.add(b.getDuration(), b.getName(), label);
            }
        }
        ChartUtil.generateGraph(request, response,
                BenchmarksProjectAction.createRespondingTimeChart(dataSetBuilder.build()), 400, 200);
    }

    private void parseReports(AbstractBuild<?, ?> build, TaskListener listener, BenchmarksReportCollector collector, final String filename) throws IOException {
        File repo = new File(build.getRootDir(),
                BenchmarksReportMap.getBenchmarksReportDirRelativePath());

        // files directly under the directory are for JMeter, for compatibility reasons.
        File[] files = repo.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return !f.isDirectory();
            }
        });
        // this may fail, if the build itself failed, we need to recover gracefully
        if (files != null) {
            addAll(new JUnitParser("").parse(build,
                    Arrays.asList(files), listener));
        }

        // otherwise subdirectory name designates the parser ID.
        File[] dirs = repo.listFiles(new FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        // this may fail, if the build itself failed, we need to recover gracefully
        if (dirs != null) {
            for (File dir : dirs) {
                ReportParser p = buildAction.getParserByDisplayName(dir.getName());
                if (p != null) {
                    File[] listFiles = dir.listFiles(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            if (filename == null) {
                                return true;
                            }
                            if (name.equals(filename)) {
                                return true;
                            }
                            return false;
                        }
                    });
                    collector.addAll(p.parse(build, Arrays.asList(listFiles), listener));
                }
            }
        }
    }

    private interface BenchmarksReportCollector {

        public void addAll(Collection<BenchmarksReport> parse);
    }
}
