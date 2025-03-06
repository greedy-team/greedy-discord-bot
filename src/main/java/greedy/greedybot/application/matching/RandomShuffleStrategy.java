package greedy.greedybot.application.matching;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RandomShuffleStrategy implements ShuffleStrategy {
    @Override
    public void shuffle(List<String> list) {
        Collections.shuffle(list);
    }
}
