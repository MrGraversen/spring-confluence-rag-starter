package io.graversen.springboot3app.configuration;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import io.graversen.starter.spring.confluence.rag.ConfluenceIngestor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleRunner implements ApplicationRunner {
    // ConversationalRetrievalChain is the main entry point for RAG (Retrieval-Augmented Generation) tasks.
    // It allows you to ask questions and get answers based on the ingested documents.
    private final ConversationalRetrievalChain ragChain;

    // ConfluenceIngestor is responsible for ingesting documents from Confluence into the RAG system.
    // It handles the retrieval and processing of documents, making them available for querying.
    private final ConfluenceIngestor confluenceIngestor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Asynchronously ingest documents from Confluence into the RAG system, then run the example questions.
        confluenceIngestor.ingest().thenRun(askExampleQuestions());
    }

    private Runnable askExampleQuestions() {
        return () -> {
            askAndAnswer("Which rig had the highest yield per hour during Cycle 338?");
            askAndAnswer("How many radiation incidents occurred in Bay 4-D?");
            askAndAnswer("Who executed a manual override using Protocol Theta-9?");
            askAndAnswer("How many miners were promoted to Overseer rank?");
        };
    }

    private void askAndAnswer(@NonNull String question) {
        final var answer = ragChain.execute(question);
        log.info("Question: {}", question);
        log.info("Answer: {}", answer);
    }
}
