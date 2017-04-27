package info.novatec.metricscollector.twitter.metrics;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.primitives.Ints;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import info.novatec.metricscollector.twitter.TwitterMetricsResult;
import info.novatec.metricscollector.twitter.exception.TwitterRuntimeException;


@Component
public class NumberOfMentions extends TwitterMetricAbstract implements TwitterMetric{
    public NumberOfMentions(Twitter twitter, TwitterMetricsResult metrics) {
        super(twitter, metrics);
    }

    @Override
    public void collect() {
        try {
            List<Status> tweets = getAllTweets(new Query("@" + metrics.getAtUserName()));
            //filter out mentions from user itself
            long mentioned = tweets.stream().filter(tweet -> !tweet.getUser().getName().equals(metrics.getUserName())).count();
            metrics.setMentions(Ints.checkedCast(mentioned));
        }catch(TwitterException e){
            throw new TwitterRuntimeException(e);
        }

    }
}