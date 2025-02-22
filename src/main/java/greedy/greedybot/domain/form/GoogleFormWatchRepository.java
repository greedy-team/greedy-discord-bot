package greedy.greedybot.domain.form;

import java.util.List;
import java.util.Optional;

public interface GoogleFormWatchRepository {

    void saveGoogleFormWatch(final GoogleFormWatch googleFormWatch);

    void deleteByFormId(final String formId);

    Optional<GoogleFormWatch> findByFormId(final String formId);

    List<GoogleFormWatch> findAll();

    void updateGoogleFormWatch(final GoogleFormWatch googleFormWatch);
}
