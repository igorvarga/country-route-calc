package hr.oblivion.countryroute.data;

import java.time.Instant;

public record DatasetMetadata(String source, Instant loadedAt, int countryCount) {}
