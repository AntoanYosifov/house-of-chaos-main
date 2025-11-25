package com.antdevrealm.housechaosmain.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ImgUrlExpander {
    @Value("${app.public-base-url}")
    private String base;

    public String toPublicUrl(String imgUrl) {
        if(imgUrl == null || imgUrl.isBlank()) {
            return imgUrl;
        }

        if(imgUrl.startsWith("http://") || imgUrl.startsWith("https://")) {
            return imgUrl;
        }

        return base + imgUrl;
    }
}
