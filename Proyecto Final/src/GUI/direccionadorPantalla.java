package GUI;

import javax.swing.*;
import java.awt.*;

public class direccionadorPantalla {

    public static void fadeToPanel(JFrame frame, JPanel panelDestino) {
        // Contenedor para alpha (así lo podemos mutar dentro de lambdas/inner class)
        final float[] a = {0f}; // 0 = transparente, 1 = negro total

        // Overlay negro encima de todo
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.SrcOver.derive(a[0]));
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);
        frame.setGlassPane(overlay);
        overlay.setVisible(true);

        // Fade in (aparece negro)
        Timer fadeIn = new Timer(16, null);
        fadeIn.addActionListener(e -> {
            a[0] += 0.03f;
            if (a[0] > 1f) a[0] = 1f;
            overlay.repaint();

            if (a[0] >= 1f) {
                fadeIn.stop();

                // Cambiar contenido cuando ya esté cubierto
                frame.setContentPane(panelDestino);
                frame.revalidate();
                frame.repaint();

                // Fade out (se va el negro)
                Timer fadeOut = new Timer(16, null);
                fadeOut.addActionListener(ev -> {
                    a[0] -= 0.03f;
                    if (a[0] < 0f) a[0] = 0f;
                    overlay.repaint();

                    if (a[0] <= 0f) {
                        fadeOut.stop();
                        overlay.setVisible(false);
                    }
                });
                fadeOut.start();
            }
        });
        fadeIn.start();
    }
}