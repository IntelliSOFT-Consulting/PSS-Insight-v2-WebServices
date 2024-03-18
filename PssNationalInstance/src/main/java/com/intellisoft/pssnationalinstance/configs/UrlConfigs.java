package com.intellisoft.pssnationalinstance.configs;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class UrlConfigs {
    public static final String INTERNATIONAL_BASE_URL;

    static {
        String defaultBasePssUrl = "https://global.pssinsight.org";
        String configuredPssUrl = System.getProperty("dhis.international");

        INTERNATIONAL_BASE_URL = configuredPssUrl != null ? configuredPssUrl : defaultBasePssUrl;
    }

    public static String getInternationalBaseUrl() {
        return INTERNATIONAL_BASE_URL;
    }
}

