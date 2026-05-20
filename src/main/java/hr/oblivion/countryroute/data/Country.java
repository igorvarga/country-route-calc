package hr.oblivion.countryroute.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Country(String cca3, List<String> borders, List<Double> latlng, Name name) {
  public Country {
    Objects.requireNonNull(cca3, "cca3");
    borders = borders == null ? List.of() : List.copyOf(borders);
    latlng = latlng == null ? List.of() : List.copyOf(latlng);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Name(String common) {}
}
