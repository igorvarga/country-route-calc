package hr.oblivion.countryroute.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Component("local")
public class LocalFileCountrySource implements CountrySource {

    private final CountriesConfig config;
    private final ObjectMapper objectMapper;

    public LocalFileCountrySource(CountriesConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    public CountryDataset load() {
        if (config.local() == null || config.local().path() == null) {
            throw new IllegalStateException("countries.local.path is required when countries.source=local");
        }
        Path path = config.local().path();
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("countries.local.path does not point to a readable file: " + path);
        }
        try (var stream = Files.newInputStream(path)) {
            List<Country> countries = objectMapper.readValue(stream, new TypeReference<>() {});
            return new CountryDataset(
                    countries,
                    new DatasetMetadata("local", Instant.now(), countries.size())
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load countries from " + path, e);
        }
    }
}
