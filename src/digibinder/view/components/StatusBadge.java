package digibinder.view.components;

import digibinder.model.StatusPhotocard;

import javax.swing.*;
import java.awt.*;

/**
 * badge visual que exibe o status de um photocard com a cor correspondente
 */
public class StatusBadge extends JLabel {

    public StatusBadge(StatusPhotocard status) {
        super(status.getNome());
        setOpaque(false);
        setFont(AppTheme.FONT_SMALL);
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    }

    public void setStatus(StatusPhotocard status) {
        setText(status.getNome());
        putClientProperty("status", status);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Object prop = getClientProperty("status");
        StatusPhotocard status = StatusPhotocard.DESEJADO;
        if (prop instanceof StatusPhotocard s) {
            status = s;
        } else {
            for (StatusPhotocard s : StatusPhotocard.values()) {
                if (s.getNome().equals(getText())) {
                    status = s;
                    break;
                }
            }
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.corDoStatus(status));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 4, d.height + 2);
    }
}
