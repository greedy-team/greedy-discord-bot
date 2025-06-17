package greedy.greedybot.domain.billshare;

public class EqualBillShare {

    private final int memberCount;
    private final String memberName;
    private final int totalAmount;
    private final BankInfo bankInfo;
    private final String accountNumber;

    public EqualBillShare(int memberCount, int totalAmount, BankInfo bankInfo, String accountNumber, String memberName) {
        this.memberCount = memberCount;
        this.totalAmount = totalAmount;
        this.bankInfo = bankInfo;
        this.accountNumber = accountNumber;
        this.memberName = memberName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getMemberName() {
        return memberName;
    }
}
