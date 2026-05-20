package hr.oblivion.countryroute;

import static org.assertj.core.api.Assertions.assertThat;

import hr.oblivion.countryroute.data.CountryDataset;
import hr.oblivion.countryroute.data.CountryGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CountryRouteApplicationTest {

  @Autowired private CountryDataset dataset;

  @Autowired private CountryGraph graph;

  @Test
  void contextLoadsAndPopulatesDatasetFromEmbeddedSource() {
    assertThat(dataset.countries()).isNotEmpty();
    assertThat(dataset.metadata().source()).isEqualTo("embedded");
    assertThat(dataset.metadata().location()).isEqualTo("classpath:data/countries.json");
    assertThat(graph.size()).isGreaterThan(200);
    assertThat(graph.contains("CZE")).isTrue();
    assertThat(graph.neighbors("CZE")).contains("AUT");
  }
}
