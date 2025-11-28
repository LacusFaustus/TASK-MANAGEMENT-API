package com.taskmanagement.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HtmlSanitizer {

    private static final Safelist SAFELIST = Safelist.basic()
            .addTags("p", "br", "strong", "em", "u", "s", "ul", "ol", "li")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https");

    public String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, SAFELIST);
    }

    public String sanitizeWithLineBreaks(String text) {
        if (text == null) {
            return null;
        }
        // Сохраняем переносы строк
        return Jsoup.clean(text, Safelist.none())
                .replace("\n", "<br>");
    }
}
