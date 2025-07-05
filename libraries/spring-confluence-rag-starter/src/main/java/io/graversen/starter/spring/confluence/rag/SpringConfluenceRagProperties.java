package io.graversen.starter.spring.confluence.rag;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.spring.confluence.rag")
public class SpringConfluenceRagProperties {
    @NestedConfigurationProperty
    private ConfluenceProperties confluence = new ConfluenceProperties();

    @NestedConfigurationProperty
    private EmbeddingModelProperties embeddingModel = new EmbeddingModelProperties();

    @NestedConfigurationProperty
    private DatabaseProperties database = new DatabaseProperties();

    @NestedConfigurationProperty
    private ChatModelProperties chatModel = new ChatModelProperties();

    @NestedConfigurationProperty
    private OpenAiProperties openAi = new OpenAiProperties();
}
