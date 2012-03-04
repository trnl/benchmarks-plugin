package hudson.plugins.benchmarks;

/**
 * @author Uladzimir Mihura
 *         Date: 3/4/12
 *         Time: 1:01 PM
 */
public class Benchmark {
    private String name;
    private String group;
    private long duration;
    private boolean successful;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
}
