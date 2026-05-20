package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileCountrySourceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void loadsCountriesFromJsonFile(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("countries.json");
    Files.writeString(
        file,
        """
        [
          {"cca3":"CZE","borders":["AUT"],"latlng":[49.75,15.5],"name":{"common":"Czechia"}},
          {"cca3":"AUT","borders":["CZE"],"latlng":[47.33,13.33],"name":{"common":"Austria"}}
        ]
        """);
    LocalFileCountrySource source =
        new LocalFileCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.LOCAL, null, new CountriesConfig.Local(file)),
            objectMapper);

    CountryDataset dataset = source.load();

    assertThat(dataset.countries()).hasSize(2);
    assertThat(dataset.metadata().source()).isEqualTo("local");
    assertThat(dataset.metadata().countryCount()).isEqualTo(2);
    assertThat(dataset.metadata().location())
        .isEqualTo(file.toAbsolutePath().normalize().toString());
  }

  @Test
  void missingLocalConfigFailsFast() {
    LocalFileCountrySource source =
        new LocalFileCountrySource(
            new CountriesConfig(CountriesConfig.Source.LOCAL, null, null), objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("countries.local.path is required");
  }

  @Test
  void missingLocalPathFailsFast() {
    LocalFileCountrySource source =
        new LocalFileCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.LOCAL, null, new CountriesConfig.Local(null)),
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("countries.local.path is required");
  }

  @Test
  void nonRegularFilePathFailsFast(@TempDir Path tmp) {
    Path missing = tmp.resolve("does-not-exist.json");
    LocalFileCountrySource source =
        new LocalFileCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.LOCAL, null, new CountriesConfig.Local(missing)),
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("does not point to a readable file");
  }

  @Test
  void malformedJsonFailsFast(@TempDir Path tmp) throws Exception {
    Path file = tmp.resolve("bad.json");
    Files.writeString(file, "not json");
    LocalFileCountrySource source =
        new LocalFileCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.LOCAL, null, new CountriesConfig.Local(file)),
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to load countries from");
  }
}
