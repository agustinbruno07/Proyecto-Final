package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dialogo3 extends JPanel {
    private JFrame parentFrame;
    private Image imagenDialogo;
    private float alpha = 1.0f;

    // Configuración de bordes
    private final int MARGEN_HORIZONTAL = 100;
    private final int MARGEN_VERTICAL = 80;
    
    // Flag para determinar si hay un cuarto diálogo o se va directo al juego
    private static final boolean TIENE_DIALOGO4 = true; // Cambiar a false para ir directo al juego

    public dialogo3(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);

        // Cargar la tercera imagen del diálogo
        imagenDialogo = new ImageIcon("src/resources/images/dialogo3.png").getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                avanzar();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || 
                    e.getKeyCode() == KeyEvent.VK_ENTER || 
                    e.getKeyCode() == KeyEvent.VK_E) {
                    avanzar();
                }
            }
        });
        requestFocusInWindow();
    }

    /**
     * Avanza al siguiente diálogo o al juego según la configuración
     */
    private void avanzar() {
        if (TIENE_DIALOGO4) {
            avanzarAlSiguienteDialogo();
        } else {
            avanzarAlJuego();
        }
    }

    /**
     * Avanza al cuarto diálogo
     */
    private void avanzarAlSiguienteDialogo() {
        parentFrame.getContentPane().removeAll();
        dialogo4 nuevoDialogo = new dialogo4(parentFrame);
        parentFrame.getContentPane().add(nuevoDialogo);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(() -> {
            nuevoDialogo.requestFocusInWindow();
        });
    }

    /**
     * Inicia el juego directamente (transición a la calle)
     */
    private void avanzarAlJuego() {
        // Transicionar al mapa principal
        calle panelCalle = new calle();
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(panelCalle);
        parentFrame.revalidate();
        parentFrame.repaint();
        SwingUtilities.invokeLater(panelCalle::requestFocusInWindow);

        // Crear y adjuntar el overlay del temporizador (singleton) para que se muestre
        // solo después de que terminen los diálogos y comience el juego
        try {
            EstadoJuego.createTimerOverlayIfNeeded(parentFrame);
        } catch (Exception e) {
            System.err.println("No se pudo crear TimerOverlay desde dialogo3: " + e.getMessage());
        }
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

            // Calcular escala manteniendo relación de aspecto
            double escalaX = (double) areaDisponibleAncho / imgWidth;
            double escalaY = (double) areaDisponibleAlto / imgHeight;
            double escala = Math.min(escalaX, escalaY);
            
            int nuevoAncho = (int) (imgWidth * escala);
            int nuevoAlto = (int) (imgHeight * escala);
            
            // Centrar la imagen en el panel con los bordes
            int x = (panelWidth - nuevoAncho) / 2;
            int y = (panelHeight - nuevoAlto) / 2;

            g2d.drawImage(imagenDialogo, x, y, nuevoAncho, nuevoAlto, this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Indicador para continuar (texto dinámico según configuración)
            g2d.setColor(new Color(255, 255, 255, 
                (int)(Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128)));
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String texto = TIENE_DIALOGO4 ? 
                "Haz CLIC para continuar" : 
                "Haz CLIC para comenzar el juego";
            int textoWidth = g2d.getFontMetrics().stringWidth(texto);
            g2d.drawString(texto, (panelWidth - textoWidth) / 2, panelHeight - 50);
        }
    }

    /**
     * Método para detener animaciones si fuera necesario en el futuro.
     * Actualmente no hay animaciones activas que requieran limpieza.
     */
    public void detenerAnimaciones() {
        // No hay animaciones activas en este diálogo
    }
}