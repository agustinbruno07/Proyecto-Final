package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dialogo2 extends JPanel {
    private JFrame parentFrame;
    private Image imagenDialogo;
    private float alpha = 1.0f;

    // Configuración de bordes
    private final int MARGEN_HORIZONTAL = 100;
    private final int MARGEN_VERTICAL = 80;

    public dialogo2(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);

        // Cargar la segunda imagen del diálogo
        imagenDialogo = new ImageIcon("src/resources/images/dialogo2.png").getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                avanzarAlSiguienteDialogo();
            }
        });

        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE || 
                    e.getKeyCode() == KeyEvent.VK_ENTER || 
                    e.getKeyCode() == KeyEvent.VK_E) {
                    avanzarAlSiguienteDialogo();
                }
            }
        });
        requestFocusInWindow();
    }

    private void avanzarAlSiguienteDialogo() {
        parentFrame.getContentPane().removeAll();
        dialogo3 nuevoDialogo = new dialogo3(parentFrame);
        parentFrame.getContentPane().add(nuevoDialogo);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(() -> {
            nuevoDialogo.requestFocusInWindow();
        });
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

            int areaDisponibleAncho = panelWidth - (2 * MARGEN_HORIZONTAL);
            int areaDisponibleAlto = panelHeight - (2 * MARGEN_VERTICAL);

            double escalaX = (double) areaDisponibleAncho / imgWidth;
            double escalaY = (double) areaDisponibleAlto / imgHeight;
            double escala = Math.min(escalaX, escalaY);
            
            int nuevoAncho = (int) (imgWidth * escala);
            int nuevoAlto = (int) (imgHeight * escala);
            
            int x = (panelWidth - nuevoAncho) / 2;
            int y = (panelHeight - nuevoAlto) / 2;

            g2d.drawImage(imagenDialogo, x, y, nuevoAncho, nuevoAlto, this);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            g2d.setColor(new Color(255, 255, 255, 
                (int)(Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128)));
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String texto = "Haz CLIC para continuar";
            int textoWidth = g2d.getFontMetrics().stringWidth(texto);
            g2d.drawString(texto, (panelWidth - textoWidth) / 2, panelHeight - 50);
        }
    }

   
  
}