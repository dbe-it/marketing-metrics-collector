package info.novatec.metricCollector.github;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import info.novatec.metricCollector.commons.RestRequester;
import info.novatec.metricCollector.exception.UserDeniedException;


@Component
public class GithubCollector {

    private static final String BASE_URL = "https://api.github.com";

    private RestRequester restRequester;

    @Autowired
    public GithubCollector(RestRequester restRequester) {
        this.restRequester = restRequester;
    }

    GithubMetrics collect(String projectName) {

        setHeadersForGithub();
        GithubMetrics githubMetrics = new GithubMetrics();

        JSONObject projectRepository = new JSONObject(getProjectRepository(projectName).getBody());

        collectNumberOfContributor(githubMetrics, projectName);
        collectNumberOfStars(githubMetrics, projectRepository);
        collectNumberOfForks(githubMetrics, projectRepository);
        collectNumberOfWatchers(githubMetrics, projectRepository);
        collectNumberOfOpenIssues(githubMetrics, projectRepository);
        collectNumberOfCommits(githubMetrics, projectName);
        collectNumberOfUniqueVisitors(githubMetrics, projectName);
        collectReferringSitesLast14Days(githubMetrics, projectName);
        collectNumberOfDownloads(githubMetrics, projectRepository, projectName);

        return githubMetrics;
    }

    private void setHeadersForGithub() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Accept", "application/vnd.github.v3+json");
        httpHeaders.set("Authorization", "token 62eb1c5273456e8ed6fc165671fbdc2ba059db81");
        restRequester.setHeaders(httpHeaders);
    }

    private ResponseEntity<String> getProjectRepository(String projectName) {
        return restRequester.sendRequest(BASE_URL + "/repos/" + projectName);
    }

    private void collectNumberOfContributor(GithubMetrics githubMetrics, String projectName) {
        String url = BASE_URL + "/repos/" + projectName + "/contributors";
        JSONArray contributors = new JSONArray(restRequester.sendRequest(url).getBody());
        githubMetrics.setNumberOfContributors(contributors.length());
    }

    private void collectNumberOfStars(GithubMetrics githubMetrics, JSONObject projectRepository) {
        githubMetrics.setNumberOfStars(projectRepository.getInt("stargazers_count"));
    }

    private void collectNumberOfForks(GithubMetrics githubMetrics, JSONObject projectRepository) {
        githubMetrics.setNumberOfForks(projectRepository.getInt("forks_count"));
    }

    private void collectNumberOfWatchers(GithubMetrics githubMetrics, JSONObject projectRepository) {
        //note: the keys 'watchers_count' and 'watchers' are actually equals to 'stargazers_count'
        githubMetrics.setNumberOfWatchers(projectRepository.getInt("subscribers_count"));
    }

    private void collectNumberOfOpenIssues(GithubMetrics githubMetrics, JSONObject projectRepository) {
        githubMetrics.setNumberOfOpenIssues(projectRepository.getInt("open_issues_count"));
    }

    private void collectNumberOfCommits(GithubMetrics githubMetrics, String projectName) {
        String url = BASE_URL + "/repos/" + projectName + "/commits";
        JSONArray commits = new JSONArray(restRequester.sendRequest(url).getBody());
        githubMetrics.setNumberOfCommits(commits.length());
    }

    /**
     * Collects the number of Downloads for each artifact for each Release.
     * Stores the artifact-name with pattern: 'releaseVersion:artifact'
     *
     * @param githubMetrics The model where to store collected data
     * @param projectRepository The projectRepository retrieved from github api v3.
     * Due to improve performance the repository is used to check whether downloads exists or not before requesting for
     * download statistics.
     * If 'null' download statistics will be requested anyway.
     * @param projectName The GitHub repository. pattern: '<user>/<repository>'
     */
    private void collectNumberOfDownloads(GithubMetrics githubMetrics, JSONObject projectRepository, String projectName) {

        if (projectRepository == null || projectRepository.getBoolean("has_downloads")) {
            String url = BASE_URL + "/repos/" + projectName + "/releases";
            JSONArray releases = new JSONArray(restRequester.sendRequest(url).getBody());

            for (int releasesIndex = 0; releasesIndex < releases.length(); releasesIndex++) {
                JSONObject releaseRepository = releases.getJSONObject(releasesIndex);
                JSONArray assets = releaseRepository.getJSONArray("assets");
                for (int assetsIndex = 0; assetsIndex < assets.length(); assetsIndex++) {
                    JSONObject asset = assets.getJSONObject(assetsIndex);
                    String key = releaseRepository.getString("tag_name") + ":" + asset.getString("name");
                    int value = asset.getInt("download_count");

                    githubMetrics.addDownloadsPerRelease(key, value);
                }
            }
        }
    }

    private void collectNumberOfUniqueVisitors(GithubMetrics githubMetrics, String projectName) {
        try {
            String url = BASE_URL + "/repos/" + projectName + "/traffic/views";
            JSONObject visitors = new JSONObject(restRequester.sendRequest(url).getBody());
            githubMetrics.setNumberOfUniqueVisitors(visitors.getInt("uniques"));
        } catch (HttpClientErrorException e) {
            throw new UserDeniedException("No authorized user logged in!");
        }
    }

    /**
     * Contribution to the traffic from sites referring to the given Github repository @projectName.
     * The method collects the top 10 referrers and their non-unique visits over the last 14 days.
     *
     * @param githubMetrics The model where to store collected data
     * @param projectName The GitHub repository. pattern: '<user>/<repository>'
     */
    private void collectReferringSitesLast14Days(GithubMetrics githubMetrics, String projectName) {
        String url = BASE_URL + "/repos/" + projectName + "/traffic/popular/referrers";
        JSONArray referringSites = new JSONArray(restRequester.sendRequest(url).getBody());

        for (int referrerIndex = 0; referrerIndex < referringSites.length(); referrerIndex++) {
            JSONObject referringSite = referringSites.getJSONObject(referrerIndex);
            githubMetrics.addReferringSitesLast14Days(referringSite.getString("referrer"), referringSite.getInt("count"));
        }
    }

}