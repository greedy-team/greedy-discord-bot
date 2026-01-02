package greedy.greedybot.domain.birthday;

import java.time.MonthDay;
import java.util.UUID;

public class Birthday {

    private final String id;
    private final String userId;
    private final String userName;
    private final MonthDay birthday;

    public Birthday(String userId, String userName, MonthDay birthday) {
        this.id = UUID.randomUUID().toString(); // 암호
        this.userId = userId;
        this.userName = userName;
        this.birthday = birthday;
    }

    public Birthday(String id, String userId, String userName, MonthDay birthday) {
        this.id = id; // 자동생성 x
        this.userId = userId;
        this.userName = userName;
        this.birthday = birthday;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public MonthDay getBirthday() {
        return birthday;
    }

    public boolean isSameMonthDay(int month, int day){
        return birthday.getMonthValue() == month && birthday.getDayOfMonth() == day;
    }
}