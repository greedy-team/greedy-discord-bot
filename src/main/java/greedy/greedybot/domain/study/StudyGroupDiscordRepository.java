package greedy.greedybot.domain.study;

import greedy.greedybot.common.exception.GreedyBotException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

@Repository
public class StudyGroupDiscordRepository implements StudyGroupRepository {

    private static final int MAX_HISTORY_SIZE = 100;

    private final TextChannel studyGroupChannel;
    private final StudyGroupMapper studyGroupMapper;

    public StudyGroupDiscordRepository(@Lazy final TextChannel studyGroupChannel,
                                       final StudyGroupMapper studyGroupMapper) {
        this.studyGroupChannel = studyGroupChannel;
        this.studyGroupMapper = studyGroupMapper;
    }

    @Override
    public void saveStudyGroup(final StudyGroup studyGroup) {
        final String studyGroupText = studyGroupMapper.toTextEntity(studyGroup);
        studyGroupChannel.sendMessage(studyGroupText).queue();
    }

    @Override
    public void deleteById(final String id) {
        findMessageById(id)
            .orElseThrow(() -> new GreedyBotException("🚫 존재하지 않는 스터디 그룹입니다: " + id))
            .delete()
            .queue();
    }

    @Override
    public Optional<StudyGroup> findById(final String id) {
        return findMessageById(id)
            .map(message -> studyGroupMapper.toEntity(message.getContentDisplay()));
    }

    @Override
    public List<StudyGroup> findAll() {
        return studyGroupMessages()
            .map(message -> studyGroupMapper.toEntity(message.getContentDisplay()))
            .toList();
    }

    // id 가 다른 id 의 접두사일 수 있으므로(be-4, be-4-java) 부분 문자열이 아닌 id 값으로 정확히 비교한다
    private Optional<Message> findMessageById(final String id) {
        return studyGroupMessages()
            .filter(message -> studyGroupMapper.toEntity(message.getContentDisplay()).id().equals(id))
            .findAny();
    }

    private Stream<Message> studyGroupMessages() {
        return studyGroupChannel.getHistory().retrievePast(MAX_HISTORY_SIZE).complete()
            .stream()
            .filter(message -> studyGroupMapper.isStudyGroupText(message.getContentDisplay()));
    }
}
