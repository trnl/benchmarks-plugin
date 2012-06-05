package hudson.plugins.benchmarks;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.plugins.benchmarks.action.BuildAction;
import hudson.plugins.benchmarks.action.ProjectAction;
import hudson.plugins.benchmarks.model.Report;
import hudson.plugins.benchmarks.parser.JSONParser;
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


    @DataBoundConstructor
    public ReportRecorder(String glob) {
        this.glob = glob;
    }

    public String getGlob() {
        return glob;
    }

    public void setGlob(String glob) {
        this.glob = glob;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new ProjectAction(project);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        PrintStream logger = listener.getLogger();

        logger.println("Benchmarks: Recording reports '" + glob + "'");
        FilePath[] files = build.getWorkspace().list(glob);

        if (files.length == 0) {
            logger.println("Benchmarks: no files matching '" + glob + "' have been found.");
        }

        ReportParser parser = new JSONParser();
        Collection<Report> reports = parser.parse(Arrays.asList(files), listener);

        BuildAction a = new BuildAction(build,reports);
        build.addAction(a);

        //TODO thresholds

        return true;
    }

}
