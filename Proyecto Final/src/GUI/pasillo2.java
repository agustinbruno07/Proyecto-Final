package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class pasillo2 extends JPanel implements KeyListener {
    
    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    
    private boolean ignoreCollisions = false;
  
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    private static final int BASE_PLAYER_X = 650;
    private static final int BASE_PLAYER_Y = 680;

    public pasillo2(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/pasillo2 REWORK.png").getImage();
        
        colisiones = new colisiones("src/resources/images/pasillo2 MASCARA.png");

        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);


        gameLoop = new Timer(16, e -> {
            int oldX = player.getX();
            int oldY = player.getY();

            if (upPressed)    player.moveUp();
            if (downPressed)  player.moveDown();
            if (leftPressed)  player.moveLeft();
            if (rightPressed) player.moveRight();

            if (!ignoreCollisions) {
                if (colisiones.hayColision(player.getBounds())) {
                    player.setPosition(oldX, oldY);
                }
            }
            System.out.println("Jugador Pos: (" + player.getX() + ", " + player.getY() + ")");
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
          
            
            verificarSalidaInferior();
            verificarSalidaSuperior();
            
            repaint();
        });
        gameLoop.start();
    }

    public pasillo2(JFrame parentFrame, int entryX, int entryY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/pasillo2 REWORK.png").getImage();
        colisiones = new colisiones("src/resources/images/pasillo2 MASCARA.png");

        player = new jugador(entryX, entryY);

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);


        gameLoop = new Timer(16, e -> {
            int oldX = player.getX();
            int oldY = player.getY();

            if (upPressed)    player.moveUp();
            if (downPressed)  player.moveDown();
            if (leftPressed)  player.moveLeft();
            if (rightPressed) player.moveRight();

            if (!ignoreCollisions) {
                if (colisiones.hayColision(player.getBounds())) {
                    player.setPosition(oldX, oldY);
                }
            }
            System.out.println("Jugador Pos: (" + player.getX() + ", " + player.getY() + ")");
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
            verificarSalidaInferior();
            verificarSalidaSuperior();
            
            repaint();
        });
        gameLoop.start();
    }

    private void verificarSalidaInferior() {
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = escalaManager.getAltoActual();
        
        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - 5) {
            volverACasaPrincipal();
        }
    }

    private void verificarSalidaSuperior() {
       
        if (player.getY()<=160) {
            irAComedor();
        }
    }

    private void volverACasaPrincipal() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame, 
            CasaPrincipal.BASE_RETORNO_PASILLO2_X, 
            CasaPrincipal.BASE_RETORNO_PASILLO2_Y);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void irAComedor() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        comedor siguientePanel = new comedor(parentFrame, false, player.getX(), player.getY()); 
        
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
        
        g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
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