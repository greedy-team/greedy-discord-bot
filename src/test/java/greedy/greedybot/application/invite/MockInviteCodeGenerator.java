package greedy.greedybot.application.invite;

public class MockInviteCodeGenerator implements InviteCodeGenerator {

    private final String code;

    public MockInviteCodeGenerator(final String code) {
        this.code = code;
    }

    @Override
    public String generate() {
        return code;
    }
}
