package digibinder.model;

import java.util.Objects;

/**
 * representa o item colecionável 
 * guarda as informações do membro, a imagem, se o usuário já tem o card ou quer trocar, e em qual slot do binder ele está guardado
 */
public class Photocard {
    private Integer id; 
    private String membro;
    private String caminhoImagem;
    private StatusPhotocard status;
    
    private Album album; 
    
    private Integer posicaoX; 
    private Integer posicaoY; 
    private Integer paginaId; 

    /**
     * cria um photocard definindo ele como desejado por padrão
     */
    public Photocard() {
        this.status = StatusPhotocard.DESEJADO; 
    }

    /**
     * cria um card informando quem tá na foto, onde a imagem tá salva e de qual álbum ele é
     * @param membro nome do ídolo no card
     * @param caminhoImagem endereço do arquivo da foto no computador
     * @param album o álbum de origem desse colecionável
     */
    public Photocard(String membro, String caminhoImagem, Album album) {
        this(); 
        this.membro = membro;
        this.caminhoImagem = caminhoImagem;
        this.album = album;
    }

    /**
     * construtor completo que traz as informações direto do banco de dados
     * @param id código do card
     * @param membro nome do integrante
     * @param caminhoImagem caminho da foto salva
     * @param status indica se você tem, quer ou tá trocando
     * @param album o álbum dono do card
     */
    public Photocard(Integer id, String membro, String caminhoImagem, 
    				StatusPhotocard status, Album album) {
        this(membro, caminhoImagem, album); 
        this.id = id;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getMembro() {
        return membro;
    }
    public void setMembro(String membro) {
        this.membro = membro;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }
    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }

    public StatusPhotocard getStatus() {
        return status;
    }
    public void setStatus(StatusPhotocard status) {
        this.status = status;
    }

    public Album getAlbum() {
        return album;
    }
    public void setAlbum(Album album) {
        this.album = album;
    }

    public Integer getPosicaoX() {
        return posicaoX;
    }
    public void setPosicaoX(Integer posicaoX) {
        this.posicaoX = posicaoX;
    }

    public Integer getPosicaoY() {
        return posicaoY;
    }
    public void setPosicaoY(Integer posicaoY) {
        this.posicaoY = posicaoY;
    }

    public Integer getPaginaId() {
        return paginaId;
    }
    public void setPaginaId(Integer paginaId) {
        this.paginaId = paginaId;
    }

    /**
     * junta o nome do ídolo com o nome do álbum 
     * @return a descrição completa do photocard
     */
    public String getDescricao() {
        if (album != null) { 
            return membro + " - " + album.getNomeCompleto(); 
        } else {
            return membro;
        }
    }

    /**
     * dois photocards são o mesmo se tiverem o exato mesmo código do banco
     * @param obj o outro objeto que tá sendo verificado
     * @return verdadeiro se for a mesma carta
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
        	return false;
        }
        Photocard that = (Photocard) obj;
        return Objects.equals(id, that.id);  
    }

    /**
     * cria um valor único do card baseada no id pra otimização
     * @return o identificador gerado
     */
    @Override
    public int hashCode() { 
        return Objects.hash(id); 
    }

    /**
     * define exibição do card
     * @return o texto formatado do card
     */
    @Override
    public String toString() {
        return getDescricao(); 
    }
}