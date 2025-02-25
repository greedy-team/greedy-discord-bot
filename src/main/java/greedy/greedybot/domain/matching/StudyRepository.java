package greedy.greedybot.domain.matching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyRepository {

    private static final Logger log = LoggerFactory.getLogger(StudyRepository.class);

    private final BackendStudy backendStudy;
    private final FrontendStudy frontendStudy;

    public StudyRepository(final BackendStudy backendStudy, final FrontendStudy frontendStudy) {
        this.backendStudy = backendStudy;
        this.frontendStudy = frontendStudy;
    }

    public Study findStudyTypeByGroupRoleId(String groupRoleId) {
        final List<Study> studies = List.of(backendStudy, frontendStudy);
        return studies.stream()
                .filter(study -> study.getGroupRoleId().equals(groupRoleId))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Incorrect study group");
                    return new StudyNotMatchException("잘못된 스터디를 설정하였습니다.");
                });
    }
}
