package greedy.greedybot.domain.study;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository {

    void saveStudyGroup(StudyGroup studyGroup);

    void deleteById(String id);

    Optional<StudyGroup> findById(String id);

    List<StudyGroup> findAll();
}
