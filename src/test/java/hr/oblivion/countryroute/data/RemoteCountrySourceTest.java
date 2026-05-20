package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RemoteCountrySourceTest {

  private static final String URL = "https://example.test/countries";

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void fetchesAndParsesRemoteJson() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server
        .expect(requestTo(URL))
        .andRespond(
            withSuccess(
                """
                [
                  {"cca3":"CZE","borders":["AUT"],"latlng":[49.75,15.5],"name":{"common":"Czechia"}}
                ]
                """,
                MediaType.APPLICATION_JSON));
    RemoteCountrySource source =
        new RemoteCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.REMOTE, new CountriesConfig.Remote(URL), null),
            builder,
            objectMapper);

    CountryDataset dataset = source.load();

    assertThat(dataset.countries()).hasSize(1);
    assertThat(dataset.metadata().source()).isEqualTo("remote");
    assertThat(dataset.metadata().location()).isEqualTo(URL);
    assertThat(dataset.metadata().countryCount()).isEqualTo(1);
    server.verify();
  }

  @Test
  void missingRemoteConfigFailsFast() {
    RemoteCountrySource source =
        new RemoteCountrySource(
            new CountriesConfig(CountriesConfig.Source.REMOTE, null, null),
            RestClient.builder(),
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("countries.remote.url is required");
  }

  @Test
  void missingRemoteUrlFailsFast() {
    RemoteCountrySource source =
        new RemoteCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.REMOTE, new CountriesConfig.Remote(null), null),
            RestClient.builder(),
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("countries.remote.url is required");
  }

  @Test
  void malformedJsonResponseFailsFast() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(URL)).andRespond(withSuccess("not json", MediaType.APPLICATION_JSON));
    RemoteCountrySource source =
        new RemoteCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.REMOTE, new CountriesConfig.Remote(URL), null),
            builder,
            objectMapper);

    assertThatThrownBy(source::load)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to parse countries JSON from");
  }

  @Test
  void httpErrorPropagates() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(URL)).andRespond(withServerError());
    RemoteCountrySource source =
        new RemoteCountrySource(
            new CountriesConfig(
                CountriesConfig.Source.REMOTE, new CountriesConfig.Remote(URL), null),
            builder,
            objectMapper);

    assertThatThrownBy(source::load).isInstanceOf(RuntimeException.class);
  }
}
