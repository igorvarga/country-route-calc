package hr.oblivion.countryroute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CountryRouteApplication {

  public static void main(String[] args) {
    SpringApplication.run(CountryRouteApplication.class, args);
  }
}
