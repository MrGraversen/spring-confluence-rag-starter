package io.graversen.starter.spring.confluence.rag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddingModelProperties {
    private Integer chunkSize = 512;
    private Double overlapFraction = 0.25;
    private String model = "text-embedding-3-small";
    private Integer topK = 5;
    private Double similarityThreshold = 0.5;
}
