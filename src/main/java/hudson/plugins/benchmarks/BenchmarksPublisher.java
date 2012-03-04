package hudson.plugins.benchmarks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BenchmarksPublisher extends Recorder {
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public String getDisplayName() {
            return Messages.Publisher_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/benchmarks/help.html";
        }

        public List<BenchmarksReportParserDescriptor> getParserDescriptors() {
            return BenchmarksReportParserDescriptor.all();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    private int failureThreshold = 20;

    private int unstableThreshold = 10;

    /**
     * Configured report parseres.
     */
    private List<ReportParser> parsers;

    @DataBoundConstructor
    public BenchmarksPublisher(int failureThreshold,
                               int unstableThreshold,
                               List<? extends ReportParser> parsers) {
        this.failureThreshold = failureThreshold;
        this.unstableThreshold = unstableThreshold;
        if (parsers == null)
            parsers = Collections.emptyList();
        this.parsers = new ArrayList<ReportParser>(parsers);
    }

    public static File getPerformanceReport(AbstractBuild<?, ?> build,
                                            String parserDisplayName, String performanceReportName) {
        return new File(build.getRootDir(),
                BenchmarksReportMap.getPerformanceReportFileRelativePath(
                        parserDisplayName,
                        getPerformanceReportBuildFileName(performanceReportName)));
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new BenchmarksProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public List<ReportParser> getParsers() {
        return parsers;
    }

    /**
     * <p>
     * Delete the date suffix appended to the Performance result files by the
     * Maven Performance plugin
     * </p>
     *
     * @param performanceReportWorkspaceName
     * @return the name of the BenchmarksReport in the Build
     */
    public static String getPerformanceReportBuildFileName(
            String performanceReportWorkspaceName) {
        String result = performanceReportWorkspaceName;
        if (performanceReportWorkspaceName != null) {
            Pattern p = Pattern.compile("-[0-9]*\\.xml");
            Matcher matcher = p.matcher(performanceReportWorkspaceName);
            if (matcher.find()) {
                result = matcher.replaceAll(".xml");
            }
        }
        return result;
    }

    /**
     * look for benchmarks reports based in the configured parameter includes.
     * 'includes' is - an Ant-style pattern - a list of files and folders
     * separated by the characters ;:,
     */
    protected static List<FilePath> locatePerformanceReports(FilePath workspace,
                                                             String includes) throws IOException, InterruptedException {

        // First use ant-style pattern
        try {
            FilePath[] ret = workspace.list(includes);
            if (ret.length > 0) {
                return Arrays.asList(ret);
            }
        } catch (IOException e) {
        }

        // If it fails, do a legacy search
        ArrayList<FilePath> files = new ArrayList<FilePath>();
        String parts[] = includes.split("\\s*[;:,]+\\s*");
        for (String path : parts) {
            FilePath src = workspace.child(path);
            if (src.exists()) {
                if (src.isDirectory()) {
                    files.addAll(Arrays.asList(src.list("**/*")));
                } else {
                    files.add(src);
                }
            }
        }
        return files;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        if (unstableThreshold > 0 && unstableThreshold < 100) {
            logger.println("Performance: Percentage of errors greater or equal than "
                    + unstableThreshold + "% sets the build as "
                    + Result.UNSTABLE.toString().toLowerCase());
        } else {
            logger.println("Performance: No threshold configured for making the test "
                    + Result.UNSTABLE.toString().toLowerCase());
        }
        if (failureThreshold > 0 && failureThreshold < 100) {
            logger.println("Performance: Percentage of errors greater or equal than "
                    + failureThreshold + "% sets the build as "
                    + Result.FAILURE.toString().toLowerCase());
        } else {
            logger.println("Performance: No threshold configured for making the test "
                    + Result.FAILURE.toString().toLowerCase());
        }

        // add the report to the build object.
        BenchmarksBuildAction a = new BenchmarksBuildAction(build, logger,
                parsers);
        build.addAction(a);

        for (ReportParser parser : parsers) {
            String glob = parser.glob;
            logger.println("Performance: Recording " + parser.getReportName()
                    + " reports '" + glob + "'");

            List<FilePath> files = locatePerformanceReports(build.getWorkspace(),
                    glob);

            if (files.isEmpty()) {
                if (build.getResult().isWorseThan(Result.UNSTABLE)) {
                    return true;
                }
                build.setResult(Result.FAILURE);
                logger.println("Performance: no " + parser.getReportName()
                        + " files matching '" + glob
                        + "' have been found. Has the report generated?. Setting Build to "
                        + build.getResult());
                return true;
            }

            List<File> localReports = copyReportsToMaster(build, logger, files,
                    parser.getDescriptor().getDisplayName());
            Collection<BenchmarksReport> parsedReports = parser.parse(build,
                    localReports, listener);

            // mark the build as unstable or failure depending on the outcome.
            for (BenchmarksReport r : parsedReports) {
                r.setBuildAction(a);
                Result result = Result.SUCCESS;

                //get prev build
                BenchmarksBuildAction ba = build.getPreviousBuild().getAction(BenchmarksBuildAction.class);
                if (ba != null) {
                    BenchmarksReport prevReport = ba.getBenchmarksReportMap().getPerformanceReport(
                            r.getReportFileName());
                    for (String name : r.getNames()) {
                        long prev = prevReport.get(name).getDuration();
                        long curr = r.get(name).getDuration();
                        if (prev>0 && (curr / prev - 1) * 100 > unstableThreshold) result = Result.UNSTABLE;
                        if (prev>0 && (curr / prev - 1) * 100 > failureThreshold) result = Result.FAILURE;
                    }
                }

                if (result.isWorseThan(build.getResult())) {
                    build.setResult(result);
                }
                logger.println("Benchmarks: File "
                        + r.getReportFileName()
                        + ". Build status is: "
                        + build.getResult());
            }
        }

        return true;
    }

    private List<File> copyReportsToMaster(AbstractBuild<?, ?> build,
                                           PrintStream logger, List<FilePath> files, String parserDisplayName)
            throws IOException, InterruptedException {
        List<File> localReports = new ArrayList<File>();
        for (FilePath src : files) {
            final File localReport = getPerformanceReport(build, parserDisplayName,
                    src.getName());
            if (src.isDirectory()) {
                logger.println("Performance: File '" + src.getName()
                        + "' is a directory, not a Performance Report");
                continue;
            }
            src.copyTo(new FilePath(localReport));
            localReports.add(localReport);
        }
        return localReports;
    }

    public Object readResolve() {
        // data format migration
        if (parsers == null)
            parsers = new ArrayList<ReportParser>();
        return this;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = Math.max(0, Math.min(failureThreshold, 100));
    }

    public int getUnstableThreshold() {
        return unstableThreshold;
    }

    public void setUnstableThreshold(int unstableThreshold) {
        this.unstableThreshold = Math.max(0, Math.min(unstableThreshold,100));
    }
}
