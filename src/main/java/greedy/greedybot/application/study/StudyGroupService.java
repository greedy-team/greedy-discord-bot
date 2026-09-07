package greedy.greedybot.application.study;

import greedy.greedybot.common.exception.GreedyBotException;
import greedy.greedybot.domain.study.StudyGroup;
import greedy.greedybot.domain.study.StudyGroupMapper;
import greedy.greedybot.domain.study.StudyGroupRepository;
import greedy.greedybot.domain.study.StudyRole;
import greedy.greedybot.domain.study.StudyType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class StudyGroupService {

    private static final Comparator<StudyGroup> DISPLAY_ORDER =
        Comparator.<StudyGroup, StudyType>comparing(StudyGroup::type)
            .thenComparing(Comparator.comparingInt(StudyGroup::generation).reversed())
            .thenComparing(StudyGroup::id);

    private static final Logger log = LoggerFactory.getLogger(StudyGroupService.class);

    // 자동 완성은 키 입력마다 호출되기 때문에 매번 채널 히스토리를 조회하지 않도록 캐싱한다.
    // 등록/삭제는 이 서비스를 거치므로 쓰기 시점에 캐시도 함께 갱신한다.
    // 채널 메세지를 직접 수정한 경우에는 봇을 재시작해야 반영된다.
    private final AtomicReference<List<StudyGroup>> cachedStudyGroups = new AtomicReference<>();

    private final StudyGroupRepository studyGroupRepository;
    private final StudyGroupMapper studyGroupMapper;

    public StudyGroupService(final StudyGroupRepository studyGroupRepository,
                             final StudyGroupMapper studyGroupMapper) {
        this.studyGroupRepository = studyGroupRepository;
        this.studyGroupMapper = studyGroupMapper;
    }

    public StudyGroup createStudyGroup(final String id,
                                       final StudyRole role,
                                       final StudyType type,
                                       final int generation,
                                       final String rawMembers) {
        final StudyGroup studyGroup = new StudyGroup(
            id.trim(), role, type, generation, studyGroupMapper.parseMembers(rawMembers));
        validateNotDuplicated(studyGroup.id());

        studyGroupRepository.saveStudyGroup(studyGroup);
        addToCache(studyGroup);
        log.info("[STUDY GROUP CREATED] : {}", studyGroup.id());
        return studyGroup;
    }

    public StudyGroup deleteStudyGroup(final String id) {
        final StudyGroup studyGroup = getById(id);

        studyGroupRepository.deleteById(studyGroup.id());
        removeFromCache(studyGroup.id());
        log.info("[STUDY GROUP DELETED] : {}", studyGroup.id());
        return studyGroup;
    }

    public List<StudyGroup> findAll() {
        return studyGroups().stream()
            .sorted(DISPLAY_ORDER)
            .toList();
    }

    public List<StudyGroup> findAllByRole(final StudyRole role) {
        return studyGroups().stream()
            .filter(studyGroup -> studyGroup.hasRole(role))
            .sorted(DISPLAY_ORDER)
            .toList();
    }

    public StudyGroup getByIdAndRole(final String id, final StudyRole role) {
        final StudyGroup studyGroup = getById(id);
        if (!studyGroup.hasRole(role)) {
            log.warn("[STUDY GROUP ROLE MISMATCH] : {}", id);
            throw new GreedyBotException("🚫 %s 는 %s 그룹이 아닙니다.".formatted(id, role.label()));
        }
        return studyGroup;
    }

    // 리뷰이 그룹과 리뷰어 그룹은 같은 스터디(타입 + 기수)에 속해야 한다
    public void validateSameStudy(final StudyGroup revieweeGroup, final StudyGroup reviewerGroup) {
        if (!revieweeGroup.isSameStudyWith(reviewerGroup)) {
            log.warn("[REVIEWER AND REVIEWEE STUDY MISMATCH] : {} {}", revieweeGroup.id(), reviewerGroup.id());
            throw new GreedyBotException("🚫 리뷰어 리뷰이 스터디 정보가 일치 하지 않습니다. (%s / %s)"
                .formatted(revieweeGroup.studyName(), reviewerGroup.studyName()));
        }
    }

    private StudyGroup getById(final String id) {
        return studyGroups().stream()
            .filter(studyGroup -> studyGroup.id().equals(id))
            .findAny()
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 스터디 그룹입니다: " + id));
    }

    private void validateNotDuplicated(final String id) {
        final boolean duplicated = studyGroups().stream()
            .anyMatch(studyGroup -> studyGroup.id().equals(id));
        if (duplicated) {
            log.warn("[DUPLICATED STUDY GROUP ID] : {}", id);
            throw new GreedyBotException("🚫 이미 존재하는 스터디 그룹 id 입니다: " + id);
        }
    }

    private List<StudyGroup> studyGroups() {
        final List<StudyGroup> cached = cachedStudyGroups.get();
        if (cached != null) {
            return cached;
        }
        final List<StudyGroup> loaded = List.copyOf(studyGroupRepository.findAll());
        cachedStudyGroups.set(loaded);
        log.info("[STUDY GROUP CACHE LOADED] : {}", loaded.size());
        return loaded;
    }

    private void addToCache(final StudyGroup studyGroup) {
        final List<StudyGroup> updated = new ArrayList<>(studyGroups());
        updated.add(studyGroup);
        cachedStudyGroups.set(List.copyOf(updated));
    }

    private void removeFromCache(final String id) {
        cachedStudyGroups.set(studyGroups().stream()
            .filter(studyGroup -> !studyGroup.id().equals(id))
            .toList());
    }
}
