package io.graversen.starter.spring.confluence.rag;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConfluenceProperties {
    private String baseUrl;
    private String username;
    private String apiKey;
    private List<String> spaces;
}
