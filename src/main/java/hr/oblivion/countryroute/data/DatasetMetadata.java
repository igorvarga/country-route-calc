package hr.oblivion.countryroute.data;

import java.time.Instant;

public record DatasetMetadata(String source, String location, Instant loadedAt, int countryCount) {}
