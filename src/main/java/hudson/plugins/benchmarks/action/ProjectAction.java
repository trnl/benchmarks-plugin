package hudson.plugins.benchmarks.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.benchmarks.Constants;
import hudson.plugins.benchmarks.model.BenchmarkResult;
import hudson.plugins.benchmarks.model.Report;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.*;

public final class ProjectAction implements Action {

    public final AbstractProject<?, ?> project;
    private String fieldsToVisualize;

    public ProjectAction(AbstractProject project, String fieldsToVisualize) {
        this.project = project;
        this.fieldsToVisualize = fieldsToVisualize;
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
        List list = new ArrayList();


        Run build = getProject().getBuilds().getFirstBuild();
        while (build!=null){
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null && action.getReport(key) != null) {
                Report r = action.getReport(key);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("build", build.getDisplayName());
                map.put("benchmarks", r.getBenchmarkResults());
                list.add(map);
            }
            build = build.getNextBuild();
        }

        Map<String, Object> r = new HashMap<String, Object>();
        r.put("fieldsToVisualize", StringUtils.split(fieldsToVisualize, " ,"));
        r.put("report", list);

        Gson gson = new GsonBuilder().create();
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(r));
    }

    public void doGetAll(StaplerRequest request, StaplerResponse response) throws IOException {
        List<?> builds = getProject().getBuilds();
        List list = new ArrayList();

        Run build = getProject().getBuilds().getFirstBuild();
        while (build!=null){
            BuildAction action = build.getAction(BuildAction.class);
            if (action != null && action.getReports() != null && action.getReports() != null) {
                int count = 0;
                double sum = 0;
                for (Report r : action.getReports()) {
                    count += r.getBenchmarkResults().size();
                    for (BenchmarkResult b : r.getBenchmarkResults()) {
                        if (b.containsKey(Constants.FIELD_TIME_USER) && (b.get(Constants.FIELD_TIME_USER) instanceof Double))
                            sum += (Double) b.get(Constants.FIELD_TIME_USER);
                    }
                }
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("build", build.getDisplayName());
                map.put("count", count);
                map.put("sum", sum);
                list.add(map);
            }
            build = build.getNextBuild();
        }

        Gson gson = new GsonBuilder().create();
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(list));
    }
}
