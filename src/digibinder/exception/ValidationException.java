package digibinder.exception;

/**
 * exceção lançada quando os dados fornecidos pelo usuário não passam nas regras de validação
 */
public class ValidationException extends DigibinderException {

    public ValidationException(String mensagem) {
        super(mensagem);
    }
}
