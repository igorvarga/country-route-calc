package hr.oblivion.countryroute.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.oblivion.countryroute.data.CountryGraph;
import hr.oblivion.countryroute.data.EmbeddedCountrySource;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RoutingConfigTest {

  private final RoutingConfig config = new RoutingConfig();
  private final CountryGraph graph =
      CountryGraph.from(
          new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json").load());

  @Test
  void selectsConfiguredAlgorithm() {
    BfsRouteFinder bfs = new BfsRouteFinder(graph);

    RouteFinder finder = config.activeRouteFinder(Map.of("bfs", bfs), "bfs");

    assertThat(finder).isSameAs(bfs);
  }

  @Test
  void unknownAlgorithmFailsFast() {
    assertThatThrownBy(() -> config.activeRouteFinder(Map.of(), "mystery"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Unknown routing.algorithm=mystery");
  }
}
