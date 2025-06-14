package lxthon.backend.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a quiz generated from a transcript or any given text.
 * <p>
 * A quiz consists of a descriptive title and a collection of multiple-choice questions.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Quiz {
    /**
     * The descriptive title of the quiz.
     * <p>
     * Example: "Digital Security Fundamentals Quiz".
     * </p>
     */
    @JsonProperty("title")
    String title;
    /**
     * The list of questions that make up this quiz.
     * Each element is a {@link QuizQuestion} containing the question text,
     * a set of possible answers, and the index of the correct option.
     */
    @JsonProperty("questions")
    List<QuizQuestion> questions;
}
