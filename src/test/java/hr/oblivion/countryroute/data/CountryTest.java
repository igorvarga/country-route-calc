package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class CountryTest {

  @Test
  void nullBordersAndLatlngDefaultToEmpty() {
    Country country = new Country("CZE", null, null, new Country.Name("Czechia"));

    assertThat(country.borders()).isEmpty();
    assertThat(country.latlng()).isEmpty();
  }

  @Test
  void copiesBordersAndLatlngWhenProvided() {
    Country country =
        new Country("CZE", List.of("AUT"), List.of(49.75, 15.5), new Country.Name("Czechia"));

    assertThat(country.borders()).containsExactly("AUT");
    assertThat(country.latlng()).containsExactly(49.75, 15.5);
  }

  @Test
  void nullCca3FailsFast() {
    assertThatThrownBy(() -> new Country(null, List.of(), List.of(), new Country.Name("X")))
        .isInstanceOf(NullPointerException.class);
  }
}
