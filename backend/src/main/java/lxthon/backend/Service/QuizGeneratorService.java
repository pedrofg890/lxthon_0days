package lxthon.backend.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import lxthon.backend.Domain.Quiz;
import lxthon.backend.Domain.QuizQuestion;
import lxthon.backend.Domain.TranscriptSegment;

import java.io.IOException;
import java.util.List;

/**
 * QuizGeneratorService: cria um Quiz com base na transcrição limpa.
 * Agora inclui um título descritivo para o Quiz.
 */
@Service
public class QuizGeneratorService {
    private final OpenAIService openAIService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String SYSTEM_PROMPT =
            "You are a quiz generator. Given the following text, create a quiz with %d multiple-choice questions. " +
                    "Provide a short descriptive 'title' for the quiz. " +
                    "For each question, include:\n" +
                    "- id (integer starting from 1)\n" +
                    "- question (string)\n" +
                    "- choices (array of 4 distinct strings)\n" +
                    "- correctIndex (integer 0–3)\n" +
                    "Return the result as a JSON object with two fields: 'title' (string) and 'questions' (array of QuizQuestion).";
    //Constructor
    public QuizGeneratorService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Gera um Quiz com numQuestions perguntas e um título.
     */
    public Quiz generateQuiz(String cleanedTranscript, int numQuestions) throws IOException {
        String prompt = String.format(SYSTEM_PROMPT, numQuestions) + "\n\n" + cleanedTranscript;

        String response = openAIService.getChatCompletion(prompt);

        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Resposta da API veio vazia.");
        }

        return mapper.readValue(response, Quiz.class);
    }

}
