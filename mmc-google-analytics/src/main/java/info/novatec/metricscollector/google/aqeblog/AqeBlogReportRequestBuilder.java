package info.novatec.metricscollector.google.aqeblog;

import com.google.api.services.analyticsreporting.v4.AnalyticsReporting;
import com.google.api.services.analyticsreporting.v4.model.*;
import info.novatec.metricscollector.google.GaConfigProperties;
import info.novatec.metricscollector.google.IGaReportRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static info.novatec.metricscollector.google.GaDimensionsEnum.*;
import static info.novatec.metricscollector.google.GaFilterOperatorsEnum.*;
import static info.novatec.metricscollector.google.GaMetricsEnum.*;

@Component
public class AqeBlogReportRequestBuilder implements IGaReportRequestBuilder {

    private AnalyticsReporting service;

    private GaConfigProperties gaConfigProperties;

    @Autowired
    public AqeBlogReportRequestBuilder(AnalyticsReporting service, GaConfigProperties customConfigurationProperties) {
        this.service = service;
        this.gaConfigProperties = customConfigurationProperties;
    }

    @Override
    public GetReportsResponse sendReportRequest() throws IOException {
        DateRange dateRange = createDateRange(gaConfigProperties.getStartPeriod(), gaConfigProperties.getEndPeriod());
        List<DimensionFilter> dimensionFilters = new ArrayList<>();
        List<ReportRequest> reportRequests = new ArrayList<>();

        Metric metric = createMetrics();
        Dimension dimension = createDimension();
        DimensionFilter hostNameDimensionFilter = createDimensionFilter(GA_HOST_NAME.toString(), EXACT.toString(), gaConfigProperties.getHostName());
        DimensionFilter pagePathDimensionFilter = createDimensionFilter(GA_PAGE_PATH.toString(), REGEXP.toString(), "");
        dimensionFilters.add(hostNameDimensionFilter);
        dimensionFilters.add(pagePathDimensionFilter);
        DimensionFilterClause dimensionFilterClause = createDimensionFilterClause(AND.toString(), dimensionFilters);

        ReportRequest reportRequest = createReportRequest(gaConfigProperties.getViewId(), dateRange, metric, dimension, dimensionFilterClause);
        reportRequests.add(reportRequest);

        GetReportsRequest getReport = new GetReportsRequest()
                .setReportRequests(reportRequests);

        return service.reports().batchGet(getReport).execute();
    }

    /**
     * Creates the DataRange Object by providing it with start and end date
     *
     * @param startDate the begin of the period as String
     * @param endDate   the end of the period as String
     * @return the DataRange object
     */
    private DateRange createDateRange(String startDate, String endDate) {
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);
        return dateRange;
    }

    /**
     * Creates the Metric object with its required values. Metric is a termin coming from Google Analytics Reporting API.
     *
     * @return the Metric object
     */
    private Metric createMetrics() {
        return new Metric()
                .setExpression(GA_PAGE_VIEWS.toString())
                .setExpression(GA_UNIQUE_PAGE_VIEWS.toString())
                .setExpression(GA_SESSION.toString())
                .setExpression(GA_BOUNCES.toString())
                .setExpression(GA_BOUNCE_RATE.toString())
                .setExpression(GA_AVG_SESSION_DURATION.toString())
                .setExpression(GA_AVG_TIME_ON_PAGE.toString());
    }

    /**
     * Creates the Dimension object with its required values. Dimension is a termin coming from Google Analytics Reporting API.
     *
     * @return the Dimension object
     */
    private Dimension createDimension() {
        return new Dimension()
                .setName(GA_PAGE_TITLE.toString())
                .setName(GA_HOST_NAME.toString())
                .setName(GA_PAGE_PATH.toString());
    }

    /**
     * Creates DimensionFiler object with its name, operator and value for comparison.
     * DimensionFiler is used when additional filtering condition is required on a dimension.
     *
     * @param name            the name of the dimension to be filtered on.
     * @param operator        operator that can be used for filtering.
     * @param comparisonValue value against which the dimension is compared
     * @return the DimensionFilter object
     */
    private DimensionFilter createDimensionFilter(String name, String operator, String comparisonValue) {
        return new DimensionFilter()
                .setDimensionName(name)
                .setOperator(operator)
                .setExpressions(Collections.singletonList(comparisonValue));
    }

    /**
     * Creates DimensionFilterClause object. It serves as a unification of more than one dimension filters.
     *
     * @param operator         the operator for joining the dimension filters like AND, OR.
     * @param dimensionFilters list of DimensionFilter objects
     * @return the DimensionFilterClause object
     */
    private DimensionFilterClause createDimensionFilterClause(String operator, List<DimensionFilter> dimensionFilters) {
        return new DimensionFilterClause()
                .setOperator(operator)
                .setFilters(dimensionFilters);
    }

    private ReportRequest createReportRequest(String viewId, DateRange dateRange, Metric metric, Dimension dimension, DimensionFilterClause dimensionFilterClause) {
        return new ReportRequest()
                .setViewId(viewId)
                .setDateRanges(Collections.singletonList(dateRange))
                .setMetrics(Collections.singletonList(metric))
                .setDimensions(Collections.singletonList(dimension))
                .setDimensionFilterClauses(Collections.singletonList(dimensionFilterClause));
    }
}
