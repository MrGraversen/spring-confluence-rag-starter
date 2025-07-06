package io.graversen.starter.spring.confluence.rag;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.util.Lazy;

@Slf4j
@RequiredArgsConstructor
public class Html2MarkdownContentFormatter implements ConfluenceContentFormatter {
    private final Lazy<FlexmarkHtmlConverter> flexmarkHtmlConverter = Lazy.of(this::flexmarkHtmlConverter);

    @Override
    public String formatContent(@NonNull String pageContent) {
        final var cleanedContent = Jsoup.clean(pageContent, "", safelist(), new Document.OutputSettings().prettyPrint(true));
        return flexmarkHtmlConverter.get().convert(cleanedContent);
    }

    protected Safelist safelist() {
        return Safelist.relaxed();
    }

    protected FlexmarkHtmlConverter flexmarkHtmlConverter() {
        return FlexmarkHtmlConverter.builder().build();
    }
}
