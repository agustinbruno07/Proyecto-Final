package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dialogo1 extends JPanel {
    private JFrame parentFrame;
    private Image imagenDialogo;
    private float alpha = 0.0f;
    private Timer fadeTimer;
    private boolean fadeCompletado = false;

    private final int MARGEN_HORIZONTAL = 100; // Borde izquierdo y derecho
    private final int MARGEN_VERTICAL = 80;   // Borde superior e inferior

    public dialogo1(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);

        // Cargar la primera imagen del diálogo
        imagenDialogo = new ImageIcon("src/resources/images/dialogo1.png").getImage();
        //Reproducir ruidos de pajaros
        try {
            Musica.reproducir("src/resources/sonidos/birdSound.wav");
        } catch (Exception e) {
            System.err.println("Error al reproducir música de fondo: " + e.getMessage());
        }
        // Fade in
        fadeTimer = new Timer(30, e -> {
            alpha += 0.02f;
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
                fadeCompletado = true;
            }
            repaint();
        });
        fadeTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (fadeCompletado) {
                    avanzarAlSiguienteDialogo();
                }
            }
        });
         
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (fadeCompletado && 
                    (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER || 
                     e.getKeyCode() == KeyEvent.VK_E)) {
                    avanzarAlSiguienteDialogo();
                }
            }
        });
        requestFocusInWindow();
    }

    private void avanzarAlSiguienteDialogo() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(new dialogo2(parentFrame));
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (imagenDialogo != null) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = imagenDialogo.getWidth(this);
            int imgHeight = imagenDialogo.getHeight(this);

            // Calcular área disponible (panel menos bordes)
            int areaDisponibleAncho = panelWidth - (2 * MARGEN_HORIZONTAL);
            int areaDisponibleAlto = panelHeight - (2 * MARGEN_VERTICAL);

            // Calcular escala para que la imagen quepa en el área disponible manteniendo relación de aspecto
            double escalaX = (double) areaDisponibleAncho / imgWidth;
            double escalaY = (double) areaDisponibleAlto / imgHeight;
            double escala = Math.min(escalaX, escalaY); // Usar la escala más pequeña para mantener proporción
            
            int nuevoAncho = (int) (imgWidth * escala);
            int nuevoAlto = (int) (imgHeight * escala);
            
            // Centrar la imagen en el panel con los bordes
            int x = (panelWidth - nuevoAncho) / 2;
            int y = (panelHeight - nuevoAlto) / 2;

            g2d.drawImage(imagenDialogo, x, y, nuevoAncho, nuevoAlto, this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Indicador para continuar
            if (fadeCompletado) {
                g2d.setColor(new Color(255, 255, 255, (int)(Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128)));
                g2d.setFont(new Font("Arial", Font.BOLD, 18));
                String texto = "Haz CLIC para continuar";
                int textoWidth = g2d.getFontMetrics().stringWidth(texto);
                g2d.drawString(texto, (panelWidth - textoWidth) / 2, panelHeight - 50);
            }
        }
    }

    public void detenerAnimaciones() {
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
    }
}