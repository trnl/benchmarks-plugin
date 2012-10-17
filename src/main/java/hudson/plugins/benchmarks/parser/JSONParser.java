package hudson.plugins.benchmarks.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.benchmarks.Constants;
import hudson.plugins.benchmarks.model.BenchmarkResult;
import hudson.plugins.benchmarks.model.Report;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Uladzimir Mihura
 *         Date: 5/24/12
 *         Time: 6:34 PM
 */
public class JSONParser extends ReportParser {
    @Override
    public Collection<Report> parse(Collection<FilePath> files, TaskListener listener) throws IOException {
        PrintStream logger = listener.getLogger();
        Collection<Report> reports = new ArrayList<Report>();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<BenchmarkResult>>() {
        }.getType();

        for (FilePath f : files) {
            logger.printf("Parsing %s.........", f.getName());
            try {
                List<BenchmarkResult> list = gson.fromJson(new InputStreamReader(f.read()), listType);
                reports.add(new Report(f.getName(), list));
                logger.print("SUCCESS\n");
            } catch (Exception e) {
                logger.print("ERROR\n");
            }

        }
        return reports;
    }
}
