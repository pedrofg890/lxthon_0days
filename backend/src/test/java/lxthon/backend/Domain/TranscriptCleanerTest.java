package lxthon.backend.Domain;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TranscriptCleanerTest {

//    @Test
//    void cleanTranscript_removesFillersAndPreservesTimecodes() throws IOException {
//        // 1) Cria um mock do ChatClient
//        ChatClient mockClient = Mockito.mock(ChatClient.class);
//
//        // 2) Faz o mock devolver sempre "Hello world!" independentemente do prompt
//        Mockito.when(mockClient.normalize(Mockito.anyString()))
//                .thenReturn("Hello world!");
//
//        TranscriptCleaner cleaner = new TranscriptCleaner(mockClient);
//
//        // 3) Prepara dados de entrada
//        List<TranscriptSegment> input = List.of(
//                new TranscriptSegment(0.0, 2.0, "um test uh", null)
//        );
//
//        // 4) Executa
//        List<TranscriptSegment> output = cleaner.cleanTranscript(input);
//
//        // 5) Verificações
//        assertEquals(1, output.size());
//        TranscriptSegment seg = output.get(0);
//        assertEquals(0.0, seg.getStartTime());
//        assertEquals(2.0, seg.getEndTime());
//        assertEquals("um test uh", seg.getText());            // texto original inalterado
//        assertEquals("Hello world!", seg.getNormalizedText()); // texto normalizado pelo mock
//    }

}