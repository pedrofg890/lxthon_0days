package lxthon.backend.Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@NoArgsConstructor
public class Quiz {
    @Getter
    @Setter
    String title;
    @Getter
    @Setter
    List<QuizQuestion> questions;
}
