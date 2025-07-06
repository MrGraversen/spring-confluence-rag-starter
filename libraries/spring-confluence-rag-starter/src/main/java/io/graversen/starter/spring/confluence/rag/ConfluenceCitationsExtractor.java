package io.graversen.starter.spring.confluence.rag;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ConfluenceCitationsExtractor implements ConfluenceMetadataExtractor {
    private final ConfluenceProperties confluenceProperties;

    @Override
    public Map<String, String> extractMetadata(ConfluenceDtos.@NonNull PageResponse confluencePage) {
        return Map.of(
                "pageId", confluencePage.id(),
                "pageTitle", confluencePage.title(),
                "pageUrl", "%s/pages/viewpage.action?pageId=%s".formatted(confluenceProperties.getBaseUrl(), confluencePage.id())
        );
    }
}
