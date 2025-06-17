package greedy.greedybot.application.billshare;

import greedy.greedybot.domain.billshare.BankInfo;

public class CustomBillShareResponse{
    private final String text;
    private final String fullAccountInfo;
    private final String paymentLink;

    public CustomBillShareResponse(String text, String fullAccountInfo, String paymentLink) {
        this.text = text;
        this.fullAccountInfo = fullAccountInfo;
        this.paymentLink = paymentLink;
    }

    public String toDiscordMessage() {
        return """
                👉 %s
                
                🏦 계좌정보: `%s`
                """.formatted(text, paymentLink);
    }
//
//    public String toDiscordMessage() {
//        return """
//                💸 정산 인원 및 금액: %s
//
//                🏦 계좌정보: `%s`
//                🔗 토스 간편결제: %s
//                """.formatted(text, fullAccountInfo, paymentLink);
//    }
}
