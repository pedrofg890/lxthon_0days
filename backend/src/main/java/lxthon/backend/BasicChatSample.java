package lxthon.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class BasicChatSample {
    public static void main(String[] args) {
        // Start the Spring application
        ApplicationContext context = SpringApplication.run(BasicChatSample.class, args);
        
        // Get the OpenAIService bean
        lxthon.backend.service.OpenAIService openAIService = context.getBean(lxthon.backend.service.OpenAIService.class);
        
        // Test the chat completion
        String prompt = "Can you explain the basics of machine learning?";
        String response = openAIService.getChatCompletion(prompt);
        
        System.out.println("Response from OpenAI:");
        System.out.println(response);
    }
}
