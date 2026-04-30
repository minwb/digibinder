package digibinder.model;

/**
 * define as opções de status que um photocard pode ter na coleção
 * ajuda o usuário a controlar o que ele já possui, o que é desejado e o que é para troca
 */
public enum StatusPhotocard {
    ADQUIRIDO("Adquirido", "Tenho"), 
    DESEJADO("Desejado", "Wishlist"),
    PARA_TROCA("Para Troca", "Trocando");

    private final String nome;
    private final String descricao;

    StatusPhotocard(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    
    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return nome;
    }
}