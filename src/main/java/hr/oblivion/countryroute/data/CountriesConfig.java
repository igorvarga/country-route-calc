package hr.oblivion.countryroute.data;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "countries")
@Validated
public record CountriesConfig(@NotNull Source source, Remote remote, Local local) {
  public enum Source {
    REMOTE,
    EMBEDDED,
    LOCAL
  }

  public record Remote(@URL String url) {}

  public record Local(Path path) {}
}
