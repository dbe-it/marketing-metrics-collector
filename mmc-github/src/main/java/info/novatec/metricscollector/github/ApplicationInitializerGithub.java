package info.novatec.metricscollector.github;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.Getter;
import lombok.Setter;

import info.novatec.metricscollector.commons.ApplicationInitializerCommons;


@SpringBootApplication
@EnableScheduling
@Import(ApplicationInitializerCommons.class)
@ConfigurationProperties(prefix="github")
@Setter
public class ApplicationInitializerGithub {

    private String token;

    @Getter
    private List<String> urls = new ArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(ApplicationInitializerGithub.class, args);
    }

    @Bean
    @Autowired
    public GithubCollector githubCollector(RestService restService){
        GithubCollector collector = new GithubCollector(restService);
        return collector;
    }

    @Bean
    public String token(){
        return token;
    }

}
