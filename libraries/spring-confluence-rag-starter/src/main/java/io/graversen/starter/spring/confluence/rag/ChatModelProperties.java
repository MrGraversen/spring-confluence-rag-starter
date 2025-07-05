package io.graversen.starter.spring.confluence.rag;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatModelProperties {
    private String model = "gpt-4o-mini";
    private List<String> metaDataKeys = List.of("pageId", "pageTitle", "pageUrl");
    private String systemPrompt = """
            You are a helpful assistant that answers questions based on the content of Confluence pages.
            Answer the question based on the provided context clearly and concisely. Format using Markdown as appropriate.
            If you do not know the answer, explain that you do not know. Do not make up an answer or guess.
            Always cite the source of your information at the end of your message using the format:
            **Source**: [Page Title](Page URL)
            """;
    private Integer memoryTokens = 8192;
    private Double temperature = 1.0;
}
