package digibinder.view;

import digibinder.controller.PhotocardController;
import digibinder.exception.DatabaseException;
import digibinder.model.Photocard;
import digibinder.model.StatusPhotocard;
import digibinder.view.components.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * exibe os detalhes completos de um photocard
 * permite alterar o status diretamente por este painel
 */
public class PhotocardDetailDialog extends JDialog {

    private final Photocard pc;
    private final PhotocardController controller;
    private final Runnable onUpdate;
    private JLabel lblStatusAtual;

    public PhotocardDetailDialog(Frame parent, Photocard pc, PhotocardController controller, Runnable onUpdate) {
        super(parent, pc.getMembro(), true);
        this.pc = pc;
        this.controller = controller;
        this.onUpdate = onUpdate;

        setSize(500, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(AppTheme.BG_APP);

        construirUI();
    }

    private void construirUI() {
        JPanel painel = new JPanel(new BorderLayout(0, 0));
        painel.setBackground(AppTheme.BG_APP);
        painel.setBorder(new EmptyBorder(24, 28, 24, 28));

        // imagem ampliada
        JLabel lblImagem = new JLabel();
        lblImagem.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagem.setPreferredSize(new Dimension(444, 280));
        lblImagem.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));
        lblImagem.setBackground(AppTheme.PRIMARY_LIGHT);
        lblImagem.setOpaque(true);

        carregarImagemAmpliada(lblImagem);

        // infos
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(18, 0, 0, 0));

        JLabel lblNome = new JLabel(pc.getMembro());
        lblNome.setFont(AppTheme.FONT_TITLE);
        lblNome.setForeground(AppTheme.TEXT_PRIMARY);
        lblNome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAlbum = new JLabel(pc.getAlbum() != null ? pc.getAlbum().getNomeCompleto() : "");
        lblAlbum.setFont(AppTheme.FONT_BODY);
        lblAlbum.setForeground(AppTheme.TEXT_SECONDARY);
        lblAlbum.setAlignmentX(Component.LEFT_ALIGNMENT);

        // status atual
        JPanel linhaStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaStatus.setOpaque(false);
        linhaStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblStatusLabel = new JLabel("Status:");
        lblStatusLabel.setFont(AppTheme.FONT_LABEL);
        lblStatusLabel.setForeground(AppTheme.TEXT_SECONDARY);

        lblStatusAtual = new JLabel(pc.getStatus().getNome());
        lblStatusAtual.setFont(AppTheme.FONT_BODY);
        lblStatusAtual.setForeground(AppTheme.corDoStatus(pc.getStatus()));

        linhaStatus.add(lblStatusLabel);
        linhaStatus.add(lblStatusAtual);

        // botões de status
        JPanel botoesStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        botoesStatus.setOpaque(false);
        botoesStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (StatusPhotocard status : StatusPhotocard.values()) {
            StyledButton btn = new StyledButton(status.getNome(),
                pc.getStatus() == status ? StyledButton.Style.PRIMARY : StyledButton.Style.SECONDARY);

            btn.addActionListener(e -> alterarStatus(status, btn));
            botoesStatus.add(btn);
        }

        info.add(lblNome);
        info.add(Box.createVerticalStrut(4));
        info.add(lblAlbum);
        info.add(Box.createVerticalStrut(14));
        info.add(linhaStatus);
        info.add(Box.createVerticalStrut(10));
        info.add(botoesStatus);

        // button fechar
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.setOpaque(false);
        StyledButton btnFechar = new StyledButton("Fechar", StyledButton.Style.SECONDARY);
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);

        painel.add(lblImagem, BorderLayout.NORTH);
        painel.add(info, BorderLayout.CENTER);
        painel.add(rodape, BorderLayout.SOUTH);

        setContentPane(painel);
    }

    private void carregarImagemAmpliada(JLabel alvo) {
        String caminho = pc.getCaminhoImagem();

        if (caminho == null || caminho.isBlank()) {
            alvo.setText("Sem imagem");
            alvo.setFont(AppTheme.FONT_BODY);
            alvo.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }

        File arquivo = new File(caminho);
        if (!arquivo.exists()) {
            alvo.setText("Imagem não encontrada");
            alvo.setFont(AppTheme.FONT_BODY);
            alvo.setForeground(AppTheme.TEXT_SECONDARY);
            return;
        }

        try {
            BufferedImage img = ImageIO.read(arquivo);
            if (img != null) {
                Image scaled = img.getScaledInstance(444, 280, Image.SCALE_SMOOTH);
                alvo.setIcon(new ImageIcon(scaled));
                alvo.setText(null);
            }
        } catch (IOException e) {
            alvo.setText("Erro ao carregar imagem");
        }
    }

    private void alterarStatus(StatusPhotocard novoStatus, StyledButton btnClicado) {
        try {
            controller.atualizarStatus(pc.getId(), novoStatus);
            pc.setStatus(novoStatus);
            lblStatusAtual.setText(novoStatus.getNome());
            lblStatusAtual.setForeground(AppTheme.corDoStatus(novoStatus));
            if (onUpdate != null) onUpdate.run();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar status: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}