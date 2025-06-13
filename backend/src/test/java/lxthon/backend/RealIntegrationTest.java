package lxthon.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lxthon.backend.Domain.TranscriptSegment;
import lxthon.backend.Domain.Quiz;
import lxthon.backend.Service.TranscriptCleanerService;
import lxthon.backend.Service.QuizGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RealIntegrationTest {

    @Value("classpath:sample_input.json")
    private Resource sampleInput;

    @Autowired
    private TranscriptCleanerService transcriptCleanerService;

    @Autowired
    private QuizGeneratorService quizGeneratorService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testTranscriptCleaningAndQuizGeneration() throws Exception {
        // 1) Carrega segmentos brutos do JSON de teste
        List<TranscriptSegment> rawSegments = mapper.readValue(
                sampleInput.getInputStream(),
                new TypeReference<List<TranscriptSegment>>() {}
        );
        assertFalse(rawSegments.isEmpty(), "sample_input.json deve conter segmentos");

        // 2) Limpa transcrição chamando a API real
        List<TranscriptSegment> cleaned = transcriptCleanerService.cleanTranscript(rawSegments);
        assertFalse(cleaned.isEmpty(), "Lista de segmentos limpos não deve estar vazia");
        System.out.println("Cleaned Transcript:\n" +
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cleaned));

        // 3) Gera quiz de 5 perguntas chamando a API real
        Quiz quiz = quizGeneratorService.generateQuiz(cleaned, 1);
        assertNotNull(quiz.getTitle(), "Quiz deve ter um título");
        assertEquals(5, quiz.getQuestions().size(), "Quiz deve ter 5 perguntas");
        System.out.println("Generated Quiz:\n" +
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(quiz));
    }
}
