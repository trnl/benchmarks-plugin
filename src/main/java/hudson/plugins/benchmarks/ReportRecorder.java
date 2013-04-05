package hudson.plugins.benchmarks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.benchmarks.action.BuildAction;
import hudson.plugins.benchmarks.action.ProjectAction;
import hudson.plugins.benchmarks.model.BenchmarkResult;
import hudson.plugins.benchmarks.model.Change;
import hudson.plugins.benchmarks.model.Report;
import hudson.plugins.benchmarks.parser.JSONParser;
import hudson.plugins.benchmarks.parser.GitParser;
import hudson.plugins.benchmarks.parser.ReportParser;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Recorder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;

public class ReportRecorder extends Recorder {


    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Publisher> {
        @Override
        public String getDisplayName() {
            return "Publish Benchmarks reports";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/benchmarks/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    private String glob;
    private int failureThreshold = 20;
    private int unstableThreshold = 10;
    private String fieldsToVisualize = "opsUser";
    private String pathToGitChanges = ".";

    public int getUnstableThreshold() {
        return unstableThreshold;
    }

    public void setUnstableThreshold(int unstableThreshold) {
        this.unstableThreshold = unstableThreshold;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public void setFailureThreshold(int failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    public String getFieldsToVisualize() {
        return fieldsToVisualize;
    }

    public void setFieldsToVisualize(String fieldsToVisualize) {
        this.fieldsToVisualize = fieldsToVisualize;
    }

    public String getPathToGitChanges() {
        return pathToGitChanges;
    }

    public void setPathToGitChanges(String pathToGitChanges) {
        this.pathToGitChanges = pathToGitChanges;
    }

    @DataBoundConstructor

    public ReportRecorder(String glob, int failureThreshold, int unstableThreshold, String fieldsToVisualize, String pathToGitChanges) {
        this.glob = glob;
        this.failureThreshold = failureThreshold;
        this.unstableThreshold = unstableThreshold;
        this.fieldsToVisualize = fieldsToVisualize;
        this.pathToGitChanges = pathToGitChanges;
    }

    public String getGlob() {
        return glob;
    }

    public void setGlob(String glob) {
        this.glob = glob;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> project) {
        return Arrays.asList(new ProjectAction(project, fieldsToVisualize));
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();
        logger.println("\n-------------------------------------------------------");
        logger.println("BENCHMARKS PLUGIN");
        logger.println("-------------------------------------------------------");
        logger.printf("Parsing reports matching '%s' pattern.%n", glob);
        FilePath[] files = build.getWorkspace().list(glob);

        if (files.length == 0) {
            logger.printf("No reports matching '%s' pattern found.%n%n", glob);
            return true;
        }

        ReportParser parser = new JSONParser();
        Collection<Report> reports = parser.parse(Arrays.asList(files), listener);
        logger.println("Parsing finished.\n");

        GitParser git = new GitParser( build.getWorkspace().child(pathToGitChanges).toString() );
        Change change = git.getChangeHead();

        BuildAction a = new BuildAction(build, reports, change);
        build.addAction(a);

        logger.println("Analysing changes.");
        analyseChanges(build, logger, reports, change);
        logger.printf("\nResult: %s.%n%n", build.getResult());

        return true;
    }

    private void analyseChanges(AbstractBuild<?, ?> build, PrintStream logger, Collection<Report> reports, Change change) {
        Result result = Result.SUCCESS;
        AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
        BuildAction previousAction = null;
        if (previousBuild != null && (previousAction = previousBuild.getAction(BuildAction.class)) != null) {
            for (Report currentReport : reports) {
                Report previousReport = null;
                if ((previousReport = previousAction.getReport(currentReport.getKey())) != null) {
                    for (String benchmarkName : currentReport.getNames()) {
                        result = compareBenchmarks(benchmarkName, currentReport.get(benchmarkName), previousReport.get(benchmarkName), logger, change);
                    }
                }
                if (result.isWorseThan(build.getResult())) {
                    build.setResult(result);
                }
            }
        }
    }

    private Result compareBenchmarks(String benchmarkName, BenchmarkResult current, BenchmarkResult previous, PrintStream logger, Change change) {
        Result result = Result.SUCCESS;

        Double currentValue = extractDouble(current, Constants.FIELD_OPS_USER);
        Double previousValue = extractDouble(current, Constants.FIELD_OPS_USER);

        if (currentValue == null || previousValue == null) return result;

        if (previousValue > 0 && (currentValue / previousValue - 1) * 100 > unstableThreshold)
            result = Result.UNSTABLE;
        if (previousValue > 0 && (currentValue / previousValue - 1) * 100 > failureThreshold)
            result = Result.FAILURE;

        if (result.isWorseThan(Result.SUCCESS)) {
            logger.printf("%s: [result: %s, gain: %g%%]%n", benchmarkName, result, (currentValue / previousValue - 1) * 100);
            logger.printf("Commit %s [%s, %s, %s]", change.getId(), change.getAuthor(), change.getDate().toString(), change.getMessage() );
        }
        return result;
    }

    private Double extractDouble(BenchmarkResult result, String field) {
        return result.containsKey(field) && (result.get(field) instanceof Double)
                ? (Double) result.get(field)
                : null;
    }

}
