package hr.oblivion.countryroute.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component("embedded")
public class EmbeddedCountrySource implements CountrySource {

    private static final String DEFAULT_RESOURCE = "data/countries.json";

    private final ObjectMapper objectMapper;
    private final String resourcePath;

    @Autowired
    public EmbeddedCountrySource(ObjectMapper objectMapper) {
        this(objectMapper, DEFAULT_RESOURCE);
    }

    public EmbeddedCountrySource(ObjectMapper objectMapper, String resourcePath) {
        this.objectMapper = objectMapper;
        this.resourcePath = resourcePath;
    }

    @Override
    public CountryDataset load() {
        try (var stream = new ClassPathResource(resourcePath).getInputStream()) {
            List<Country> countries = objectMapper.readValue(stream, new TypeReference<>() {});
            return new CountryDataset(
                    countries,
                    new DatasetMetadata("embedded", Instant.now(), countries.size())
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load embedded countries from " + resourcePath, e);
        }
    }
}
