package greedy.greedybot.application.billshare;

public class EqualBillShareResponse {
    private final int totalAmount;
    private final int memberCount;
    private final String memberName;
    private final int amount;
    private final String paymentLink;
    private final String fullAccountInfo;

    public EqualBillShareResponse(int totalAmount, int memberCount, String paymentLink, String fullAccountInfo, String memberName) {
        this.totalAmount = totalAmount;
        this.memberCount = memberCount;
        this.memberName = memberName;
        this.amount = totalAmount / (memberCount + 1);
        this.paymentLink = paymentLink;
        this.fullAccountInfo = fullAccountInfo;
    }

    public String toDiscordMessage() {
        return """
                - 총 금액: %,d원
                - 정산 인원: %s
                👉 1인당 금액: `%d`원
                
                🏦 계좌정보: `%s`
                """.formatted(totalAmount, memberName, amount, fullAccountInfo);
    }
//
//    public String toDiscordMessage() {
//        return """
//                💸 총 금액: %,d원
//                👥 인원수: %d명
//                1인당 금액: '%,d'원
//
//                🏦 계좌정보: `%s`
//                🔗 토스 간편결제: %s
//                """.formatted(totalAmount, memberCount, amount, fullAccountInfo, paymentLink);
//    }
}
