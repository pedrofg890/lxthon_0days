package lxthon.backend.Service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.core.credential.AzureKeyCredential;

@Service
public class OpenAIService {

    private final String key;
    private final String endpoint;
    private final String model;

    public OpenAIService() {
        this.key = System.getProperty("OPENAI_API_KEY"); // You set this using EnvLoader
        this.endpoint = "https://models.github.ai/inference";
        this.model = "openai/gpt-4o-mini";

        if (this.key == null || this.key.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is missing or blank.");
        }
    }

    public String getChatCompletion(String prompt) {
        ChatCompletionsClient client = new ChatCompletionsClientBuilder()
                .credential(new AzureKeyCredential(key))
                .endpoint(endpoint)
                .buildClient();

        String systemMessage = "You are a helpful assistant for a social media platform. " +
                "You should only answer questions related to social media features, user interactions, " +
                "content sharing, profiles, messaging, privacy settings, community guidelines, " +
                "and platform-specific functionality. " +
                "If a user asks about topics unrelated to social media or this platform, " +
                "politely redirect them to platform-related topics and suggest they contact " +
                "general support for other questions.";

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(systemMessage),
                new ChatRequestUserMessage(prompt)
        );

        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
        chatCompletionsOptions.setModel(model);

        ChatCompletions completions = client.complete(chatCompletionsOptions);

        return completions.getChoice().getMessage().getContent();
    }
}
