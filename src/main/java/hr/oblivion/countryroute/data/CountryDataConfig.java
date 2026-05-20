package hr.oblivion.countryroute.data;

import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CountryDataConfig {

  private static final Logger log = LoggerFactory.getLogger(CountryDataConfig.class);

  @Bean
  public CountryDataset countryDataset(Map<String, CountrySource> sources, CountriesConfig config) {
    String key = config.source().name().toLowerCase(Locale.ROOT);
    CountrySource source = sources.get(key);
    if (source == null) {
      throw new IllegalStateException(
          "Unknown countries.source: " + key + " (available: " + sources.keySet() + ")");
    }
    CountryDataset dataset = source.load();
    DatasetMetadata md = dataset.metadata();
    log.info(
        "Country dataset metadata: source={} location={} countryCount={} loadedAt={}",
        md.source(),
        md.location(),
        md.countryCount(),
        md.loadedAt());
    return dataset;
  }

  @Bean
  public CountryGraph countryGraph(CountryDataset dataset) {
    CountryGraph graph = CountryGraph.from(dataset);
    log.info("Built country graph with {} nodes", graph.size());
    return graph;
  }
}
