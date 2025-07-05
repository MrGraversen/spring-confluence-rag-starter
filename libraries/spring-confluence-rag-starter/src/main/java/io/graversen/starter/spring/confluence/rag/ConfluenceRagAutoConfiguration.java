package io.graversen.starter.spring.confluence.rag;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties({SpringConfluenceRagProperties.class})
@Import({ConfluenceConfiguration.class, AiRagConfiguration.class})
public class ConfluenceRagAutoConfiguration {

}
