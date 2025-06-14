package lxthon.backend.Service;

import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import com.azure.ai.inference.ChatCompletionsAsyncClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;

/**
 * Service for obtaining chat completions from an LLM via the GitHub AI/Inference endpoint.
 * <p>
 * Configures an asynchronous Azure ChatCompletions client with long timeouts and exposes
 * a synchronous wrapper method for fetching completions given a single prompt string.
 * </p>
 */
@Service
public class OpenAIService {

    /**
     * The API key (injected as a JVM system property OPENAI_API_KEY).
     */
    private final String key;

    /**
     * The inference endpoint URL (e.g., GitHub AI Inference).
     */
    private final String endpoint;

    /**
     * The model identifier to use for completions.
     */
    private final String model;

    /**
     * The asynchronous chat completions client.
     */
    private final ChatCompletionsAsyncClient client;

    /**
     * Constructs the OpenAIService by reading the {@code OPENAI_API_KEY} system property,
     * configuring a Netty HTTP client with generous timeouts, and building an async
     * ChatCompletions client pointed at the specified endpoint and model.
     * <p>
     * Throws an {@link IllegalStateException} if the API key is missing or blank.
     * </p>
     */
    public OpenAIService() {
        this.key = System.getProperty("OPENAI_API_KEY");
        this.endpoint = "https://models.github.ai/inference";
        this.model = "microsoft/Phi-3.5-mini-instruct";

        if (this.key == null || this.key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is missing or blank.");
        }

        HttpClient nettyClient = new NettyAsyncHttpClientBuilder()
                .readTimeout(Duration.ofMinutes(5))
                .writeTimeout(Duration.ofMinutes(5))
                .responseTimeout(Duration.ofMinutes(5))
                .build();

        this.client = new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .httpClient(nettyClient)
                .buildAsyncClient();
    }

    /**
     * Sends the given prompt to the configured model and returns the completion text.
     * <p>
     * Wraps the async call in a blocking operation (up to 5 minutes) to simulate
     * synchronous behavior. Returns an empty string on any error or if no content
     * is received.
     * </p>
     *
     * @param prompt the user prompt to send to the language model
     * @return the completion text from the model, or an empty string on failure
     */
    public String getChatCompletion(String prompt) {
        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestUserMessage(prompt)
        );

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setModel(model);

        try {
            // Mono blocking here to simulate sync behavior
            ChatCompletions completions = client.complete(chatCompletionsOptions).block(Duration.ofMinutes(5));

            if (completions == null || completions.getChoice() == null) {
                return "";
            }

            String content = completions.getChoice().getMessage().getContent();
            return content != null ? content : "";
        } catch (Exception e) {
            System.err.println("Error calling OpenAI service: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}
