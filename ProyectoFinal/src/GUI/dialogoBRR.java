package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class dialogoBRR extends JPanel {
    private JFrame parentFrame;
    private Image imagenDialogo;
    private float alpha = 0.0f;
    private Timer fadeTimer;
    private boolean fadeCompletado = false;
    private int dialogoActual = 1;
    private final int TOTAL_DIALOGOS = 5;

    private final int MARGEN_HORIZONTAL = 100;
    private final int MARGEN_VERTICAL = 80;

    public dialogoBRR(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);

        cargarImagenDialogo();
       

        iniciarFadeIn();

        configurarControles();
        
        setFocusable(true);
        requestFocusInWindow();
    }

    private void cargarImagenDialogo() {
            String ruta = "src/resources/images/dialogoBRR" + dialogoActual + ".png";
            imagenDialogo = new ImageIcon(ruta).getImage();
     
    }

    private void iniciarFadeIn() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        
        alpha = 0.0f;
        fadeCompletado = false;
        
        fadeTimer = new Timer(30, e -> {
            alpha += 0.03f; // Un poco más rápido que los diálogos normales
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
                fadeCompletado = true;
            }
            repaint();
        });
        fadeTimer.start();
    }

    private void configurarControles() {
      
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (fadeCompletado) {
                    avanzarSiguiente();
                } else {
                    completarFadeInmediato();
                }
            }
        });
         
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (fadeCompletado) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || 
                        e.getKeyCode() == KeyEvent.VK_ENTER || 
                        e.getKeyCode() == KeyEvent.VK_E ||
                        e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        avanzarSiguiente();
                    }
                } else {
                   
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || 
                        e.getKeyCode() == KeyEvent.VK_ENTER || 
                        e.getKeyCode() == KeyEvent.VK_E) {
                        completarFadeInmediato();
                    }
                }
                
                
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    terminarDialogo();
                }
            }
        });
    }

    private void completarFadeInmediato() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
        alpha = 1.0f;
        fadeCompletado = true;
        repaint();
    }

    private void avanzarSiguiente() {

        dialogoActual++;
        
        if (dialogoActual <= TOTAL_DIALOGOS) {
           
            cargarImagenDialogo();
            iniciarFadeIn();
        } else {
            terminarDialogo();
        }
    }

    private void terminarDialogo() {
        detenerAnimaciones();
     
          
       

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
           
            casaDerecha casaPanel = new casaDerecha(parentFrame);
            
            parentFrame.getContentPane().removeAll();
            parentFrame.getContentPane().add(casaPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
            SwingUtilities.invokeLater(casaPanel::requestFocusInWindow);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

     
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

            if (fadeCompletado) {
                dibujarIndicadorContinuar(g2d, panelWidth, panelHeight);
            }
            
            dibujarContadorProgreso(g2d, panelWidth);
    
    }

    private void dibujarIndicadorContinuar(Graphics2D g2d, int panelWidth, int panelHeight) {
        int alphaPulse = (int)(Math.sin(System.currentTimeMillis() / 300.0) * 127 + 128);
        g2d.setColor(new Color(255, 255, 255, alphaPulse));
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        
        String texto;
        if (dialogoActual < TOTAL_DIALOGOS) {
            texto = "Haz CLICK ";
        } else {
            texto = "Haz CLICK para finalizar";
        }
        
        int textoWidth = g2d.getFontMetrics().stringWidth(texto);
        g2d.drawString(texto, (panelWidth - textoWidth) / 2, panelHeight - 50);
    }

    private void dibujarContadorProgreso(Graphics2D g2d, int panelWidth) {
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        String progreso = dialogoActual + "/" + TOTAL_DIALOGOS;
        int progWidth = g2d.getFontMetrics().stringWidth(progreso);
        g2d.drawString(progreso, panelWidth - progWidth - 20, 30);
    }

    public void detenerAnimaciones() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
    }
}