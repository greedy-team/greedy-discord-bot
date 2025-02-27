package greedy.greedybot.application.matching;

import greedy.greedybot.application.matching.dto.MatchingResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MatchingService {

    private final ShuffleStrategy shuffleStrategy;

    public MatchingService(ShuffleStrategy shuffleStrategy) {
        this.shuffleStrategy = shuffleStrategy;
    }

    public MatchingResult matchStudy(final List<String> reviewees, final List<String> reviewers) {

        shuffleStrategy.shuffle(reviewers);

        if (reviewees.size() >= reviewers.size()) {
            return matchWhenRevieweesAreMore(reviewees, reviewers);
        }

        return matchWhenRevieweesAreFewer(reviewees, reviewers);
    }

    // 리뷰이가 리뷰어보다 적을때 랜덤 매칭
    // 리뷰이 사람 수 만큼 리뷰어를 랜덤 선택하여 매칭 결과 생성
    private MatchingResult matchWhenRevieweesAreFewer(final List<String> reviewees, final List<String> randomReviewers) {
        // { 리뷰어1 : [리뷰이1] } 형태로 매칭 결과 표현
        final Map<String, List<String>> matchingResult = new HashMap<>();

        int index = 0;
        for (final String reviewee : reviewees) {
            // 리뷰어에게 할당되는 리뷰이
            final List<String> singleAssignedReviewee = List.of(reviewee);

            matchingResult.put(randomReviewers.get(index++), singleAssignedReviewee);
        }

        return new MatchingResult(matchingResult);
    }

    // 리뷰이가 리뷰어보다 많을때 랜덤 매칭
    // 리뷰어가 같은 숫자로 리뷰이를 나눠 가진 다음 남은 리뷰이를 랜덤으로 한명씩 갖는 함수
    private MatchingResult matchWhenRevieweesAreMore(final List<String> reviewees, final List<String> randomReviewers) {
        // { 리뷰어1: [리뷰이1, 리뷰이2] } 형태로 매칭 결과 표현
        final Map<String, List<String>> matchingResult = new HashMap<>();

        final int revieweeSize = reviewees.size();
        final int reviewerSize = randomReviewers.size();

        // 공평하게 나눠 가질 리뷰이 숫자를 나타내는 변수
        final int baseCount = revieweeSize / reviewerSize;
        // 공평하게 나눠 갖고 남은 리뷰이 숫자를 나타내는 변수
        int remainderCount = revieweeSize % reviewerSize;

        int index = 0;

        for (final String reviewer : randomReviewers) {
            // 리뷰어에게 몇명의 리뷰이를 할당할지 나타내는 변수
            final int assignCount = getAssignCount(baseCount, remainderCount);
            remainderCount--;

            // 리뷰어에게 할당되는 리뷰이들을 나타내는 변수
            final List<String> assignedReviewees = reviewees.subList(index, index + assignCount);
            index += assignCount;

            matchingResult.put(reviewer, assignedReviewees);
        }
        return new MatchingResult(matchingResult);
    }

    // 남은 리뷰이 숫자가 존재한다면 리뷰어에게 한명의 리뷰이를 더 배정하는 함수
    private int getAssignCount(final int baseCount, final int remainderCount) {
        if (remainderCount > 0) {
            return baseCount + 1;
        }

        return baseCount;
    }
}
