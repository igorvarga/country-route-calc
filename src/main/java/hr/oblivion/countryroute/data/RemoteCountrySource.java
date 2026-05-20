package hr.oblivion.countryroute.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

@Component("remote")
public class RemoteCountrySource implements CountrySource {

    private static final Logger log = LoggerFactory.getLogger(RemoteCountrySource.class);

    private final CountriesConfig config;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RemoteCountrySource(CountriesConfig config,
                               RestClient.Builder builder,
                               ObjectMapper objectMapper) {
        this.config = config;
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public CountryDataset load() {
        if (config.remote() == null || config.remote().url() == null) {
            throw new IllegalStateException("countries.remote.url is required when countries.source=remote");
        }
        String url = config.remote().url();
        log.info("Fetching countries dataset from {}", url);
        String body = restClient.get().uri(url).retrieve().body(String.class);
        try {
            List<Country> countries = objectMapper.readValue(body, new TypeReference<>() {});
            return new CountryDataset(
                    countries,
                    new DatasetMetadata("remote", Instant.now(), countries.size())
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse countries JSON from " + url, e);
        }
    }
}
