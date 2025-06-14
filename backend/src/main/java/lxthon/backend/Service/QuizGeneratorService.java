package lxthon.backend.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.Quiz;

import java.io.IOException;

/**
 * Service responsible for generating a multiple-choice {@link Quiz} from a given text.
 * <p>
 * It sends a prompt to the configured OpenAIService, instructing the model to
 * return a raw JSON object containing a quiz title and an array of questions.
 * </p>
 */
@Service
public class QuizGeneratorService {
    private final OpenAIService openAIService;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * The system prompt template used to instruct the model on quiz creation.
     * <p>
     * %d will be replaced by the requested number of questions.
     * The model is explicitly told to return only the JSON object, without
     * any explanatory text or markdown formatting.
     * </p>
     */
    private static final String SYSTEM_PROMPT =
            "You are a quiz generator. Given the following text, create a quiz with %d multiple-choice questions. " +
                    "Provide a short descriptive 'title' for the quiz. " +
                    "For each question, include:\n" +
                    "- id (integer starting from 1)\n" +
                    "- question (string)\n" +
                    "- choices (array of 4 distinct strings)\n" +
                    "- correctIndex (integer 0–3)\n\n" +
                    "IMPORTANT: Return ONLY the raw JSON object with two fields, 'title' and 'questions'. " +
                    "Do NOT include any explanations, markdown, or leading text.";

    /**
     * Constructs a new {@code QuizGeneratorService} with the given {@link OpenAIService}.
     *
     * @param openAIService the service used to send prompts and receive completions
     */
    public QuizGeneratorService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Generates a {@link Quiz} based on the provided cleaned transcript text.
     * <p>
     * It formats the system prompt with the desired number of questions,
     * appends the transcript text, and sends it to the language model.
     * The response is expected to be a raw JSON object. Any leading markdown
     * fences are stripped before parsing.
     * </p>
     *
     * @param cleanedTranscript the cleaned transcript or summary text to base the quiz on
     * @param numQuestions      the number of multiple-choice questions to generate
     * @return a {@link Quiz} object parsed from the model’s JSON response
     * @throws IOException                  if an error occurs during JSON parsing
     * @throws IllegalArgumentException     if the API response is null or empty
     */
    public Quiz generateQuiz(String cleanedTranscript, int numQuestions) throws IOException {
        String prompt = String.format(SYSTEM_PROMPT, numQuestions) + "\n\n" + cleanedTranscript;

        String response = openAIService.getChatCompletion(prompt);

        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Resposta da API veio vazia.");
        }

        response = response.trim();
        if (response.startsWith("```")) {
            response = response.replaceAll("```(json)?", "").replaceAll("```", "").trim();
        }

        return mapper.readValue(response, Quiz.class);
    }

}
