package info.novatec.metricscollector.github.metrics;

import javax.json.JsonArray;

import org.springframework.stereotype.Component;

import info.novatec.metricscollector.github.RestService;
import info.novatec.metricscollector.github.GithubMetricsResult;


@Component
public class NumberOfContributors extends GithubMetricAbstract implements GithubMetric {

    public NumberOfContributors(RestService restService, GithubMetricsResult metrics) {
        super(restService, metrics);
    }

    @Override
    public void collect() {
        String url = getBaseRequestUrl() + "/contributors";
        JsonArray contributors = createJsonArray(restService.sendRequest(url).getBody());
        metrics.setContributors(contributors.size());
    }

}
