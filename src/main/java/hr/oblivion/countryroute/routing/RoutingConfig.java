package hr.oblivion.countryroute.routing;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
public class RoutingConfig {

  private static final Logger log = LoggerFactory.getLogger(RoutingConfig.class);

  @Bean
  @Primary
  public RouteFinder activeRouteFinder(
      Map<String, RouteFinder> finders, @Value("${routing.algorithm}") String algorithmKey) {
    RouteFinder finder = finders.get(algorithmKey);
    if (finder == null) {
      throw new IllegalStateException(
          "Unknown routing.algorithm=" + algorithmKey + " (available: " + finders.keySet() + ")");
    }
    log.info("Active routing algorithm: {}", algorithmKey);
    return finder;
  }
}
