package info.novatec.metricscollector.github.collector;

import javax.json.JsonArray;

import org.springframework.stereotype.Component;

import info.novatec.metricscollector.commons.rest.RestService;
import info.novatec.metricscollector.github.Metrics;


@Component
public class ClosedIssues extends GithubBasicMetricCollector implements GithubMetricCollector {

    public ClosedIssues(RestService restService, Metrics metrics) {
        super(restService, metrics);
    }

    @Override
    public void collect() {
        String url = getBaseRequestUrl() + "/issues/events";
        JsonArray closedIssues = createJsonArray(restService.sendRequest(url).getBody());
        metrics.addMetric("closedIssues", closedIssues.size());
    }
}
