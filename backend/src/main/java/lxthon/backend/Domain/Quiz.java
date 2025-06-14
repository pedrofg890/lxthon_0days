package lxthon.backend.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Quiz {
    @JsonProperty("title")
    String title;
    @JsonProperty("questions")
    List<QuizQuestion> questions;
}
