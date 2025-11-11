package greedy.greedybot.application.matching;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RandomShuffleStrategy implements ShuffleStrategy {

    @Override
    public void shuffle(List<String> list) {
        Collections.shuffle(list);
    }
}
