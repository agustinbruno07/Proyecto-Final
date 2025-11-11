package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class pasillo1 extends JPanel implements KeyListener {
    
    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new java.util.HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;
    
    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
   
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    private static final int BASE_PLAYER_X = 650;
    private static final int BASE_PLAYER_Y = 680;

    public pasillo1(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/pasillo1 REWORK.png").getImage();
        
        colisiones = new colisiones("src/resources/images/pasillo1 Mascara.png");

        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);
        
        spawnObjetosAleatorios();

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
            
            checkPickups();

            repaint();
        });
        gameLoop.start();
    }

    public pasillo1(JFrame parentFrame, int entryX, int entryY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/pasillo1 REWORK.png").getImage();
        
        colisiones = new colisiones("src/resources/images/pasillo1 Mascara.png");

             int startX, startY;
        if (entryX >= 0 && entryY >= 0) {
           
            if (entryX > escalaManager.BASE_WIDTH || entryY > escalaManager.BASE_HEIGHT) {
                startX = escalaManager.escalaX(entryX);
                startY = escalaManager.escalaY(entryY);
            } else {
                startX = entryX;
                startY = entryY;
            }
            player = new jugador(startX, startY);
        } else {
            startX = escalaManager.escalaX(BASE_PLAYER_X);
            startY = escalaManager.escalaY(BASE_PLAYER_Y);
            player = new jugador(startX, startY);
        }
        if (colisiones != null && colisiones.hayColision(player.getBounds())) {
            int[] seguro = SistemaSpawnJuego.obtenerSpawnSeguro("pasillo1", colisiones, false);
            if (seguro != null) {
                player.setPosition(seguro[0], seguro[1]);
            } else {
                int py = startY;
                int maxY = Math.max(escalaManager.getAltoActual() - 60, py + 1);
                boolean found = false;
                for (int tryY = py; tryY <= maxY; tryY += 10) {
                    player.setPosition(startX, tryY);
                    if (!colisiones.hayColision(player.getBounds())) {
                        found = true; break;
                    }
                }
                if (!found) {
                    player.setPosition(startX, startY);
                }
            }
        }
        
        spawnObjetosAleatorios();

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
            
            checkPickups();

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
        Rectangle jugadorBounds = player.getBounds();
        
        if (jugadorBounds.y <= 5) {
            irAComedor();
        }
    }

    private void volverACasaPrincipal() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame, 
            CasaPrincipal.BASE_RETORNO_PASILLO1_X, 
            CasaPrincipal.BASE_RETORNO_PASILLO1_Y);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void irAComedor() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        
        // Pasar la posición actual del jugador al comedor para que al volver se regrese al mismo punto
        comedor siguientePanel = new comedor(parentFrame, true, player.getX(), player.getY());
        
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
        if (key == KeyEvent.VK_E) {
            if (nearbyObject != null) {
                collectNearbyObject();
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

        // 🔹 TECLA 'C' PARA TOGGLE DE COLISIONES (útil para debug)
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
    
    // Método para spawnear objetos aleatoriamente en las zonas definidas por SistemaSpawnJuego
    private void spawnObjetosAleatorios() {
        String scene = "pasillo1";
        // Obtener la evidencia exclusiva asignada para esta escena
        String evidenciaAsignada = EstadoJuego.getOrAssignUniqueEvidenceForScene(scene);

        String[] objetos = new String[]{ evidenciaAsignada };

        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list.isEmpty()) {
            // Solo un objeto por sección: intentar obtener una posición segura para un objeto
            int[] pos = SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos == null) {
                // intentar un spawn aleatorio dentro de zonas permitidas
                pos = SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
            }
            if (pos == null) {
                // última opción: colocar en el centro de la primera zona permitida (forzar aparición)
                java.awt.Rectangle[] zonas = SistemaSpawnJuego.obtenerZonasSpawnParaObjetos(scene);
                if (zonas != null && zonas.length > 0) {
                    java.awt.Rectangle z = zonas[0];
                    pos = new int[]{z.x + z.width/2, z.y + z.height/2};
                }
            }
            if (pos != null) {
                list.add(new EstadoJuego.SpawnedObject(evidenciaAsignada, pos[0], pos[1]));
            }
            EstadoJuego.setSpawnedObjects(scene, list);
        }

        // Limpiar labels antiguos
        for (JLabel lbl : objetosLabels.values()) {
            this.remove(lbl);
        }
        objetosLabels.clear();

        // Agrandar un poco las imágenes de los objetos
        int size = escalaManager.escalaUniforme(64);
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            try {
                Image img;
                if (getClass().getResource("/resources/images/" + so.nombre) != null) {
                    img = new ImageIcon(getClass().getResource("/resources/images/" + so.nombre)).getImage();
                } else {
                    img = new ImageIcon("src/resources/images/" + so.nombre).getImage();
                }
                Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                JLabel itemLabel = new JLabel(new ImageIcon(scaled));
                int x = escalaManager.escalaX(so.x) - size/2;
                int y = escalaManager.escalaY(so.y) - size/2;
                itemLabel.setBounds(x, y, size, size);
                this.add(itemLabel);
                objetosLabels.put(so, itemLabel);
            } catch (Exception ex) {
                System.out.println("No se pudo cargar objeto: " + so.nombre + " -> " + ex.getMessage());
            }
        }
    }
    
    // Método para comprobar recogidas (pickups) en la posición actual del jugador
    private void checkPickups() {
        if (player == null) return;
        Rectangle playerBounds = player.getBounds();
        String scene = "pasillo1";
        nearbyObject = null;
        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            if (playerBounds.intersects(lbl.getBounds())) {
                nearbyObject = so;
                System.out.println("Cerca de objeto: E para recoger");
                break;
            }
        }
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        int objetosAntes = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos antes de recoger: " + objetosAntes);
        
        String scene = "pasillo1";
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        
        nearbyObject = null;
        this.revalidate();
        this.repaint();
        
        int objetosDespues = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos después de recoger: " + objetosDespues);
        
        if (objetosAntes == 4 && objetosDespues == 5) {
            // Mensaje especial cuando se recolecta el 5to objeto
            System.out.println("Tendre que hablar con Brr brr patapim");
            EstadoJuego.setPuedeMostrarDialogosEspeciales(true);
        }
        
        System.out.println("Evidencia recogida: total=" + EstadoJuego.getObjetosRecogidos());
    }
}