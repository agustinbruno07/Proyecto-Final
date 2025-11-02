package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class casaDerecha extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private JLabel mensajeLabel;
    private Timer mensajeTimer;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private colisiones colisiones;

    // 🔹 RESOLUCIÓN BASE
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 POSICIÓN INICIAL BASE
    private static final int BASE_PLAYER_X = 1000;
    private static final int BASE_PLAYER_Y = 650;

    public casaDerecha(JFrame parentFrame) {
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        fondo = new ImageIcon("src/resources/images/casas_derecha.png").getImage();
        colisiones = new colisiones("src/resources/images/casas_derechaColisiones.png");

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

    private void verificarSalidaInferior() {
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = getHeight();
        
        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - 5) {
            volverACalle();
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
            calle callePanel = new calle(1150, 286);

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