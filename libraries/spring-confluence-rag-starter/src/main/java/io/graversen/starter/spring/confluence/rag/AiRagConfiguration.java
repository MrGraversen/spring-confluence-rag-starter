package io.graversen.starter.spring.confluence.rag;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.*;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class AiRagConfiguration {
    @Bean
    public OpenAiChatModelName chatModelName(SpringConfluenceRagProperties springConfluenceRagProperties) {
        final var chatModelProperties = springConfluenceRagProperties.getChatModel();
        final var chatModel = Arrays.stream(OpenAiChatModelName.values())
                .filter(modelName -> modelName.toString().equalsIgnoreCase(chatModelProperties.getModel()))
                .findFirst()
                .orElse(OpenAiChatModelName.GPT_4_O_MINI);

        log.info("✅ Setting OpenAI Embedding Model Name: {}", chatModel);
        return chatModel;
    }

    @Bean
    public OpenAiEmbeddingModelName embeddingModelName(SpringConfluenceRagProperties springConfluenceRagProperties) {
        final var embeddingProperties = springConfluenceRagProperties.getEmbeddingModel();
        final var embeddingModel = Arrays.stream(OpenAiEmbeddingModelName.values())
                .filter(modelName -> modelName.toString().equalsIgnoreCase(embeddingProperties.getModel()))
                .findFirst()
                .orElse(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_SMALL);

        log.info("✅ Setting OpenAI Chat Model Name: {}", embeddingModel);
        return embeddingModel;
    }

    @Bean
    public OpenAiChatModel chatModel(
            SpringConfluenceRagProperties springConfluenceRagProperties,
            OpenAiChatModelName chatModelName
    ) {
        final var openAiProperties = springConfluenceRagProperties.getOpenAi();
        final var chatModelProperties = springConfluenceRagProperties.getChatModel();
        final var chatModel = OpenAiChatModel.builder()
                .apiKey(openAiProperties.getApiKey())
                .modelName(chatModelName)
                .temperature(Math.max(chatModelProperties.getTemperature(), 1.25))
                .build();

        log.info("✅ Initialized OpenAI Chat Model");
        return chatModel;
    }

    @Bean
    public ContentRetriever contentRetriever(
            PgVectorEmbeddingStore store,
            EmbeddingModel embedder,
            SpringConfluenceRagProperties springConfluenceRagProperties
    ) {
        final var embeddingProperties = springConfluenceRagProperties.getEmbeddingModel();
        final var retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embedder)
                .maxResults(embeddingProperties.getTopK())
                .minScore(embeddingProperties.getSimilarityThreshold())
                .build();

        log.info("✅ Initialized Embedding Store Content Retriever");
        return retriever;
    }

    @Bean
    public ConversationalRetrievalChain ragChain(
            OpenAiChatModel chatModel,
            ContentRetriever retriever,
            OpenAiChatModelName chatModelName,
            SpringConfluenceRagProperties springConfluenceRagProperties
    ) {
        final var chatModelProperties = springConfluenceRagProperties.getChatModel();
        final var memory = TokenWindowChatMemory.withMaxTokens(chatModelProperties.getMemoryTokens(), new OpenAiTokenCountEstimator(chatModelName));
        memory.add(new SystemMessage(chatModelProperties.getSystemPrompt()));

        final var queryRouter = new DefaultQueryRouter(retriever);

        final var contentInjector = DefaultContentInjector.builder()
                .metadataKeysToInclude(List.copyOf(chatModelProperties.getMetaDataKeys()))
                .build();

        final var retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .contentInjector(contentInjector)
                .build();

        final var chain = ConversationalRetrievalChain.builder()
                .chatModel(chatModel)
                .chatMemory(memory)
                .retrievalAugmentor(retrievalAugmentor)
                .build();

        log.info("✅ Initialized Conversational Retrieval Chain");
        return chain;
    }

    @Bean
    public EmbeddingModel embeddingModel(
            SpringConfluenceRagProperties springConfluenceRagProperties,
            OpenAiEmbeddingModelName embeddingModelName
    ) {
        final var openAiProperties = springConfluenceRagProperties.getOpenAi();

        final var embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openAiProperties.getApiKey())
                .modelName(embeddingModelName)
                .dimensions(embeddingModelName.dimension())
                .build();

        log.info("✅ Initialized OpenAI Embedding Model");
        return embeddingModel;
    }

    @Bean
    public PgVectorEmbeddingStore embeddingStore(SpringConfluenceRagProperties springConfluenceRagProperties, EmbeddingModel embeddingModel) {
        final var databaseProperties = springConfluenceRagProperties.getDatabase();
        final var embeddingStore = PgVectorEmbeddingStore.builder()
                .host(databaseProperties.getHostname())
                .port(databaseProperties.getPort())
                .database(databaseProperties.getDatabase())
                .user(databaseProperties.getUsername())
                .password(databaseProperties.getPassword())
                .table(databaseProperties.getTable())
                .dimension(embeddingModel.dimension())
                .createTable(true)
                .dropTableFirst(false)
                .build();

        log.info("✅ Initialized PgVector Embedding Store");
        return embeddingStore;
    }

    @Bean
    public DocumentSplitter documentSplitter(SpringConfluenceRagProperties springConfluenceRagProperties) {
        final var embeddingProperties = springConfluenceRagProperties.getEmbeddingModel();
        final var chunkSize = embeddingProperties.getChunkSize();
        final var fraction = embeddingProperties.getOverlapFraction();
        final var overlap = (int) Math.round(Math.max(0.0, Math.min(fraction, 1.0)) * chunkSize);

        final var documentSplitter = DocumentSplitters.recursive(chunkSize, overlap);

        log.info("✅ Initialized Document Splitter");
        return documentSplitter;
    }

    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingModel embeddingModel,
            PgVectorEmbeddingStore embeddingStore,
            DocumentSplitter documentSplitter
    ) {
        final var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .documentSplitter(documentSplitter)
                .build();

        log.info("✅ Initialized Embedding Store Ingestor");
        return ingestor;
    }
}
