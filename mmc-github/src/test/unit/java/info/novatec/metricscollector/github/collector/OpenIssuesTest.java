package info.novatec.metricscollector.github.collector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import info.novatec.metricscollector.commons.rest.RestService;
import info.novatec.metricscollector.github.Metrics;
import info.novatec.metricscollector.github.util.DataProvider;


@RunWith(SpringRunner.class)
public class OpenIssuesTest {

    @MockBean
    private RestService restService;

    private Metrics metrics;

    OpenIssues openIssues;

    @MockBean
    JsonObject mockedRepository;

    @Before
    public void init() {
        metrics = DataProvider.createEmptyMetrics();
        openIssues = spy(new OpenIssues(restService, metrics));
        when(mockedRepository.getInt("open_issues_count")).thenReturn(4);
    }

    @Test
    public void collectTest() {
        openIssues.setProjectRepository(mockedRepository);
        openIssues.collect();
        assertThat(metrics.getMetrics().size()).isEqualTo(1);
        assertThat(metrics.getMetrics().entrySet().iterator().next().getValue()).isEqualTo(4);
    }
}
