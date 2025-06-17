package greedy.greedybot.domain.billshare;

public enum BankInfo {

    KYEONGNAM("경남은행", "039"),
    GWANGJU("광주은행", "034"),
    KOOKMIN("국민은행", "004"),
    IBK("기업은행", "003"),
    NONGHYUP("농협은행", "011"),
    DAEGU("대구은행", "031"),
    BUSAN("부산은행", "032"),
    SAEMAEUL("새마을금고", "045"),
    SUHYUP("수협은행", "007"),
    SHINHAN("신한은행", "088"),
    SHINHYEOP("신협", "048"),
    CITI("씨티은행", "027"),
    WOORI("우리은행", "020"),
    POST("우체국", "071"),
    JEONBUK("전북은행", "037"),
    JEJU("제주은행", "035"),
    KAKAOBANK("카카오뱅크", "090"),
    TOSSBANK("토스뱅크", "092"),
    HANA("하나은행", "081"),
    SC("SC제일은행", "023");

    private final String bankName;
    private final String bankCode;

    BankInfo(String bankName, String bankCode) {
        this.bankName = bankName;
        this.bankCode = bankCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankName() {
        return bankName;
    }

}
