package digibinder.view;

import digibinder.controller.GrupoController;
import digibinder.exception.DatabaseException;
import digibinder.exception.ValidationException;
import digibinder.model.Grupo;
import digibinder.view.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * tela principal, exibe os grupos cadastrados em grade
 * clicar em um grupo abre seus álbuns
 */
public class DashboardView extends JPanel {

    private final GrupoController grupoController;
    private final MainFrame mainFrame;

    private JPanel gridGrupos;
    private JTextField campoBusca;

    public DashboardView(MainFrame mainFrame, GrupoController grupoController) {
        this.mainFrame = mainFrame;
        this.grupoController = grupoController;

        setBackground(AppTheme.BG_APP);
        setLayout(new BorderLayout());

        construirCabecalho();
        construirGrid();
        carregarGrupos();
    }

    // header

    private void construirCabecalho() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        // título
        JLabel titulo = new JLabel("Grupos");
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);

        // campo de busca
        campoBusca = new JTextField();
        campoBusca.setFont(AppTheme.FONT_BODY);
        campoBusca.setPreferredSize(new Dimension(240, 36));
        campoBusca.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        campoBusca.setBackground(AppTheme.BG_CARD);
        campoBusca.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { 
            	filtrarGrupos(campoBusca.getText()); 
            }
        });

        // botão adicionar
        StyledButton btnAdicionar = new StyledButton("Novo Grupo");
        btnAdicionar.addActionListener(e -> abrirDialogNovoGrupo());

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        direita.setOpaque(false);
        direita.add(campoBusca);
        direita.add(btnAdicionar);

        header.add(titulo, BorderLayout.WEST);
        header.add(direita, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    // grid 

    private void construirGrid() {
        gridGrupos = new JPanel();
        gridGrupos.setOpaque(false);
        gridGrupos.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 16));

        JScrollPane scroll = new JScrollPane(gridGrupos);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    /** carrega todos os grupos do banco e exibe no grid */
    public void carregarGrupos() {
        try {
            List<Grupo> grupos = grupoController.listarGrupos();
            renderizarGrupos(grupos);
        } catch (DatabaseException e) {
            mostrarErro("Erro ao carregar grupos: " + e.getMessage());
        }
    }

    private void renderizarGrupos(List<Grupo> grupos) {
        gridGrupos.removeAll();

        if (grupos.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum grupo cadastrado. Clique em 'Novo Grupo' para começar.");
            vazio.setFont(AppTheme.FONT_BODY);
            vazio.setForeground(AppTheme.TEXT_SECONDARY);
            gridGrupos.add(vazio);
        } else {
            for (Grupo grupo : grupos) {
                gridGrupos.add(criarCardGrupo(grupo));
            }
        }

        gridGrupos.revalidate();
        gridGrupos.repaint();
    }

    private JPanel criarCardGrupo(Grupo grupo) {
        RoundedPanel card = new RoundedPanel(AppTheme.RADIUS_CARD);
        card.setPreferredSize(new Dimension(180, 200));
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(0, 0, 0, 0));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // área de imagem / cor de fundo
        JPanel imgArea = new JPanel();
        imgArea.setBackground(AppTheme.PRIMARY_LIGHT);
        imgArea.setPreferredSize(new Dimension(180, 130));
        imgArea.setOpaque(true);

        // placeholder com inicial do grupo
        JLabel inicial = new JLabel(grupo.getNome().substring(0, 1).toUpperCase());
        inicial.setFont(new Font("Segoe UI", Font.BOLD, 48));
        inicial.setForeground(AppTheme.PRIMARY_DARK);
        inicial.setHorizontalAlignment(SwingConstants.CENTER);
        imgArea.add(inicial);

        // rodapé do card
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 14, 12, 14));

        JLabel lblNome = new JLabel(grupo.getNome());
        lblNome.setFont(AppTheme.FONT_SUBTITLE);
        lblNome.setForeground(AppTheme.TEXT_PRIMARY);

        String ano = grupo.getDataDebut() != null ? String.valueOf(grupo.getDataDebut().getYear()) : "";
        JLabel lblAno = new JLabel(ano);
        lblAno.setFont(AppTheme.FONT_SMALL);
        lblAno.setForeground(AppTheme.TEXT_SECONDARY);

        footer.add(lblNome, BorderLayout.NORTH);
        footer.add(lblAno, BorderLayout.SOUTH);

        // botão de ações (menu de contexto)
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        acoes.setOpaque(false);

        JLabel btnEditar = criarIconeAcao("Editar");
        JLabel btnDeletar = criarIconeAcao("Excluir");
        acoes.add(btnEditar);
        acoes.add(btnDeletar);

        btnEditar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                abrirDialogEditarGrupo(grupo);
            }
        });

        btnDeletar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                confirmarDelecaoGrupo(grupo);
            }
        });

        footer.add(acoes, BorderLayout.EAST);

        card.add(imgArea, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        // abre tela de álbuns
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainFrame.abrirAlbums(grupo);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                imgArea.setBackground(AppTheme.PRIMARY.darker());
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                imgArea.setBackground(AppTheme.PRIMARY_LIGHT);
                card.repaint();
            }
        });

        return card;
    }

    private JLabel criarIconeAcao(String simbolo) {
        JLabel label = new JLabel(simbolo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { 
            		label.setForeground(AppTheme.TEXT_PRIMARY); 
            }
            @Override
            public void mouseExited(MouseEvent e)  { 
            	label.setForeground(AppTheme.TEXT_SECONDARY); 
            }
        });
        return label;
    }

    // filtro

    private void filtrarGrupos(String termo) {
        try {
            List<Grupo> grupos = grupoController.listarGrupos();
            if (!termo.isBlank()) {
                grupos = grupos.stream()
                    .filter(g -> g.getNome().toLowerCase().contains(termo.toLowerCase()))
                    .toList();
            }
            renderizarGrupos(grupos);
        } catch (DatabaseException e) {
            mostrarErro("Erro ao filtrar grupos.");
        }
    }

    // dialogs

    private void abrirDialogNovoGrupo() {
        GrupoFormDialog dialog = new GrupoFormDialog(mainFrame, null);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                grupoController.criarGrupo(dialog.getNome(), dialog.getDataDebut());
                carregarGrupos();
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void abrirDialogEditarGrupo(Grupo grupo) {
        GrupoFormDialog dialog = new GrupoFormDialog(mainFrame, grupo);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                grupoController.atualizarGrupo(grupo.getId(), dialog.getNome(), dialog.getDataDebut());
                carregarGrupos();
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void confirmarDelecaoGrupo(Grupo grupo) {
        int resp = JOptionPane.showConfirmDialog(
            mainFrame,
            "Deletar o grupo \"" + grupo.getNome() + "\" e todos os seus álbuns e photocards?",
            "Confirmar exclusão",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (resp == JOptionPane.YES_OPTION) {
            try {
                grupoController.deletarGrupo(grupo.getId());
                carregarGrupos();
            } catch (DatabaseException e) {
                mostrarErro(e.getMessage());
            }
        }
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(mainFrame, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * layout que quebra a linha automaticamente quando os cards não cabem na largura
     */
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - insets.left - insets.right - hgap * 2;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0, rowHeight = 0;

                for (Component m : target.getComponents()) {
                    if (m.isVisible()) {
                        Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                        if (rowWidth + d.width > maxWidth) {
                            dim.height += rowHeight + vgap;
                            rowWidth = d.width + hgap;
                            rowHeight = d.height;
                        } else {
                            rowWidth += d.width + hgap;
                            rowHeight = Math.max(rowHeight, d.height);
                        }
                        dim.width = Math.max(dim.width, rowWidth);
                    }
                }
                dim.height += rowHeight + vgap;
                dim.height += insets.top + insets.bottom + vgap * 2;
                return dim;
            }
        }
    }
}
