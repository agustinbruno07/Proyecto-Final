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
    private boolean estaEnPuerta = false; // nueva: proximidad a la puerta
    // Evita transiciones repetidas al laberinto
    private boolean autoEnteredLaberinto = false;

    // 🔹 RESOLUCIÓN BASE
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 COORDENADAS BASE DEL COFRE
    private static final int BASE_COFRE_X = 357;
    private static final int BASE_COFRE_Y = 500;
    private static final int BASE_COFRE_W = 100;
    private static final int BASE_COFRE_H = 100;
    
    // 🔹 COORDENADAS BASE PARA TRANSICIÓN AL LABERINTO
    private static final int BASE_LABERINTO_X = 20;
    private static final int BASE_LABERINTO_Y = 195;
    private static final int BASE_LABERINTO_W = 50;
    private static final int BASE_LABERINTO_H = 50;
    
    // 🔹 POSICIÓN INICIAL BASE
    private static final int BASE_PLAYER_X = 300;
    private static final int BASE_PLAYER_Y = 650;

    // 🔹 CONSTRUCTOR CON COORDENADAS PERSONALIZADAS
    public casaIzquierda(JFrame parentFrame, int baseX, int baseY) {
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        fondo = new ImageIcon("src/resources/images/casaIzquierda REWORK.png").getImage();
        colisiones = new colisiones("src/resources/images/casaIzquierdaMascara REWORK.png");

        if (EstadoJuego.isCofreAbierto()) {
            imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            cofreAbierto = true;
        } else {
            imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
        }

        // 🔹 USAR LAS COORDENADAS PROPORCIONADAS EN LUGAR DE LAS FIJAS
        int startX = escalaManager.escalaX(baseX);
        int startY = escalaManager.escalaY(baseY);
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
            
            verificarPosicionCofre();
            verificarTransicionLaberinto(); 
            verificarSalidaInferior();
            // Actualizar indicador de proximidad (cofre/puerta)
            actualizarIndicadorProximidad();
            
            repaint();
        });
        gameLoop.start();
    }

    public casaIzquierda(JFrame parentFrame) {
        this(parentFrame, BASE_PLAYER_X, BASE_PLAYER_Y);
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

    private void verificarPosicionCofre() {
        Rectangle jugadorBounds = player.getBounds();
        
        int cofreX = escalaManager.escalaX(BASE_COFRE_X);
        int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
        int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
        int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
        
        Rectangle cofreBounds = new Rectangle(cofreX, cofreY, cofreW, cofreH);
        estaEnCofre = jugadorBounds.intersects(cofreBounds);

        // Nota: ya no manejamos la visibilidad del label aquí. Se centraliza en
        // actualizarIndicadorProximidad() para evitar que una verificación sobrescriba a la otra.
    }

    private void verificarTransicionLaberinto() {
        Rectangle jugadorBounds = player.getBounds();
        
        int laberintoX = escalaManager.escalaX(BASE_LABERINTO_X);
        int laberintoY = escalaManager.escalaY(BASE_LABERINTO_Y);
        int laberintoW = escalaManager.escalaAncho(BASE_LABERINTO_W);
        int laberintoH = escalaManager.escalaAlto(BASE_LABERINTO_H);
        
        Rectangle laberintoBounds = new Rectangle(laberintoX, laberintoY, laberintoW, laberintoH);
        
        // Detectar intersección. Si el jugador entra en la zona del laberinto,
        // realizar la transición automáticamente (una sola vez).
        boolean intersect = jugadorBounds.intersects(laberintoBounds);
        estaEnPuerta = intersect;
        if (intersect && !autoEnteredLaberinto) {
            autoEnteredLaberinto = true;
            cambiarALaberinto();
        }
    }

    // Decide qué indicador mostrar (prioridad: cofre > puerta). No sobrescribe
    // mensajes temporales (p. ej. "Encontre una llave") mientras el timer esté activo.
    private void actualizarIndicadorProximidad() {
        // Si hay un mensaje temporal en curso, no mostrar el indicador
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            return;
        }

        if (estaEnCofre && !cofreAbierto) {
            mensajeLabel.setText("E para abrir");
            mensajeLabel.setVisible(true);
        } else {
            // Eliminado: no mostrar "E para entrar" desde dentro de casaIzquierda.
            // El prompt de entrada al laberinto se mostrará desde CasaPrincipal cuando el
            // jugador esté cerca de la puerta exterior.
            mensajeLabel.setVisible(false);
        }
    }

    private void cambiarALaberinto() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame != null) {
            laberinto laberintoPanel = new laberinto(parentFrame); 

            parentFrame.getContentPane().removeAll();
            parentFrame.getContentPane().add(laberintoPanel);
            parentFrame.revalidate();
            parentFrame.repaint();
            SwingUtilities.invokeLater(laberintoPanel::requestFocusInWindow);
        }
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
        // Forzar la visualización del mensaje: detener timer activo, actualizar texto
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            mensajeTimer.stop();
        }
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
        if (mensajeTimer != null) mensajeTimer.start();
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

        if (key == KeyEvent.VK_E) {
            // Priorizar abrir cofre si está cerca y no abierto
            if (estaEnCofre && !cofreAbierto) {
                abrirCofre();
            } else if (estaEnPuerta) {
                // ahora la entrada se realiza automáticamente al pisar la zona, pero
                // mantenemos la opción manual como respaldo si hace falta.
                cambiarALaberinto();
            }
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