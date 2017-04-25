package info.novatec.metricscollector.github.metrics;

import java.io.StringReader;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.springframework.stereotype.Component;

import info.novatec.metricscollector.commons.RestService;
import info.novatec.metricscollector.github.GithubMetricsResult;


/**
 * Collects the number of Downloads for each artifact for each Release.
 * Stores the artifact-name with pattern: 'releaseVersion:artifact'
 */
@Component
public class ReleaseDownloads extends GithubMetricAbstract implements GithubMetric {

    public ReleaseDownloads(RestService restService, GithubMetricsResult metrics) {
        super(restService, metrics);
    }

    @Override
    public void collect() {
        SortedMap<String, Integer> allDownloads = new TreeMap<>();

        if (projectRepository == null || projectRepository.getBoolean("has_downloads")) {
            String url = BASE_URL + projectName + "/releases";
            JsonReader jsonReader = Json.createReader(new StringReader(restService.sendRequest(url).getBody()));
            JsonArray releases = jsonReader.readArray();
            releases.forEach(obj -> {
                JsonObject release = (( JsonObject ) obj);
                Map<String, Integer> releaseDownloads = release.getJsonArray("assets")
                    .stream()
                    .filter(asset -> (( JsonObject ) asset).getInt("download_count") > 0)
                    .collect(Collectors.toMap(
                        asset -> release.getString("tag_name") + ":" + (( JsonObject ) asset).getString("name"),
                        asset -> (( JsonObject ) asset).getInt("download_count")));
                allDownloads.putAll(releaseDownloads);
            });
        }
        metrics.setReleaseDownloads(allDownloads);
    }

}
