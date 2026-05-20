package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EmbeddedCountrySourceTest {

  private final EmbeddedCountrySource source =
      new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json");

  @Test
  void loadsFixtureAndMapsFields() {
    CountryDataset dataset = source.load();

    assertThat(dataset.countries()).hasSize(7);
    assertThat(dataset.metadata().source()).isEqualTo("embedded");
    assertThat(dataset.metadata().location()).isEqualTo("classpath:data/countries-fixture.json");
    assertThat(dataset.metadata().countryCount()).isEqualTo(7);

    Country czechia =
        dataset.countries().stream().filter(c -> c.cca3().equals("CZE")).findFirst().orElseThrow();

    assertThat(czechia.name().common()).isEqualTo("Czechia");
    assertThat(czechia.borders()).containsExactlyInAnyOrder("AUT", "DEU", "POL", "SVK");
    assertThat(czechia.latlng()).containsExactly(49.75, 15.5);
  }

  @Test
  void missingResourceFailsFast() {
    EmbeddedCountrySource missingSource =
        new EmbeddedCountrySource(new ObjectMapper(), "data/does-not-exist.json");

    assertThatThrownBy(missingSource::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to load embedded countries from");
  }

  @Test
  void handlesEmptyBorders() {
    CountryDataset dataset = source.load();
    Country iceland =
        dataset.countries().stream().filter(c -> c.cca3().equals("ISL")).findFirst().orElseThrow();
    assertThat(iceland.borders()).isEmpty();
  }
}
