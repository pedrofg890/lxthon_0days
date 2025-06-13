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
            "You are a quiz generator. Given a cleaned transcript (list of segments " +
                    "with startTime, endTime and text), create a quiz with %d multiple-choice questions. " +
                    "Provide a short descriptive 'title' for the quiz. " +
                    "For each question, include:\n" +
                    "- id (integer starting from 1)\n" +
                    "- question (string)\n" +
                    "- choices (array of 4 distinct strings)\n" +
                    "- correctIndex (integer 0–3)\n" +
                    "Return the result as a JSON object with two fields: 'title' (string) and 'questions' (array of QuizQuestion).";

    public QuizGeneratorService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    /**
     * Gera um Quiz com numQuestions perguntas e um título.
     */
    public Quiz generateQuiz(List<TranscriptSegment> segments, int numQuestions) throws IOException {
        // Monta prompt com SYSTEM + segmentos
        String prompt = String.format(SYSTEM_PROMPT, numQuestions) + "\n\n" +
                mapper.writeValueAsString(segments);

        String response = openAIService.getChatCompletion(prompt);

        // Converte JSON de resposta diretamente em Quiz (com title e questions)
        return mapper.readValue(response, Quiz.class);
    }
}
