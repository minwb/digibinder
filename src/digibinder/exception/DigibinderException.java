package digibinder.exception;

/**
 * exceção base do sistema DigiBinder
 * todas as exceções customizadas do projeto herdam desta classe (captura genérica)
 */
public class DigibinderException extends Exception {

    public DigibinderException(String mensagem) {
        super(mensagem);
    }

    public DigibinderException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
