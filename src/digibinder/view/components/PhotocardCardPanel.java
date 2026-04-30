package digibinder.view.components;

import digibinder.exception.ImageNotFoundException;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * painel visual de um photocard individual no grid de coleção
 * exibe a imagem, o nome do membro, o álbum e um badge de status. 
 * photocards DESEJADOS recebem filtro cinza
 */
public class PhotocardCardPanel extends RoundedPanel {
    private static final int CARD_W = 150;
    private static final int CARD_H = 220;
    private static final int IMG_H = 155;

    // ícone padrão exibido quando a imagem não é encontrada
    private static BufferedImage PLACEHOLDER;

    static {
        PLACEHOLDER = new BufferedImage(CARD_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = PLACEHOLDER.createGraphics();
        g.setColor(AppTheme.PRIMARY_LIGHT);
        g.fillRect(0, 0, CARD_W, IMG_H);
        g.setColor(AppTheme.TEXT_SECONDARY);
        g.setFont(AppTheme.FONT_SMALL);
        FontMetrics fm = g.getFontMetrics();
        String msg = "sem imagem";
        g.drawString(msg, (CARD_W - fm.stringWidth(msg)) / 2, IMG_H / 2);
        g.dispose();
    }

    private final Photocard photocard;
    private BufferedImage imagem;

    public PhotocardCardPanel(Photocard photocard) {
        super(AppTheme.RADIUS_CARD);
        this.photocard = photocard;

        carregarImagem();
        construirUI();
        setPreferredSize(new Dimension(CARD_W, CARD_H));
        setMaximumSize(new Dimension(CARD_W, CARD_H));
    }

    /** carrega a imagem do disco. se falhar, usa o placeholder */
    private void carregarImagem() {
        String caminho = photocard.getCaminhoImagem();

        if (caminho == null || caminho.isBlank()) {
            imagem = PLACEHOLDER;
            return;
        }

        File arquivo = new File(caminho);
        if (!arquivo.exists() || !arquivo.isFile()) {
            // arquivo movido ou deletado
            imagem = PLACEHOLDER;
            System.err.println("Imagem não encontrada: " + caminho);
            return;
        }

        try {
            BufferedImage original = ImageIO.read(arquivo);
            if (original == null) {
                imagem = PLACEHOLDER;
                return;
            }
            imagem = redimensionar(original, CARD_W, IMG_H);

            // aplica filtro cinza nos cards DESEJADOS
            if (photocard.getStatus() == StatusPhotocard.DESEJADO) {
                imagem = aplicarFiltroDesejado(imagem);
            }
        } catch (IOException e) {
            imagem = PLACEHOLDER;
        }
    }

    /** redimensiona mantendo aspecto e recortando para preencher a área */
    private BufferedImage redimensionar(BufferedImage src, int w, int h) {
        double escalaX = (double) w / src.getWidth();
        double escalaY = (double) h / src.getHeight();
        double escala = Math.max(escalaX, escalaY);

        int nw = (int) (src.getWidth()  * escala);
        int nh = (int) (src.getHeight() * escala);

        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        
        int x = (nw - w) / 2;
        int y = (nh - h) / 2;
        return scaled.getSubimage(Math.max(0, x), Math.max(0, y),
                Math.min(w, scaled.getWidth()), Math.min(h, scaled.getHeight()));
    }

    /**
     * converte imagem para escala de cinza com transparência (cards desejados)
     */
    private BufferedImage aplicarFiltroDesejado(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage resultado = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb  = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >>  8) & 0xFF;
                int b = (rgb) & 0xFF;
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                int argb = (140 << 24) | (gray << 16) | (gray << 8) | gray;
                resultado.setRGB(x, y, argb);
            }
        }
        return resultado;
    }

    private void construirUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 8, 10, 8));

        // imagem
        JLabel lblImagem = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (imagem == null) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // clip arredondado para a imagem
                Shape clip = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setClip(clip);
                g2.drawImage(imagem, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        };
        lblImagem.setPreferredSize(new Dimension(CARD_W - 16, IMG_H));
        lblImagem.setOpaque(false);

        // info inferior
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JLabel lblMembro = new JLabel(photocard.getMembro());
        lblMembro.setFont(AppTheme.FONT_SUBTITLE);
        lblMembro.setForeground(AppTheme.TEXT_PRIMARY);
        lblMembro.setAlignmentX(Component.LEFT_ALIGNMENT);

        String albumNome = photocard.getAlbum() != null
            ? photocard.getAlbum().getNomeCompleto() : "";
        JLabel lblAlbum = new JLabel(albumNome);
        lblAlbum.setFont(AppTheme.FONT_SMALL);
        lblAlbum.setForeground(AppTheme.TEXT_SECONDARY);
        lblAlbum.setAlignmentX(Component.LEFT_ALIGNMENT);

        StatusBadge badge = new StatusBadge(photocard.getStatus());
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.putClientProperty("status", photocard.getStatus());

        info.add(lblMembro);
        info.add(Box.createVerticalStrut(2));
        info.add(lblAlbum);
        info.add(Box.createVerticalStrut(4));
        info.add(badge);

        add(lblImagem, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
    }

    public Photocard getPhotocard() {
        return photocard;
    }
}