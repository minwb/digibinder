package digibinder.view;

import digibinder.controller.PhotocardController;
import digibinder.exception.DatabaseException;
import digibinder.exception.ImageNotFoundException;
import digibinder.exception.ValidationException;
import digibinder.model.*;
import digibinder.view.components.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * exibe os photocards de um álbum em grade
 * permite adicionar, remover, alterar status e visualizar detalhes dos cards
 */
public class PhotocardGridView extends JPanel {

    private final PhotocardController photocardController;
    private final MainFrame mainFrame;
    private final Album album;
    private final Grupo grupo;

    private JPanel gridCards;
    private JLabel labelContagem;

    // filtros ativos
    private StatusPhotocard filtroStatus = null;
    private JToggleButton btnFiltroTodos;
    private JToggleButton btnFiltroAdquirido;
    private JToggleButton btnFiltroDesejado;
    private JToggleButton btnFiltroTroca;

    public PhotocardGridView(MainFrame mainFrame, PhotocardController photocardController,
                             Album album, Grupo grupo) {
        this.mainFrame = mainFrame;
        this.photocardController  = photocardController;
        this.album = album;
        this.grupo = grupo;

        setBackground(AppTheme.BG_APP);
        setLayout(new BorderLayout(0, 0));

        construirCabecalho();
        construirFiltros();
        construirGrid();
        carregarPhotocards();
    }

    // header

    private void construirCabecalho() {
        JPanel header = new JPanel(new BorderLayout(0, 6));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        // breadcrumb
        JPanel breadcrumb = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        breadcrumb.setOpaque(false);

        JLabel linkHome = criarLink("Grupos",   e -> mainFrame.voltarDashboard());
        JLabel sep1  = criarSeparador();
        JLabel linkGrp  = criarLink(grupo.getNome(), e -> mainFrame.abrirAlbums(grupo));
        JLabel sep2 = criarSeparador();
        JLabel atual = new JLabel(album.getNomeCompleto());
        atual.setFont(AppTheme.FONT_SUBTITLE);
        atual.setForeground(AppTheme.TEXT_PRIMARY);

        breadcrumb.add(linkHome);
        breadcrumb.add(sep1);
        breadcrumb.add(linkGrp);
        breadcrumb.add(sep2);
        breadcrumb.add(atual);

        // título + contagem + botão
        JPanel linha = new JPanel(new BorderLayout());
        linha.setOpaque(false);

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        esquerda.setOpaque(false);

        JLabel titulo = new JLabel(album.getNomeCompleto());
        titulo.setFont(AppTheme.FONT_TITLE);
        titulo.setForeground(AppTheme.TEXT_PRIMARY);

        labelContagem = new JLabel();
        labelContagem.setFont(AppTheme.FONT_BODY);
        labelContagem.setForeground(AppTheme.TEXT_SECONDARY);

        esquerda.add(titulo);
        esquerda.add(labelContagem);

        StyledButton btnAdicionar = new StyledButton("+ Photocard");
        btnAdicionar.addActionListener(e -> abrirDialogNovoPhotocard());

        linha.add(esquerda, BorderLayout.WEST);
        linha.add(btnAdicionar, BorderLayout.EAST);

        header.add(breadcrumb, BorderLayout.NORTH);
        header.add(linha, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
    }

    // filtros

    private void construirFiltros() {
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtros.setOpaque(false);
        filtros.setBorder(new EmptyBorder(0, 0, 14, 0));

        ButtonGroup grupo = new ButtonGroup();

        btnFiltroTodos = criarBotaoFiltro("Todos", null);
        btnFiltroAdquirido = criarBotaoFiltro("Adquirido",  StatusPhotocard.ADQUIRIDO);
        btnFiltroDesejado = criarBotaoFiltro("Desejados",  StatusPhotocard.DESEJADO);
        btnFiltroTroca = criarBotaoFiltro("Para Troca", StatusPhotocard.PARA_TROCA);

        btnFiltroTodos.setSelected(true);

        grupo.add(btnFiltroTodos);
        grupo.add(btnFiltroAdquirido);
        grupo.add(btnFiltroDesejado);
        grupo.add(btnFiltroTroca);

        filtros.add(btnFiltroTodos);
        filtros.add(btnFiltroAdquirido);
        filtros.add(btnFiltroDesejado);
        filtros.add(btnFiltroTroca);

        add(filtros, BorderLayout.CENTER);
    }

    private JToggleButton criarBotaoFiltro(String texto, StatusPhotocard status) {
        JToggleButton btn = new JToggleButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? AppTheme.PRIMARY : AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        btn.addActionListener(e -> {
            filtroStatus = status;
            carregarPhotocards();
        });

        return btn;
    }

    // grid

    private void construirGrid() {
        gridCards = new JPanel();
        gridCards.setOpaque(false);
        gridCards.setLayout(new DashboardView.WrapLayout(FlowLayout.LEFT, 14, 14));

        JScrollPane scroll = new JScrollPane(gridCards);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // painel inferior para o scroll
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 0, 0));
        wrapper.add(scroll);
        add(wrapper, BorderLayout.SOUTH);
    }

    /** carrega os photocards do álbum e aplica o filtro de status ativo */
    public void carregarPhotocards() {
        try {
            List<Photocard> lista = photocardController.listarPorAlbum(album.getId());

            if (filtroStatus != null) {
                lista = lista.stream()
                    .filter(pc -> pc.getStatus() == filtroStatus)
                    .toList();
            }

            renderizarPhotocards(lista);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(mainFrame, "Erro ao carregar photocards: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderizarPhotocards(List<Photocard> lista) {
        gridCards.removeAll();

        int total = lista.size();
        long possuidos = lista.stream().filter(p -> p.getStatus() == StatusPhotocard.ADQUIRIDO).count();
        labelContagem.setText(possuidos + "/" + total + " adquiridos");

        if (lista.isEmpty()) {
            JLabel vazio = new JLabel("Nenhum photocard encontrado");
            vazio.setFont(AppTheme.FONT_BODY);
            vazio.setForeground(AppTheme.TEXT_SECONDARY);
            gridCards.add(vazio);
        } else {
            for (Photocard pc : lista) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setOpaque(false);

                PhotocardCardPanel cardPanel = new PhotocardCardPanel(pc);

                // menu de contexto ao clicar com botão direito
                JPopupMenu menu = criarMenuContexto(pc);
                cardPanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            menu.show(cardPanel, e.getX(), e.getY());
                        } else if (e.getClickCount() == 2) {
                            abrirDetalhes(pc);
                        }
                    }
                });

                wrapper.add(cardPanel);
                gridCards.add(wrapper);
            }
        }

        gridCards.revalidate();
        gridCards.repaint();
    }

    private JPopupMenu criarMenuContexto(Photocard pc) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(AppTheme.BG_CARD);
        menu.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1));

        JMenuItem detalhes = new JMenuItem("Ver detalhes");
        JMenuItem adquirido = new JMenuItem("Marcar como Adquirido");
        JMenuItem desejado = new JMenuItem("Marcar como Desejado");
        JMenuItem troca = new JMenuItem("Marcar como Para Troca");
        JMenuItem deletar = new JMenuItem("Excluir");

        for (JMenuItem item : new JMenuItem[]{detalhes, adquirido, desejado, troca, deletar}) {
            item.setFont(AppTheme.FONT_BODY);
            item.setBackground(AppTheme.BG_CARD);
            menu.add(item);
        }
        menu.addSeparator();
        menu.add(deletar);

        detalhes.addActionListener(e -> abrirDetalhes(pc));
        adquirido.addActionListener(e -> alterarStatus(pc, StatusPhotocard.ADQUIRIDO));
        desejado.addActionListener(e -> alterarStatus(pc, StatusPhotocard.DESEJADO));
        troca.addActionListener(e -> alterarStatus(pc, StatusPhotocard.PARA_TROCA));
        deletar.addActionListener(e -> confirmarDelecao(pc));

        return menu;
    }

    // ações

    private void alterarStatus(Photocard pc, StatusPhotocard novoStatus) {
        try {
            photocardController.atualizarStatus(pc.getId(), novoStatus);
            carregarPhotocards();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmarDelecao(Photocard pc) {
        int resp = JOptionPane.showConfirmDialog(
            mainFrame, "Excluir o photocard de " + pc.getMembro() + "?",
            "Confirmar exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE
        );
        if (resp == JOptionPane.YES_OPTION) {
            try {
                photocardController.deletarPhotocard(pc.getId());
                carregarPhotocards();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirDetalhes(Photocard pc) {
        PhotocardDetailDialog dialog = new PhotocardDetailDialog(mainFrame, pc, photocardController,
            () -> carregarPhotocards());
        dialog.setVisible(true);
    }

    private void abrirDialogNovoPhotocard() {
        PhotocardFormDialog dialog = new PhotocardFormDialog(mainFrame, album);
        dialog.setVisible(true);

        if (dialog.foiConfirmado()) {
            try {
                photocardController.criarPhotocard(
                    dialog.getMembro(), dialog.getCaminhoImagem(), album.getId()
                );
                carregarPhotocards();
            } catch (ImageNotFoundException e) {
                JOptionPane.showMessageDialog(mainFrame,
                    "Arquivo de imagem não encontrado:\n" + e.getCaminhoTentado(),
                    "Imagem não encontrada", JOptionPane.WARNING_MESSAGE);
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // helpers

    private JLabel criarLink(String texto, ActionListener acao) {
        JLabel label = new JLabel(texto);
        label.setFont(AppTheme.FONT_BODY);
        label.setForeground(AppTheme.PRIMARY_DARK);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { acao.actionPerformed(null); }
        });
        return label;
    }

    private JLabel criarSeparador() {
        JLabel sep = new JLabel(" › ");
        sep.setFont(AppTheme.FONT_BODY);
        sep.setForeground(AppTheme.TEXT_SECONDARY);
        return sep;
    }
}
