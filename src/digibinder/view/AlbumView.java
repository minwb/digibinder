package digibinder.view;

import digibinder.controller.AlbumController;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Album;
import digibinder.model.Grupo;
import digibinder.view.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/*  
 * exibe os álbuns de um grupo selecionado
 * permite navegar para os photocards de cada álbum e gerenciar os álbuns
*/
public class AlbumView extends JPanel {

    private final AlbumController albumController;
    private final MainFrame mainFrame;
    private final Grupo grupo;

    private JPanel gridAlbuns;

    public AlbumView(MainFrame mainFrame, AlbumController albumController, Grupo grupo) {
        this.mainFrame = mainFrame;
        this.albumController = albumController;
        this.grupo = grupo;

        setBackground(AppTheme.BG_APP);
        setLayout(new BorderLayout());

        construirCabecalho();
        construirGrid();
        carregarAlbuns();
    }

    // header
    private void construirCabecalho() {
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        // breadcrumb
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        breadcrumb.setOpaque(false);

        JLabel linkHome = new JLabel("Grupos");
        linkHome.setFont(AppTheme.FONT_BODY);
        linkHome.setForeground(AppTheme.PRIMARY_DARK);
        linkHome.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { mainFrame.voltarDashboard(); }
        });

        JLabel sep = new JLabel(" › ");
        sep.setFont(AppTheme.FONT_BODY);
        sep.setForeground(AppTheme.TEXT_SECONDARY);

        JLabel atual = new JLabel(grupo.getNome());
        atual.setFont(AppTheme.FONT_SUBTITLE);
        atual.setForeground(AppTheme.TEXT_PRIMARY);

        breadcrumb.add(linkHome);
        breadcrumb.add(sep);
        breadcrumb.add(atual);

        // linha com título e botão
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);

        JLabel titulo = new JLabel("Álbuns de " + grupo.getNome());
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);

        StyledButton btnAdicionar = new StyledButton("+ Novo Álbum");
        btnAdicionar.addActionListener(e -> abrirDialogNovoAlbum());

        linha.add(titulo, BorderLayout.WEST);
        linha.add(btnAdicionar, BorderLayout.EAST);

        header.add(breadcrumb, BorderLayout.NORTH);
        header.add(linha, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
    }

    // grid
    private void construirGrid() {
        gridAlbuns = new JPanel();
        gridAlbuns.setOpaque(false);
        gridAlbuns.setLayout(new DashboardView.WrapLayout(FlowLayout.LEFT, 16, 16));

        JScrollPane scroll = new JScrollPane(gridAlbuns);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    // carrega os álbuns do grupo no grid
    public void carregarAlbuns() {
        try {
            List<Album> albuns = albumController.listarPorGrupo(grupo.getId());
            renderizarAlbuns(albuns);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(mainFrame, "Erro ao carregar álbuns: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderizarAlbuns(List<Album> albuns) {
        gridAlbuns.removeAll();

        if (albuns.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum álbum cadastrado. Clique em 'Novo Álbum' para começar.");
            vazio.setFont(AppTheme.FONT_BODY);
            vazio.setForeground(AppTheme.TEXT_SECONDARY);
            gridAlbuns.add(vazio);
        } else {
            for (Album album : albuns) {
                gridAlbuns.add(criarCardAlbum(album));
            }
        }

        gridAlbuns.revalidate();
        gridAlbuns.repaint();
    }

    private JPanel criarCardAlbum(Album album) {
        RoundedPanel card = new RoundedPanel(AppTheme.RADIUS_CARD);
        card.setPreferredSize(new Dimension(175, 215));
        card.setLayout(new BorderLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // área de capa
        JPanel capaArea = new JPanel(new GridBagLayout());
        capaArea.setBackground(AppTheme.PRIMARY_LIGHT);
        capaArea.setPreferredSize(new Dimension(175, 130));

        // definir a capa do álbum como a primeira letra
        String inicial = album.getNome().substring(0, 1).toUpperCase();
        JLabel icone = new JLabel(inicial);
        icone.setFont(new Font("Segoe UI", Font.BOLD, 42));
        icone.setForeground(AppTheme.PRIMARY_DARK);
        capaArea.add(icone);

        // rodapé
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 14, 10, 10));

        JLabel lblNome = new JLabel(album.getNome());
        lblNome.setFont(AppTheme.FONT_SUBTITLE);
        lblNome.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel lblVersao = new JLabel(album.getVersao() != null ? album.getVersao() : " ");
        lblVersao.setFont(AppTheme.FONT_SMALL);
        lblVersao.setForeground(AppTheme.TEXT_SECONDARY);

        String ano = album.getAnoLancamento() != null ? album.getAnoLancamento().toString() : "";
        JLabel lblAno = new JLabel(ano);
        lblAno.setFont(AppTheme.FONT_SMALL);
        lblAno.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);
        textos.add(lblNome);
        textos.add(lblVersao);

        // ações
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        acoes.setOpaque(false);

        JLabel btnEditar = criarIconeAcao("Editar");
        JLabel btnDeletar = criarIconeAcao("Excluir");

        btnEditar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirDialogEditarAlbum(album);
            }
        });
        btnDeletar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                confirmarDelecaoAlbum(album);
            }
        });

        acoes.add(btnEditar);
        acoes.add(btnDeletar);

        footer.add(textos, BorderLayout.CENTER);
        footer.add(acoes, BorderLayout.EAST);

        card.add(capaArea, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.abrirPhotocards(album);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                capaArea.setBackground(AppTheme.PRIMARY.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                capaArea.setBackground(AppTheme.PRIMARY_LIGHT);
            }
        });

        return card;
    }

    private JLabel criarIconeAcao(String simbolo) {
        JLabel label = new JLabel(simbolo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { 
            	label.setForeground(AppTheme.TEXT_PRIMARY); 
            }
            @Override
            public void mouseExited(MouseEvent e) { 
            	label.setForeground(AppTheme.TEXT_SECONDARY); 
            }
        });
        return label;
    }

    // dialogs
    private void abrirDialogNovoAlbum() {
        AlbumFormDialog dialog = new AlbumFormDialog(mainFrame, null);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                albumController.criarAlbum(
                    dialog.getNome(), dialog.getVersao(),
                    dialog.getAnoLancamento(), grupo.getId()
                );
                carregarAlbuns();
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirDialogEditarAlbum(Album album) {
        AlbumFormDialog dialog = new AlbumFormDialog(mainFrame, album);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                albumController.atualizarAlbum(
                    album.getId(), dialog.getNome(),
                    dialog.getVersao(), dialog.getAnoLancamento()
                );
                carregarAlbuns();
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void confirmarDelecaoAlbum(Album album) {
        int resp = JOptionPane.showConfirmDialog(
            mainFrame,
            "Deletar o álbum \"" + album.getNomeCompleto() + "\" e todos os seus photocards?",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (resp == JOptionPane.YES_OPTION) {
            try {
                albumController.deletarAlbum(album.getId());
                carregarAlbuns();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
