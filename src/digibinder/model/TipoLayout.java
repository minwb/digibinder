package digibinder.model;

/**
 * define os formatos de folha disponíveis para o binder
 * página com grade de 4 espaços (2x2) ou de 9 espaços (3x3)
 */
public enum TipoLayout {
    GRID_2X2(2, 2, "2x2 (4 slots)"), 
    GRID_3X3(3, 3, "3x3 (9 slots)");

    private final int linhas;
    private final int colunas;
    private final String descricao;

    TipoLayout(int linhas, int colunas, String descricao) {
        this.linhas = linhas;
        this.colunas = colunas;
        this.descricao = descricao;
    }
    
    public int getLinhas() {
        return linhas;
    }

    public int getColunas() {
        return colunas;
    }

    /**
     * conta quantas cartas cabem nessa página
     * @return o total de espaços disponíveis somando as linhas e colunas
     */
    public int getTotalSlots() { 
        return linhas * colunas;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}