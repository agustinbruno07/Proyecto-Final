package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class casaIzquierda extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Image imagenCofre;
    private Timer gameLoop;
    private JLabel mensajeLabel;
    private Timer mensajeTimer;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean estaEnCofre = false;
    private boolean cofreAbierto = false;
    private colisiones colisiones;

    // 🔹 RESOLUCIÓN BASE
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 COORDENADAS BASE DEL COFRE
    private static final int BASE_COFRE_X = 357;
    private static final int BASE_COFRE_Y = 500;
    private static final int BASE_COFRE_W = 100;
    private static final int BASE_COFRE_H = 100;
    
    // 🔹 POSICIÓN INICIAL BASE
    private static final int BASE_PLAYER_X = 300;
    private static final int BASE_PLAYER_Y = 650;

    public casaIzquierda(JFrame parentFrame) {
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        fondo = new ImageIcon("src/resources/images/casa_izquierda.png").getImage();
        colisiones = new colisiones("src/resources/images/casa_izquierdaColisiones.png");

        if (EstadoJuego.isCofreAbierto()) {
            imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            cofreAbierto = true;
        } else {
            imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
        }

        // 🔹 CREAR JUGADOR CON POSICIÓN ESCALADA
        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);

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
            // 🔹 ACTUALIZAR POSICIÓN DEL MENSAJE
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

            // 🔹 LIMITAR A LA VENTANA ACTUAL
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
            verificarPosicionCofre();
            
            // 🔹 VERIFICAR SI TOCA LA PARTE INFERIOR
            verificarSalidaInferior();
            
            repaint();
        });
        gameLoop.start();
    }

    private void actualizarPosicionLabels() {
        int msgW = escalaManager.escalaAncho(400);
        int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
        int msgY = escalaManager.escalaY(100);
        int msgH = escalaManager.escalaAlto(60);
        mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
    }

    // 🔹 NUEVO MÉTODO: Verificar si toca la parte inferior del panel
    private void verificarSalidaInferior() {
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = getHeight();
        
        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - 5) {
            volverACalle();
        }
    }

    private void verificarPosicionCofre() {
        Rectangle jugadorBounds = player.getBounds();
        
        // 🔹 ESCALAR ÁREA DEL COFRE
        int cofreX = escalaManager.escalaX(BASE_COFRE_X);
        int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
        int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
        int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
        
        Rectangle cofreBounds = new Rectangle(cofreX, cofreY, cofreW, cofreH);
        estaEnCofre = jugadorBounds.intersects(cofreBounds);
    }

    private void abrirCofre() {
        if (!cofreAbierto) {
            imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            cofreAbierto = true;
            EstadoJuego.setCofreAbierto(true);
            mostrarMensaje("Encontre una llave");
            repaint();
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (!mensajeLabel.isVisible()) {
            mensajeLabel.setText(mensaje);
            mensajeLabel.setVisible(true);
            mensajeTimer.start();
        }
    }

    private void volverACalle() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            calle callePanel = new calle(20, 286);

            parentFrame.getContentPane().removeAll();
            parentFrame.getContentPane().add(callePanel);
            parentFrame.revalidate();
            parentFrame.repaint();
            SwingUtilities.invokeLater(callePanel::requestFocusInWindow);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 🔹 DIBUJAR FONDO ESCALADO
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        // 🔹 DIBUJAR COFRE ESCALADO
        int cofreX = escalaManager.escalaX(BASE_COFRE_X);
        int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
        int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
        int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
        g.drawImage(imagenCofre, cofreX, cofreY, cofreW, cofreH, this);
        
        // 🔹 DIBUJAR JUGADOR
        player.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;

        if (key == KeyEvent.VK_E && estaEnCofre && !cofreAbierto) {
            abrirCofre();
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