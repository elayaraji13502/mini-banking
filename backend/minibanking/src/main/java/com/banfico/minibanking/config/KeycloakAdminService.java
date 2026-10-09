package com.banfico.minibanking.config;

import com.banfico.minibanking.exception.DuplicateResourceException;
import com.banfico.minibanking.exception.KeycloakProvisioningException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String adminBaseUrl;
    private final String adminRealm;
    private final String targetRealm;
    private final String clientId;
    private final String username;
    private final String password;

    public KeycloakAdminService(
            org.springframework.core.env.Environment environment
    ) {
        this.objectMapper = new ObjectMapper();
        this.adminBaseUrl = stripTrailingSlash(
                environment.getProperty("keycloak.admin.base-url", "http://localhost:8080")
        );
        this.adminRealm = environment.getProperty("keycloak.admin.realm", "master");
        this.targetRealm = environment.getProperty("keycloak.admin.target-realm", "banfico-banking");
        this.clientId = environment.getProperty("keycloak.admin.client-id", "admin-cli");
        this.username = environment.getProperty("keycloak.admin.username", "admin");
        this.password = environment.getProperty("keycloak.admin.password", "admin");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String createUser(
            String keycloakUsername,
            String initialPassword,
            String displayName,
            String email
    ) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(adminUrl("/admin/realms/" + targetRealm + "/users")))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + getAdminToken())
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(buildUserPayload(
                                    keycloakUsername,
                                    displayName,
                                    email
                            ))
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 201) {
                                String userId = extractUserId(response);
                                setPassword(userId, initialPassword);
                                return userId;
            }

            if (response.statusCode() == 409) {
                throw new DuplicateResourceException(
                        "Keycloak user already exists for username: " + keycloakUsername
                );
            }

            throw new KeycloakProvisioningException(
                    "Failed to create Keycloak user. Status: " + response.statusCode()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KeycloakProvisioningException(
                    "Keycloak user creation was interrupted",
                    exception
            );
        } catch (IOException exception) {
            throw new KeycloakProvisioningException(
                    "Failed to communicate with Keycloak while creating the user",
                    exception
            );
        }
    }

    public void deleteUser(String userId) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(adminUrl("/admin/realms/" + targetRealm + "/users/" + userId)))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + getAdminToken())
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 204 || response.statusCode() == 404) {
                return;
            }

            throw new KeycloakProvisioningException(
                    "Failed to clean up Keycloak user. Status: " + response.statusCode()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KeycloakProvisioningException(
                    "Keycloak user cleanup was interrupted",
                    exception
            );
        } catch (IOException exception) {
            throw new KeycloakProvisioningException(
                    "Failed to communicate with Keycloak while deleting the user",
                    exception
            );
        }
    }

    private String getAdminToken() throws IOException, InterruptedException {

        String form = formEncode(Map.of(
                "grant_type", "password",
                "client_id", clientId,
                "username", username,
                "password", password
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(adminUrl("/realms/" + adminRealm + "/protocol/openid-connect/token")))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new KeycloakProvisioningException(
                    "Failed to authenticate against Keycloak admin endpoint. Status: "
                            + response.statusCode()
            );
        }

        JsonNode jsonNode = objectMapper.readTree(response.body());
        JsonNode tokenNode = jsonNode.get("access_token");
        if (tokenNode == null || tokenNode.asText().isBlank()) {
            throw new KeycloakProvisioningException(
                    "Keycloak admin token response did not include an access_token"
            );
        }

        return tokenNode.asText();
    }

    private Map<String, Object> buildUserPayload(
            String keycloakUsername,
            String displayName,
            String email
    ) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("username", keycloakUsername);
        payload.put("email", email);
        payload.put("firstName", displayName);
        payload.put("enabled", true);
        payload.put("emailVerified", true);

        return payload;
    }

    private void setPassword(
            String userId,
            String initialPassword
    ) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(adminUrl("/admin/realms/" + targetRealm + "/users/" + userId + "/reset-password")))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + getAdminToken())
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .PUT(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(Map.of(
                                    "type", "password",
                                    "value", initialPassword,
                                    "temporary", false
                            ))
                    ))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 204) {
                return;
            }

            throw new KeycloakProvisioningException(
                    "Failed to set Keycloak password. Status: " + response.statusCode()
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KeycloakProvisioningException(
                    "Keycloak password setup was interrupted",
                    exception
            );
        } catch (IOException exception) {
            throw new KeycloakProvisioningException(
                    "Failed to communicate with Keycloak while setting the password",
                    exception
            );
        }
    }

    private String extractUserId(HttpResponse<String> response) {

        return response.headers()
                .firstValue("Location")
                .map(location -> {
                    String trimmed = location.endsWith("/")
                            ? location.substring(0, location.length() - 1)
                            : location;
                    int lastSlash = trimmed.lastIndexOf('/');
                    if (lastSlash < 0 || lastSlash == trimmed.length() - 1) {
                        throw new KeycloakProvisioningException(
                                "Keycloak user creation succeeded but no user id was returned"
                        );
                    }
                    return trimmed.substring(lastSlash + 1);
                })
                .orElseThrow(() -> new KeycloakProvisioningException(
                        "Keycloak user creation succeeded but no Location header was returned"
                ));
    }

    private String adminUrl(String path) {
        return adminBaseUrl + path;
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String formEncode(Map<String, String> values) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }
}