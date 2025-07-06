package io.graversen.starter.spring.confluence.rag;

import lombok.NonNull;

import java.util.Map;

public interface ConfluenceMetadataExtractor {
    Map<String, String> extractMetadata(@NonNull ConfluenceDtos.PageResponse confluencePage);
}
