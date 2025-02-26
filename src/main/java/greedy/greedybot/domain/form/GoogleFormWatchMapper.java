package greedy.greedybot.domain.form;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class GoogleFormWatchMapper {

    public String toTextEntity(final GoogleFormWatch googleFormWatch) {
        return Arrays.stream(googleFormWatch.getClass().getRecordComponents())
                .map(component -> formatField(googleFormWatch, component))
                .collect(Collectors.joining(","));
    }

    private String formatField(final Record record, final RecordComponent component) {
        try {
            final Object value = component.getAccessor().invoke(record);
            return component.getName() + ":" + value;
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert record to string", e);
        }
    }

    public GoogleFormWatch toEntity(final String text) {
        final String targetFormId = findValueByKey(text, "targetFormId");
        final String targetFormTitle = findValueByKey(text, "targetFormTitle");
        final int responseCount = Integer.parseInt(findValueByKey(text, "responseCount"));
        return new GoogleFormWatch(targetFormId, targetFormTitle, responseCount);
    }

    private String findValueByKey(final String text, final String key) {
        return Arrays.stream(text.split(","))
                .filter(field -> field.contains(key))
                .map(field -> field.split(":")[1])
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Not Found Key"));
    }
}
