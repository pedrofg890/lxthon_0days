package lxthon.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for connecting to the OpenAI API.
 * <p>
 * Reads properties prefixed with <code>openai</code> from <code>application.properties</code>
 * or environment variables. Provides the API key, endpoint URL, and model identifier.
 * </p>
 */
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    /**
     * The API key used to authenticate requests to OpenAI.
     * <p>
     * Corresponds to the property <code>openai.api-key</code>.
     * </p>
     */
    private String apiKey;

    /**
     * The base URL for the OpenAI API endpoint.
     * <p>
     * Corresponds to the property <code>openai.endpoint</code>.
     * Example: <code>https://api.openai.com/v1/chat/completions</code>
     * </p>
     */
    private String endpoint;

    /**
     * The identifier of the model to use for completions.
     * <p>
     * Corresponds to the property <code>openai.model</code>.
     * Example: <code>gpt-4</code> or <code>gpt-4-o-mini</code>.
     * </p>
     */
    private String model;



    /**
     * Returns the configured API key.
     *
     * @return the API key for OpenAI
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key for OpenAI.
     *
     * @param apiKey the API key to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the configured endpoint URL.
     *
     * @return the OpenAI API endpoint URL
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the OpenAI API endpoint URL.
     *
     * @param endpoint the endpoint URL to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Returns the configured model identifier.
     *
     * @return the model ID for completions
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model identifier for completions.
     *
     * @param model the model ID to set
     */
    public void setModel(String model) {
        this.model = model;
    }
}
