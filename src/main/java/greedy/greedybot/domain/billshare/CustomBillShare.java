package greedy.greedybot.domain.billshare;

public class CustomBillShare {

    private final String text;
    private final BankInfo bankInfo;
    private final String accountNumber;

    public CustomBillShare(String text, BankInfo bankInfo, String accountNumber) {
        this.text = text;
        this.bankInfo = bankInfo;
        this.accountNumber = accountNumber;
    }

    public String getText() {
        return text;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}
