package digibinder.exception;

/**
 * exceção lançada quando o arquivo de imagem de um photocard não é encontrado
 */
public class ImageNotFoundException extends DigibinderException {

    private final String caminhoTentado;

    public ImageNotFoundException(String caminho) {
        super("Imagem não encontrada: " + caminho);
        this.caminhoTentado = caminho;
    }

    public String getCaminhoTentado() {
        return caminhoTentado;
    }
}