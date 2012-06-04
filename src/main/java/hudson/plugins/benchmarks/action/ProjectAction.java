package hudson.plugins.benchmarks.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.plugins.benchmarks.model.Benchmark;
import hudson.plugins.benchmarks.model.Report;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.*;

public final class ProjectAction implements Action {

    public final AbstractProject<?, ?> project;

    public ProjectAction(AbstractProject project) {
        this.project = project;
    }

    public AbstractProject<?, ?> getProject() {
        return project;
    }

    public String getDisplayName() {
        return "Benchmarks";
    }

    public String getIconFileName() {
        return "/plugin/benchmarks/img/chart.png";
    }

    public String getUrlName() {
        return "benchmarks";
    }

    //index.jelly
    public Collection<Report> getReports(){
        Set<Report> set = new HashSet<Report>();
        for (Object o : getProject().getBuilds()) {
            AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) o;
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null) {
                set.addAll(action.getReports());
            }
        }
        return set;
    }



    //json
    public void doListReports(StaplerRequest request, StaplerResponse response) throws IOException {

        List list = new ArrayList();

        for (Object o : getProject().getBuilds()) {
            AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) o;
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null && action.getReports().size() > 0) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("build", build.getDisplayName());
                map.put("reports", action.getReports());
                list.add(map);
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        response.getWriter().write(gson.toJson(list));
    }



    public void doGetReport(StaplerRequest request, StaplerResponse response) throws IOException {
        String key = request.getParameter("key");
        if (StringUtils.isBlank(key)) return;

        List<?> builds = getProject().getBuilds();
        List list = new ArrayList();

        Collections.reverse(builds);

        for (Object o : builds) {
            AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) o;
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null && action.getReport(key) != null) {
                Report r = action.getReport(key);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("build", build.getDisplayName());
                map.put("benchmarks", r.getBenchmarks());
                list.add(map);
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        response.getWriter().write(gson.toJson(list));
    }
}
