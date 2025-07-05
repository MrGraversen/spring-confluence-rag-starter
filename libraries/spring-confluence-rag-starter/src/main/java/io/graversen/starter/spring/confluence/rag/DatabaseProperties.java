package io.graversen.starter.spring.confluence.rag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseProperties {
    private String hostname;
    private Integer port = 5432;
    private String database;
    private String username;
    private String password;
    private String table = "confluence_vector_store";
}
