package hr.oblivion.countryroute.routing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class DistanceConfig {

    @Bean
    @Primary
    public DistanceCalculator geographicLibDistanceCalculator() {
        return new GeographicLibDistanceCalculator();
    }

    @Bean
    public DistanceCalculator haversineDistanceCalculator() {
        return new HaversineDistanceCalculator();
    }
}
