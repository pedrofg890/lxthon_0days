package lxthon.backend.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lxthon.backend.Domain.TranscriptSegment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class TranscriptCleanerServiceTest {

    @Test
    public void testCleanTranscriptWithSampleInput() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // 1) Carrega sample_input.json dos resources
        InputStream is = getClass().getClassLoader().getResourceAsStream("sample_input.json");
        assertNotNull(is, "sample_input.json not found in src/test/resources");

        List<TranscriptSegment> segments = mapper.readValue(
                is,
                new TypeReference<List<TranscriptSegment>>() {}
        );

        // 2) Mocka o OpenAIService para devolver apenas o texto após 'Target:' em upper-case
        OpenAIService mockAI = Mockito.mock(OpenAIService.class);
        Mockito.when(mockAI.getChatCompletion(Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String prompt = invocation.getArgument(0, String.class);
                    Matcher m = Pattern.compile("Target: (.*?)\\nNormalized").matcher(prompt);
                    if (m.find()) {
                        return m.group(1).toUpperCase();
                    }
                    return "";
                });

        // 3) Instancia o service com o mock
        TranscriptCleanerService cleaner = new TranscriptCleanerService(mockAI);

        // 4) Executa a limpeza
        List<TranscriptSegment> cleaned = cleaner.cleanTranscript(segments);

        // 5) Verificações
        assertEquals(segments.size(), cleaned.size());
        for (int i = 0; i < segments.size(); i++) {
            TranscriptSegment original = segments.get(i);
            TranscriptSegment out = cleaned.get(i);
            assertEquals(original.getStartTime(),  out.getStartTime());
            assertEquals(original.getEndTime(),    out.getEndTime());
            assertEquals(original.getText(),       out.getText());
            assertEquals(original.getText().toUpperCase(), out.getNormalizedText(),
                    "normalizedText deve ser o texto original em upper-case");
        }
    }
}
