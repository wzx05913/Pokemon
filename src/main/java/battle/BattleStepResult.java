package battle;

public class BattleStepResult {
    private boolean success;
    private String message;
    
    public BattleStepResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}