package digibinder.view;

import digibinder.controller.PhotocardController;
import digibinder.model.Album;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;
import digibinder.view.components.AppTheme;
import digibinder.view.components.StatusBadge;
import digibinder.view.components.StyledButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * dialog para adicionar um novo photocard a um álbum
 */
public class PhotocardFormDialog extends JDialog {

    private JTextField campoMembro;
    private JTextField campoImagem;
    private boolean confirmado = false;

    public PhotocardFormDialog(Frame parent, Album album) {
        super(parent, "Novo Photocard — " + album.getNomeCompleto(), true);

        setSize(460, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG_APP);

        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(AppTheme.BG_APP);
        painel.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel titulo = new JLabel("Adicionar Photocard");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblMembro = criarLabel("Nome do Membro *");
        campoMembro = criarCampo();

        JLabel lblImagem = criarLabel("Imagem (caminho do arquivo)");

        // campo de imagem + botão escolher
        JPanel linhaImagem = new JPanel(new BorderLayout(8, 0));
        linhaImagem.setOpaque(false);
        linhaImagem.setAlignmentX(Component.LEFT_ALIGNMENT);
        linhaImagem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        campoImagem = new JTextField();
        campoImagem.setFont(AppTheme.FONT_BODY);
        campoImagem.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        StyledButton btnEscolher = new StyledButton("...", StyledButton.Style.SECONDARY);
        btnEscolher.setToolTipText("Escolher arquivo");
        btnEscolher.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Imagens (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"));
            if (chooser.showOpenDialog(PhotocardFormDialog.this) == JFileChooser.APPROVE_OPTION) {
                campoImagem.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        linhaImagem.add(campoImagem, BorderLayout.CENTER);
        linhaImagem.add(btnEscolher, BorderLayout.EAST);

        JLabel nota = new JLabel("* O status inicial será \"Desejado\". Mude após cadastrar.");
        nota.setFont(AppTheme.FONT_SMALL);
        nota.setForeground(AppTheme.TEXT_SECONDARY);
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botoes.setOpaque(false);
        botoes.setAlignmentX(Component.LEFT_ALIGNMENT);

        StyledButton btnCancelar = new StyledButton("Cancelar", StyledButton.Style.SECONDARY);
        StyledButton btnSalvar   = new StyledButton("Adicionar");

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> { confirmado = true; dispose(); });

        botoes.add(btnCancelar);
        botoes.add(btnSalvar);

        painel.add(titulo);
        painel.add(Box.createVerticalStrut(20));
        painel.add(lblMembro);
        painel.add(Box.createVerticalStrut(4));
        painel.add(campoMembro);
        painel.add(Box.createVerticalStrut(14));
        painel.add(lblImagem);
        painel.add(Box.createVerticalStrut(4));
        painel.add(linhaImagem);
        painel.add(Box.createVerticalStrut(10));
        painel.add(nota);
        painel.add(Box.createVerticalStrut(20));
        painel.add(botoes);

        setContentPane(painel);
        getRootPane().setDefaultButton(btnSalvar);
    }

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppTheme.FONT_LABEL);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField criarCampo() {
        JTextField campo = new JTextField();
        campo.setFont(AppTheme.FONT_BODY);
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        campo.setAlignmentX(Component.LEFT_ALIGNMENT);
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return campo;
    }

    public boolean foiConfirmado()  { 
    	return confirmado; 
    }
    public String getMembro()  { 
    	return campoMembro.getText().trim(); 
    }
    public String getCaminhoImagem() {
        String t = campoImagem.getText().trim();
        return t.isEmpty() ? null : t;
    }
}
