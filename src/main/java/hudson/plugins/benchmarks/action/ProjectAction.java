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
        return hasReports() ? "/plugin/benchmarks/img/chart.png" : null;
    }

    public String getUrlName() {
        return "benchmarks";
    }

    //index.jelly
    public Collection<Report> getReports() {
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

    // floatingBox.jelly
    public boolean hasReports() {
        Collection<Report> r = getReports();
        return (r != null && !r.isEmpty());
    }


    //json
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

    public void doGetAll(StaplerRequest request, StaplerResponse response) throws IOException {
        List<?> builds = getProject().getBuilds();
        List list = new ArrayList();

        Collections.reverse(builds);

        for (Object o : builds) {
            AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) o;
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null && action.getReports() != null) {
                int count = 0;
                double sum = 0;
                for (Report r : action.getReports()) {
                    count += r.getBenchmarks().size();
                    for (Benchmark b : r.getBenchmarks()) {
                        sum += b.getAverage();
                    }
                }
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("build", build.getDisplayName());
                map.put("count", count);
                map.put("sum", sum);
                list.add(map);
            }
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        response.getWriter().write(gson.toJson(list));
    }
}
