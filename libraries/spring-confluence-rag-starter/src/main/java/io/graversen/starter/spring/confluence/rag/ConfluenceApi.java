package io.graversen.starter.spring.confluence.rag;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import static io.graversen.starter.spring.confluence.rag.ConfluenceDtos.PageListResponse;
import static io.graversen.starter.spring.confluence.rag.ConfluenceDtos.PageResponse;

@Headers("Accept: application/json")
public interface ConfluenceApi {
    @RequestLine("GET /rest/api/content?spaceKey={spaceKey}&type=page&limit={limit}&start={start}")
    PageListResponse getPages(
            @Param("spaceKey") String spaceKey,
            @Param("limit") Integer limit,
            @Param("start") Integer start
    );

    @RequestLine("GET /rest/api/content/{id}?expand=body.storage")
    PageResponse getPage(@Param("id") String id);
}
