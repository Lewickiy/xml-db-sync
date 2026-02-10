package ru.levitsky.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.validation.Validated;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("datasource.default")
public class DataSourceConfig {

    @NotBlank
    private String url;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
