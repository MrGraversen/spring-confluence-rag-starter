package io.graversen.starter.spring.confluence.rag;

import lombok.NonNull;

public interface ConfluenceContentFormatter {
    String formatContent(@NonNull String pageContent);
}
