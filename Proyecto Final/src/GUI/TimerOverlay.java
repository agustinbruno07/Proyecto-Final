package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TimerOverlay extends JComponent {
    private JLabel timerLabel;
    private Timer updateTimer;
    private JFrame parentFrame;

    public TimerOverlay(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setOpaque(false);

        timerLabel = new JLabel("00:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(18)));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setBackground(new Color(0,0,0,120));
        timerLabel.setOpaque(true);
        add(timerLabel);

        updateBounds();

        parentFrame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                updateBounds();
            }
        });

        EstadoJuego.startTimer();
        updateTimer = new Timer(1000, e -> timerLabel.setText(EstadoJuego.getFormattedElapsed()));
        updateTimer.setInitialDelay(0);
        updateTimer.start();
    }

    private void updateBounds() {
        if (parentFrame == null) return;
        int frameW = parentFrame.getWidth();
        int frameH = parentFrame.getHeight();
        if (frameW <= 0 || frameH <= 0) return;

        int timerW = escalaManager.escalaAncho(100);
        int timerH = escalaManager.escalaAlto(40);
        int timerX = frameW - timerW - escalaManager.escalaX(20);
        int timerY = escalaManager.escalaY(20);

        setBounds(0, 0, frameW, frameH);
        timerLabel.setBounds(timerX, timerY, timerW, timerH);
        revalidate();
        repaint();
    }

    public void attachToFrame() {
        JLayeredPane lp = parentFrame.getLayeredPane();
        lp.add(this, JLayeredPane.PALETTE_LAYER);
        setVisible(true);
    }

    public void detachFromFrame() {
        if (updateTimer != null && updateTimer.isRunning()) updateTimer.stop();
        if (parentFrame != null) {
            parentFrame.getLayeredPane().remove(this);
            parentFrame.getLayeredPane().revalidate();
            parentFrame.getLayeredPane().repaint();
        }
    }
}
