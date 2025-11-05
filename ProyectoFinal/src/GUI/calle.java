package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class calle extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private JLabel labelPuerta;
    private JLabel mensajeLabel;
    private Timer mensajeTimer;
    private colisiones colisiones;

    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean estaEnPuerta = false;
    private boolean estaEnTransicionIzquierda = false;
    private boolean estaEnTransicionDerecha = false;

    // 🔹 RESOLUCIÓN BASE (donde diseñaste todo)
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 COORDENADAS BASE (sin escalar)
    private static final int BASE_PUERTA_X = 641;
    private static final int BASE_PUERTA_Y = 489;
    private static final int BASE_PUERTA_W = 50;
    private static final int BASE_PUERTA_H = 50;
    
    private static final int BASE_TRANSICION_IZQ_MIN_Y = 200;
    private static final int BASE_TRANSICION_IZQ_MAX_Y = 300;
    private static final int BASE_TRANSICION_DER_MIN_Y = 200;
    private static final int BASE_TRANSICION_DER_MAX_Y = 300;

    public calle() {
        this(641, 692); 
    }

    public calle(int baseStartX, int baseStartY) {
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        Musica.detener();

        fondo = new ImageIcon("src/resources/images/puerta REWORK.png").getImage();
        
        // 🔹 CARGAR MÁSCARA DE COLISIONES PRIMERO
        colisiones = new colisiones("src/resources/images/puertaColisionesASd.png");
        
        // Usar siempre la posición base en calle (sacar spawn aleatorio)
        int startX, startY;
        if (baseStartX >= 0 && baseStartY >= 0) {
            startX = escalaManager.escalaX(baseStartX);
            startY = escalaManager.escalaY(baseStartY);
        } else {
            // Si no se especificó posición base, usar valores por defecto fijos
            startX = escalaManager.escalaX(641);
            startY = escalaManager.escalaY(692);
        }
        player = new jugador(startX, startY);
        
        labelPuerta = new JLabel();
        labelPuerta.setOpaque(false);
        add(labelPuerta);

        mensajeLabel = new JLabel("", JLabel.CENTER);
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        mensajeLabel.setForeground(Color.YELLOW);
        mensajeLabel.setBackground(new Color(0, 0, 0, 180));
        mensajeLabel.setOpaque(true);
        mensajeLabel.setVisible(false);
        add(mensajeLabel);

        mensajeTimer = new Timer(2000, e -> {
            mensajeLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        });
        mensajeTimer.setRepeats(false);

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        gameLoop = new Timer(16, e -> {
            actualizarPosicionLabels();

            int oldX = player.getX();
            int oldY = player.getY();

            if (upPressed)    player.moveUp();
            if (downPressed)  player.moveDown();
            if (leftPressed)  player.moveLeft();
            if (rightPressed) player.moveRight();
            
            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }
            
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
            verificarPosicionPuerta();
            verificarPosicionTransicionIzquierda();
            verificarPosicionTransicionDerecha();
            
            if (estaEnTransicionIzquierda) {
                cambiarACasaIzquierda();
                return;
            }

            if (estaEnTransicionDerecha) {
                cambiarACasaDerecha();
                return;
            }
            
            repaint();
        });
        gameLoop.start();

        // Actualizar posición de labels de inmediato y mostrar mensaje inicial durante 3 segundos
        actualizarPosicionLabels();
        if (!EstadoJuego.isMensajeCalleMostrado()) {
            mostrarMensajeDuracion("Tengo que encontrar la forma de entrar", 3000);
            EstadoJuego.setMensajeCalleMostrado(true);
        }
    }

    private void actualizarPosicionLabels() {
        int puertaX = escalaManager.escalaX(BASE_PUERTA_X);
        int puertaY = escalaManager.escalaY(BASE_PUERTA_Y);
        int puertaW = escalaManager.escalaAncho(BASE_PUERTA_W);
        int puertaH = escalaManager.escalaAlto(BASE_PUERTA_H);
        labelPuerta.setBounds(puertaX, puertaY, puertaW, puertaH);

        int msgW = escalaManager.escalaAncho(400);
        int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
        int msgY = escalaManager.escalaY(100);
        int msgH = escalaManager.escalaAlto(60);
        mensajeLabel.setBounds(msgX, msgY, msgW, msgH);

        // Posicionar el timer en la esquina superior derecha
        int timerW = escalaManager.escalaAncho(100);
        int timerH = escalaManager.escalaAlto(40);
        int timerX = escalaManager.getAnchoActual() - timerW - escalaManager.escalaX(20);
        int timerY = escalaManager.escalaY(20);
        // timerLabel.setBounds(timerX, timerY, timerW, timerH);
    }

    private void verificarPosicionPuerta() {
        Rectangle jugadorBounds = player.getBounds();
        
        int puertaX = escalaManager.escalaX(BASE_PUERTA_X);
        int puertaY = escalaManager.escalaY(BASE_PUERTA_Y);
        int puertaW = escalaManager.escalaAncho(BASE_PUERTA_W);
        int puertaH = escalaManager.escalaAlto(BASE_PUERTA_H);
        
        Rectangle puertaBounds = new Rectangle(puertaX, puertaY, puertaW, puertaH);
        estaEnPuerta = jugadorBounds.intersects(puertaBounds);
    }

    private void verificarPosicionTransicionIzquierda() {
        Rectangle jugadorBounds = player.getBounds();
        
        int minY = escalaManager.escalaY(BASE_TRANSICION_IZQ_MIN_Y);
        int maxY = escalaManager.escalaY(BASE_TRANSICION_IZQ_MAX_Y);
        
        estaEnTransicionIzquierda = (jugadorBounds.x <= 5 && 
                                     jugadorBounds.y >= minY && 
                                     jugadorBounds.y <= maxY);
    }

    private void verificarPosicionTransicionDerecha() {
        Rectangle jugadorBounds = player.getBounds();
        
        // 🔹 ESCALAR ÁREA DE TRANSICIÓN
        int minY = escalaManager.escalaY(BASE_TRANSICION_DER_MIN_Y);
        int maxY = escalaManager.escalaY(BASE_TRANSICION_DER_MAX_Y);
        int limiteX = escalaManager.getAnchoActual() - escalaManager.escalaUniforme(70);
        
        estaEnTransicionDerecha = (jugadorBounds.x >= limiteX && 
                                   jugadorBounds.y >= minY && 
                                   jugadorBounds.y <= maxY);
    }

    private void cambiarACasaIzquierda() {
         if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
         if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
         // overlay global se encarga del temporizador
         
         JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
         if (parentFrame != null) {
             casaIzquierda siguientePanel = new casaIzquierda(parentFrame);
             
             parentFrame.getContentPane().removeAll();
             parentFrame.getContentPane().add(siguientePanel);
             parentFrame.revalidate();
             parentFrame.repaint();
             
             SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
         }
     }
 
     private void cambiarACasaDerecha() {
         if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
         if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
         // overlay global se encarga del temporizador
         
         JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
         if (parentFrame != null) {
             casaDerecha siguientePanel = new casaDerecha(parentFrame);
             
             parentFrame.getContentPane().removeAll();
             parentFrame.getContentPane().add(siguientePanel);
             parentFrame.revalidate();
             parentFrame.repaint();
             
             SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
         }
     }
 
     private void cambiarACasaPrincipal() {
         if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
         if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
         // overlay global se encarga del temporizador
         
         JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
         if (parentFrame != null) {
             CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame, 600,650);
             
             parentFrame.getContentPane().removeAll();
             parentFrame.getContentPane().add(siguientePanel);
             parentFrame.revalidate();
             parentFrame.repaint();
             
             SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
         }
     }

    private void mostrarMensaje(String mensaje) {
        if (!mensajeLabel.isVisible()) {
            mensajeLabel.setText(mensaje);
            mensajeLabel.setVisible(true);
            mensajeTimer.start();
        }
    }

    // Nuevo método: muestra un mensaje por una duración en ms (no interfiere con el mensaje por defecto)
    private void mostrarMensajeDuracion(String mensaje, int ms) {
        // Detener el timer por defecto si está corriendo
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            mensajeTimer.stop();
        }
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
        Timer tempTimer = new Timer(ms, e -> {
            mensajeLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        });
        tempTimer.setRepeats(false);
        tempTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        player.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;
        
        if (key == KeyEvent.VK_E && estaEnPuerta) {
            // Requerir tanto la llave (cofre abierto) como haber hablado con BRR
            if (!EstadoJuego.isCofreAbierto()) {
                mostrarMensaje("Parece estar cerrada");
            } else if (!EstadoJuego.isHabladoConBrr()) {
                mostrarMensaje("Deberia buscar a algun testigo");
            } else {
                cambiarACasaPrincipal();
            }
        }
        
        if (key == KeyEvent.VK_A && estaEnTransicionIzquierda) {
            cambiarACasaIzquierda();
        }
        
        if (key == KeyEvent.VK_D && estaEnTransicionDerecha) {
            cambiarACasaDerecha();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    upPressed = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  downPressed = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  leftPressed = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}