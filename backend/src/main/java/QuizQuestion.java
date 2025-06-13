import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {
    @Getter
    @Setter
    private String id;
    @Getter
    @Setter
    String questionText;
    @Getter
    @Setter
    List<String> options;
    @Getter
    @Setter
    int correctOptionIndex;
}
