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
import reactor.core.publisher.Mono;

@Service
public class OpenAIService {

    private final String key;
    private final String endpoint;
    private final String model;
    private final ChatCompletionsAsyncClient client;

    public OpenAIService() {
        this.key = System.getProperty("OPENAI_API_KEY");
        this.endpoint = "https://models.github.ai/inference";
        this.model = "meta/Llama-4-Scout-17B-16E-Instruct";

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
