//package lxthon.backend;
//
//import lxthon.backend.Domain.TranscriptSegment;
//import lxthon.backend.Domain.Quiz;
//import lxthon.backend.Service.TranscriptCleanerService;
//import lxthon.backend.Service.QuizGeneratorService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class RealIntegrationTest {
//
//    @Autowired
//    private TranscriptCleanerService transcriptCleanerService;
//
//    @Autowired
//    private QuizGeneratorService quizGeneratorService;
//
////    @Test
////    public void testTranscriptCleaningAndQuizGeneration() throws Exception {
////        // Carrega o sample do ficheiro JSON
////        ObjectMapper objectMapper = new ObjectMapper();
////        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sample.json");
////        assertNotNull(inputStream, "Ficheiro sample.json não encontrado");
////        List<TranscriptSegment> transcript = objectMapper.readValue(inputStream, new TypeReference<>() {});
////
////
////        // Limpa o transcript e gera o quiz
////        List<TranscriptSegment> cleanedTranscript = transcriptCleanerService.cleanTranscript(transcript);
////        Quiz quiz = quizGeneratorService.generateQuiz(cleanedTranscript);
////
////        // Validações básicas
////        assertNotNull(quiz);
////        assertNotNull(quiz.getTitle());
////        assertFalse(quiz.getQuestions().isEmpty(), "O quiz não tem questões");
////    }
//
//    @Test
//    void testTranscriptCleaningAndQuizGeneration2() throws IOException {
//        // Carrega o ficheiro sample como lista de TranscriptSegment
//        ObjectMapper mapper = new ObjectMapper();
//        InputStream inputStream = getClass().getResourceAsStream("/sample_transcript.json");
//        List<TranscriptSegment> transcriptSegments = mapper.readValue(
//                inputStream,
//                new TypeReference<List<TranscriptSegment>>() {}
//        );
//
//        // Usa o serviço de limpeza
//        List<TranscriptSegment> cleanedSegments = transcriptCleanerService.cleanTranscript(transcriptSegments);
//
//        // Gera o quiz com 3 perguntas
//        Quiz quiz = quizGeneratorService.generateQuiz(cleanedSegments, 3);
//
//        // Verificações simples (podes expandir)
//        assertNotNull(quiz);
//        assertNotNull(quiz.getTitle());
//        assertFalse(quiz.getQuestions().isEmpty());
//
//        System.out.println("Quiz Title: " + quiz.getTitle());
//        quiz.getQuestions().forEach(q -> {
//            System.out.println("Q: " + q.getQuestionText());
//            System.out.println("Options: " + q.getOptions());
//            System.out.println("Correct Index: " + q.getCorrectOptionIndex());
//        });
//    }
//
//}
