package ru.levitsky.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;

@Validated
@Introspected
@ConfigurationProperties("app")
public class AppConfig {

    @NotBlank
    private String catalogUrl;

    public String getCatalogUrl() {
        return catalogUrl;
    }

    public void setCatalogUrl(String catalogUrl) {
        this.catalogUrl = catalogUrl;
    }
}
