package it.grandimolini.aia.exception;

public class StabilimentoAccessDeniedException extends RuntimeException {
    public StabilimentoAccessDeniedException(String message) {
        super(message);
    }

    public StabilimentoAccessDeniedException(Long stabilimentoId, String username) {
        super(String.format("User '%s' does not have access to Stabilimento with ID: %d", username, stabilimentoId));
    }
}
