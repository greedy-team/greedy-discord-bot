package greedy.greedybot.domain.form;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoogleFormWatchMapperTest {

    @Test
    void testToTextEntity() {
        // Given
        final GoogleFormWatch googleFormWatch = new GoogleFormWatch("adfbnoaasd", "안녕하세요", 0);
        final GoogleFormWatchMapper googleFormWatchMapper = new GoogleFormWatchMapper();

        // When
        final String result = googleFormWatchMapper.toTextEntity(googleFormWatch);

        // Then
        assertThat(result).contains("targetFormId:adfbnoaasd,targetFormTitle:안녕하세요,responseCount:0");
    }

}
