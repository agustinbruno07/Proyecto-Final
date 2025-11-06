package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dialogo4 extends JPanel {
    private JFrame parentFrame;
    private Image imagenDialogo;
    private float alpha = 1.0f;

    // Configuraci�n de bordes
    private final int MARGEN_HORIZONTAL = 100;
    private final int MARGEN_VERTICAL = 80;

    public dialogo4(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);

        // Cargar la tercera imagen del di�logo
        imagenDialogo = new ImageIcon("src/resources/images/dialogo4.png").getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                avanzarAlJuego();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER || 
                    e.getKeyCode() == KeyEvent.VK_E) {
                    avanzarAlJuego();
                }
            }
        });
        requestFocusInWindow();
    }

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

            // Calcular �rea disponible (panel menos bordes)
            int areaDisponibleAncho = panelWidth - (2 * MARGEN_HORIZONTAL);
            int areaDisponibleAlto = panelHeight - (2 * MARGEN_VERTICAL);

            // Calcular escala manteniendo relaci�n de aspecto
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

            // Indicador para continuar
            g2d.setColor(new Color(255, 255, 255, (int)(Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128)));
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String texto = "Haz CLIC para comenzar el juego";
            int textoWidth = g2d.getFontMetrics().stringWidth(texto);
            g2d.drawString(texto, (panelWidth - textoWidth) / 2, panelHeight - 50);
        }
    }

    
}