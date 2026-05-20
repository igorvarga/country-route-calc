package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CountryDataConfigTest {

  private final CountryDataConfig config = new CountryDataConfig();

  @Test
  void selectsSourceByConfiguredKey() {
    EmbeddedCountrySource embedded =
        new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json");
    CountriesConfig settings = new CountriesConfig(CountriesConfig.Source.EMBEDDED, null, null);

    CountryDataset dataset = config.countryDataset(Map.of("embedded", embedded), settings);

    assertThat(dataset.metadata().source()).isEqualTo("embedded");
  }

  @Test
  void unknownSourceKeyFailsFast() {
    CountriesConfig settings = new CountriesConfig(CountriesConfig.Source.REMOTE, null, null);

    assertThatThrownBy(() -> config.countryDataset(Map.of(), settings))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unknown countries.source: remote");
  }

  @Test
  void countryGraphBeanWrapsDataset() {
    CountryDataset dataset =
        new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json").load();

    CountryGraph graph = config.countryGraph(dataset);

    assertThat(graph.size()).isEqualTo(7);
  }
}
