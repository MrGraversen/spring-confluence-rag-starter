package io.graversen.starter.spring.confluence.rag;

import java.util.List;

public class ConfluenceDtos {
    public record PageSummary(String id, String title) {
    }

    public record PageListResponse(List<PageSummary> results, String nextCursor) {

    }

    public record StorageBody(String value) {

    }

    public record PageResponse(String id, String title, Body body) {
        public record Body(StorageBody storage) {

        }
    }
}
