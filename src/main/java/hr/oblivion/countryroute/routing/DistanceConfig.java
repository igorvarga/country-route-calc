package hr.oblivion.countryroute.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class DistanceConfig {

    private static final Logger log = LoggerFactory.getLogger(DistanceConfig.class);

    @Bean
    @Primary
    public DistanceCalculator geographicLibDistanceCalculator() {
        DistanceCalculator calculator = new GeographicLibDistanceCalculator();
        log.info("Active distance calculator: {}", calculator.name());
        return calculator;
    }

    @Bean
    public DistanceCalculator haversineDistanceCalculator() {
        return new HaversineDistanceCalculator();
    }
}
