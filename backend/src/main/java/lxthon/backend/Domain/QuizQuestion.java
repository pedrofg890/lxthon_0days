package lxthon.backend.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Represents a single multiple-choice question within a quiz.
 * <p>
 * Each question has a unique identifier, the question text itself,
 * a list of possible answer options, and the index of the correct option.
 * </p>
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuizQuestion {
    /**
     * A unique identifier for this question.
     * <p>
     * This can be used to track or reference the question in submissions.
     * </p>
     */

    @JsonProperty("id")
    private String id;
    /**
     * The text of the question being asked.
     * <p>
     * Should clearly state what the user must answer.
     * </p>
     */
    @JsonProperty("question")
    String questionText;

    /**
     * A list of possible answer options for this question.
     * <p>
     * There should typically be exactly four distinct entries.
     * </p>
     */
    @JsonProperty("choices")
    List<String> options;

    /**
     * The zero-based index within {@link #options} that represents the correct answer.
     * <p>
     * For example, a value of 2 indicates that the third option is correct.
     * </p>
     */
    @JsonProperty("correctIndex")
    int correctOptionIndex;
}
