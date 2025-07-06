package io.graversen.starter.spring.confluence.rag;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ConfluenceIngestor {
    private final @NonNull ConfluenceApi confluenceApi;
    private final @NonNull EmbeddingStoreIngestor embeddingStoreIngestor;
    private final @NonNull ConfluenceProperties confluenceProperties;
    private final @NonNull ConfluenceContentFormatter contentFormatter;
    private final @NonNull ConfluenceMetadataExtractor metadataExtractor;

    public CompletableFuture<Void> ingest() {
        return CompletableFuture.runAsync(() -> {
            final var startedAt = Instant.now();
            log.info("⚙️ Starting ingestion of Confluence spaces {} at {}", confluenceProperties.getSpaces(), confluenceProperties.getBaseUrl());
            confluenceProperties.getSpaces().forEach(ingestConfluenceSpace());
            log.info("⚙️ Ingestion complete after {} ms", Duration.between(startedAt, Instant.now()).toMillis());
        });
    }

    private Consumer<String> ingestConfluenceSpace() {
        return spaceKey -> {
            log.info("⚙️ Starting ingestion of Confluence pages from space: {}", spaceKey);
            final var pagesResponse = confluenceApi.getPages(spaceKey, 100, 0);
            if (pagesResponse.results().isEmpty()) {
                log.warn("⚙️ No pages found in space '{}'. Skipping ingestion.", spaceKey);
            }
            pagesResponse.results().forEach(ingestConfluencePage());
        };
    }

    private Consumer<ConfluenceDtos.PageSummary> ingestConfluencePage() {
        return page -> {
            log.info("⚙️ Ingesting Confluence page: {} ({})", page.title(), page.id());
            final var pageResponse = confluenceApi.getPage(page.id());

            final var pageBody = pageResponse.body().storage().value();

            if (pageBody != null && !pageBody.isBlank()) {
                final var pageBodyFormatted = contentFormatter.formatContent(pageBody);

                final var metadataMap = metadataExtractor.extractMetadata(pageResponse);
                final var metadata = Metadata.from(metadataMap);

                final var document = Document.from(pageBodyFormatted, metadata);
                embeddingStoreIngestor.ingest(document);
            } else {
                log.warn("⚙️ No content found for page: {}", pageResponse.title());
            }
        };
    }
}
