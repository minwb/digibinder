package digibinder.model;

import java.util.Objects;

/**
 * representa uma folha dentro do binder
 * cada página tem um formato de grade que define quantos cards cabem nela
 */
public class PaginaBinder {

    private Integer id;
    private Integer numeroPagina;
    private TipoLayout layout;
    private Binder binder; 

    /**
     * cria uma página vazia já com o formato padrão de 9 espaços (3x3)
     */
    public PaginaBinder() {
        this.layout = TipoLayout.GRID_3X3; 
    }

    /**
     * cria uma página definindo a numeração dela e o formato da grade
     * @param numeroPagina o número que essa página vai ter no fichário
     * @param layout o formato de organização indicando se cabem 4 ou 9 cards
     */
    public PaginaBinder(Integer numeroPagina, TipoLayout layout) {
        this(); 
        this.numeroPagina = numeroPagina;
        if (layout != null) {
            this.layout = layout;
        }
    }

    /**
     * construtor completo que traz o código do banco e liga a página no fichário dela
     * @param id código da página no banco de dados
     * @param numeroPagina número de ordem da página
     * @param layout formato da grade
     * @param binder o fichário virtual dono dessa página
     */
    public PaginaBinder(Integer id, Integer numeroPagina, TipoLayout layout, Binder binder) {
        this(numeroPagina, layout); 
        this.id = id;
        this.binder = binder;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumeroPagina() {
        return numeroPagina;
    }
    public void setNumeroPagina(Integer numeroPagina) {
        this.numeroPagina = numeroPagina;
    }

    public TipoLayout getLayout() {
        return layout;
    }
    public void setLayout(TipoLayout layout) {
        this.layout = layout;
    }

    public Binder getBinder() {
        return binder;
    }
    public void setBinder(Binder binder) {
        this.binder = binder;
    }

    /**
     * duas páginas são iguais se tiverem o mesmo código no banco
     * @param obj o outro objeto que a gente quer comparar
     * @return verdadeiro se for exatamente a mesma página
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) {
        	return false;
        }
        PaginaBinder that = (PaginaBinder) obj;
        return Objects.equals(id, that.id); // mesmo ID -> mesma página
    }

    /**
     * cria um identificador numérico único usando o id e o nome pra acelerar as pesquisas do sistema
     * @return o identificador numérico gerado
     */
    @Override
    public int hashCode() {
        return Objects.hash(id); 
    }

    /**
     * diz como a página deve ser escrita na tela 
     * @return texto amigável representando a página
     */
    @Override
    public String toString() {
        return "Página " + numeroPagina + " (" + layout + ")";
    }
}