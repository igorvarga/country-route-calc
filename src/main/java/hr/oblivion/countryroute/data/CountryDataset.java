package hr.oblivion.countryroute.data;

import java.util.List;

public record CountryDataset(List<Country> countries, DatasetMetadata metadata) {
  public CountryDataset {
    countries = List.copyOf(countries);
  }
}
