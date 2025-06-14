package lxthon.backend.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class QuizQuestion {
    @JsonProperty("id")
    private String id;
    @JsonProperty("question")
    String questionText;
    @JsonProperty("choices")
    List<String> options;
    @JsonProperty("correctIndex")
    int correctOptionIndex;
}
