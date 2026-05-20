package hr.oblivion.countryroute.routing;

import hr.oblivion.countryroute.data.CountryGraph;
import jakarta.validation.constraints.Pattern;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/routes")
@Validated
public class RoutingController {

  private static final String CCA3_REGEX = "[A-Za-z]{3}";

  private final CountryGraph graph;
  private final RouteFinder finder;

  public RoutingController(CountryGraph graph, RouteFinder finder) {
    this.graph = graph;
    this.finder = finder;
  }

  @GetMapping(value = "/{origin}/{destination}", produces = MediaType.APPLICATION_JSON_VALUE)
  public RouteResponse route(
      @PathVariable @Pattern(regexp = CCA3_REGEX) String origin,
      @PathVariable @Pattern(regexp = CCA3_REGEX) String destination) {
    String o = origin.toUpperCase(Locale.ROOT);
    String d = destination.toUpperCase(Locale.ROOT);

    if (!graph.contains(o)) {
      throw new UnknownCountryException(o, "origin");
    }
    if (!graph.contains(d)) {
      throw new UnknownCountryException(d, "destination");
    }

    return finder
        .find(o, d)
        .map(path -> RouteResponse.of(o, d, path))
        .orElseThrow(() -> new NoRouteException(o, d));
  }
}
