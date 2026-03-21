package greedy.greedybot.domain.birthday;

public enum BirthdayReason {
    FEAT("최초 등록"),
    FIX("생일 잘못 입력");

    private final String label;

    BirthdayReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
