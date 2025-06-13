package lxthon.backend.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lxthon.backend.Domain.TranscriptSegment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.InputStream;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TranscriptCleanerServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCleanTranscriptWithSampleInput() throws Exception {
        // 1) Carrega sample_input.json dos resources
        InputStream is = getClass().getResourceAsStream("/sample_input.json");
        List<TranscriptSegment> segments = mapper.readValue(
                is,
                new TypeReference<>(){}
        );

        // 2) Mocka o OpenAIService para não chamar a API de verdade
        OpenAIService mockAI = Mockito.mock(OpenAIService.class);
        // Faz o mock devolver sempre o texto original em uppercase, por exemplo
        Mockito.when(mockAI.getChatCompletion(Mockito.anyString()))
                .thenAnswer(inv -> ((String)inv.getArgument(0)).toUpperCase());

        // 3) Instancia o service com o mock
        TranscriptCleanerService cleaner = new TranscriptCleanerService(mockAI);

        // 4) Executa a limpeza
        List<TranscriptSegment> cleaned = cleaner.cleanTranscript(segments);

        // 5) Verifica que o normalizedText foi preenchido
        assertEquals(3, cleaned.size());
        for (int i = 0; i < segments.size(); i++) {
            TranscriptSegment original = segments.get(i);
            TranscriptSegment out = cleaned.get(i);

            assertEquals(original.getStartTime(), out.getStartTime());
            assertEquals(original.getEndTime(),   out.getEndTime());
            assertNotNull(out.getNormalizedText());
            // Como o mock retorna o prompt em upper-case, deve conter o texto original
            assertTrue(out.getNormalizedText().contains(original.getText().toUpperCase()));
        }

        // 6) (Opcional) imprime o resultado para veres o JSON
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cleaned));
    }
}
