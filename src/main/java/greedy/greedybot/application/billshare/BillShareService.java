package greedy.greedybot.application.billshare;

import greedy.greedybot.domain.billshare.CustomBillShare;
import greedy.greedybot.domain.billshare.EqualBillShare;
import org.springframework.stereotype.Service;

@Service
public class BillShareService {
    public EqualBillShareResponse equalBillShare(EqualBillShare request) {
        String bankName = request.getBankInfo().getBankName();
        String account = request.getAccountNumber();
        String fullAccount = bankName + " " + account;

        String paymentLink = "supertoss://send?bankCode=%s&accountNo=%s"
                .formatted(request.getBankInfo().getBankCode(), account);

        return new EqualBillShareResponse(
                request.getTotalAmount(),
                request.getMemberCount(),
                paymentLink,
                fullAccount,
                request.getMemberName()
        );
    }

    public CustomBillShareResponse customBillShare(CustomBillShare request) {
        String bankName = request.getBankInfo().getBankName();
        String account = request.getAccountNumber();
        String fullAccount = bankName + " " + account;

        String paymentLink = "supertoss://send?bankCode=%s&accountNo=%s"
                .formatted(request.getBankInfo().getBankCode(), account);

        return new CustomBillShareResponse(
                request.getText(),
                paymentLink,
                fullAccount
        );
    }
}
