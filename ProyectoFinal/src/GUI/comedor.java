package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class comedor extends JPanel implements KeyListener {
    
    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    
    // 🔹 VARIABLES PARA SISTEMA DE COLISIONES
    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;
    
    // 🔹 RESOLUCIÓN BASE
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 POSICIONES INICIALES BASE SEGÚN DESDE DÓNDE SE VENGA
    private static final int BASE_POSICION_IZQUIERDA_X = 100;
    private static final int BASE_POSICION_IZQUIERDA_Y = 500;
    private static final int BASE_POSICION_DERECHA_X = 1200;
    private static final int BASE_POSICION_DERECHA_Y = 500;

    // 🔹 Coordenadas donde se entró (si se proporcionan)
    private Integer entradaX = null;
    private Integer entradaY = null;

    // Constructor existente mantiene compatibilidad
    public comedor(JFrame parentFrame, boolean desdePasillo1) {
        this(parentFrame, desdePasillo1, -1, -1);
    }

    // Nuevo constructor que acepta coordenadas de entrada (en coordenadas de pantalla/escaladas)
    public comedor(JFrame parentFrame, boolean desdePasillo1, int entryX, int entryY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 🔹 CONFIGURAR KEY BINDINGS
        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/comedor REWORK.png").getImage();
        
        // 🔹 CARGAR MÁSCARA DE COLISIONES
        colisiones = new colisiones("src/resources/images/comedorMascara.png");

        // Si se proporcionaron coordenadas de entrada válidas, úsalas y guárdalas
        if (entryX >= 0 && entryY >= 0) {
            this.entradaX = entryX;
            this.entradaY = entryY;
            // No usamos las coordenadas del pasillo para posicionar al jugador dentro del comedor,
            // solo las guardamos para devolver al jugador al salir.
        }

        // 🔹 POSICIÓN INICIAL ESCALADA SEGÚN DESDE QUÉ PASILLO SE VENGA
        int startX, startY;
        if (desdePasillo1) {
            startX = escalaManager.escalaX(BASE_POSICION_IZQUIERDA_X);
            startY = escalaManager.escalaY(BASE_POSICION_IZQUIERDA_Y);
        } else {
            startX = escalaManager.escalaX(BASE_POSICION_DERECHA_X);
            startY = escalaManager.escalaY(BASE_POSICION_DERECHA_Y);
        }
        player = new jugador(startX, startY);

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);


        gameLoop = new Timer(16, e -> {
            // 🔹 GUARDAR POSICIÓN ANTERIOR
            int oldX = player.getX();
            int oldY = player.getY();

            // 🔹 MOVER JUGADOR
            if (upPressed)    player.moveUp();
            if (downPressed)  player.moveDown();
            if (leftPressed)  player.moveLeft();
            if (rightPressed) player.moveRight();

            // 🔹 VERIFICAR COLISIONES Y REVERTIR SI HAY COLISIÓN
            if (!ignoreCollisions) {
                if (colisiones.hayColision(player.getBounds())) {
                    player.setPosition(oldX, oldY);
                }
            }

            // 🔹 LIMITAR A LA VENTANA ACTUAL
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
   
            
            verificarSalidas();
            
            repaint();
        });
        gameLoop.start();
    }

    private void verificarSalidas() {
        Rectangle jugadorBounds = player.getBounds();
        int panelWidth = escalaManager.getAnchoActual();
        int panelHeight = escalaManager.getAltoActual();
        
        // Salir por la izquierda -> volver a pasillo1
        if (jugadorBounds.x <= 5) {
            volverAPasillo1();
        }
        // Salir por la derecha -> volver a pasillo2
        if (jugadorBounds.x + jugadorBounds.width >= panelWidth - 5) {
            volverAPasillo2();
        }
    }

    private void volverAPasillo1() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        pasillo1 siguientePanel;
        if (entradaX != null && entradaY != null) {
            // Desplazar la Y 500px (escalados) hacia abajo para que el jugador aparezca más abajo
            int desplazamiento = escalaManager.escalaY(100);
            int targetY = entradaY + desplazamiento;
            // Evitar que quede fuera del panel: limitar por el alto actual menos un margen
            int maxY = escalaManager.getAltoActual() - 50;
            if (targetY > maxY) targetY = maxY;
            siguientePanel = new pasillo1(parentFrame, entradaX, targetY);
        } else {
            siguientePanel = new pasillo1(parentFrame);
        }
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void volverAPasillo2() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        pasillo2 siguientePanel;
        if (entradaX != null && entradaY != null) {
            // Desplazar la Y 500px (escalados) hacia abajo para que el jugador aparezca más abajo
            int desplazamiento = escalaManager.escalaY(100);
            int targetY = entradaY + desplazamiento;
            int maxY = escalaManager.getAltoActual() - 50;
            if (targetY > maxY) targetY = maxY;
            siguientePanel = new pasillo2(parentFrame, entradaX, targetY);
        } else {
            siguientePanel = new pasillo2(parentFrame);
        }
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // 🔹 DIBUJAR FONDO ESCALADO
        g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        // 🔹 DIBUJAR JUGADOR
        player.draw(g2);
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

    // 🔹 CONFIGURAR KEY BINDINGS (incluye toggle de colisiones con 'C')
    private void setupKeyBindings() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up.release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "up.release");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down.release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "down.release");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left.release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left.release");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right.release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right.press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right.release");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "toggle.collision");
        am.put("toggle.collision", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ignoreCollisions = !ignoreCollisions;
                System.out.println("Colisiones: " + (!ignoreCollisions ? "ACTIVADAS" : "DESACTIVADAS"));
            }
        });

        am.put("up.press", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { upPressed = true; }
        });
        am.put("up.release", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { upPressed = false; }
        });

        am.put("down.press", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { downPressed = true; }
        });
        am.put("down.release", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { downPressed = false; }
        });

        am.put("left.press", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { leftPressed = true; }
        });
        am.put("left.release", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { leftPressed = false; }
        });

        am.put("right.press", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { rightPressed = true; }
        });
        am.put("right.release", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { rightPressed = false; }
        });
    }
}