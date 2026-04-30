package digibinder.exception;

/**
 * exceção lançada quando ocorre um erro de acesso ou operação no banco de dados
 */
public class DatabaseException extends DigibinderException {

    public DatabaseException(String mensagem) {
        super(mensagem);
    }

    public DatabaseException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
