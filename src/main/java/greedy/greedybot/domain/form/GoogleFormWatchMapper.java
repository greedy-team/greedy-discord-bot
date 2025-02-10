package greedy.greedybot.domain.form;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GoogleFormWatchMapper {

    public String toTextEntity(GoogleFormWatch googleFormWatch) {
        return Arrays.stream(googleFormWatch.getClass().getRecordComponents())
                .map(component -> formatField(googleFormWatch, component))
                .collect(Collectors.joining(","));
    }

    private String formatField(Record record, RecordComponent component) {
        try {
            Object value = component.getAccessor().invoke(record);
            return component.getName() + ":" + value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert record to string", e);
        }
    }

    public GoogleFormWatch toEntity(String text) {
        String targetFormId = findValueByKey(text, "targetFormId");
        String targetFormTitle = findValueByKey(text, "targetFormTitle");
        int responseCount = Integer.parseInt(findValueByKey(text, "responseCount"));
        return new GoogleFormWatch(targetFormId, targetFormTitle, responseCount);
    }

    private String findValueByKey(String text, String key) {
        return Arrays.stream(text.split(","))
                .filter(field -> field.contains(key))
                .map(field -> field.split(":")[1])
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not Found Key"));
    }
}
