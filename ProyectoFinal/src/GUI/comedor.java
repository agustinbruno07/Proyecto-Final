package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class comedor extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    private Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;

    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;

    // Mensajes temporales (misma estética que en CasaPrincipal/calle)
    private JLabel mensajeLabel;
    private Timer mensajeTimer;

    private static final int BASE_POSICION_IZQUIERDA_X = 100;
    private static final int BASE_POSICION_IZQUIERDA_Y = 500;
    private static final int BASE_POSICION_DERECHA_X = 1200;
    private static final int BASE_POSICION_DERECHA_Y = 500;

    private Integer entradaX = null;
    private Integer entradaY = null;

    public comedor(JFrame parentFrame, boolean desdePasillo1) {
        this(parentFrame, desdePasillo1, -1, -1);
    }

    public comedor(JFrame parentFrame, boolean desdePasillo1, int entryX, int entryY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // Configurar escala
        SwingUtilities.invokeLater(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                escalaManager.configurarEscala(getWidth(), getHeight());
            }
        });

        // Usar key bindings para mayor fiabilidad
        setupKeyBindings();

        // Cargar fondo con manejo de errores
        try {
            fondo = new ImageIcon("src/resources/images/comedor REWORK.png").getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar fondo: " + e.getMessage());
            fondo = crearFondoPorDefecto();
        }

        // Cargar colisiones con manejo de errores
        try {
            colisiones = new colisiones("src/resources/images/comedorMascara.png");
        } catch (Exception e) {
            System.out.println("Error al cargar colisiones: " + e.getMessage());
        }

        if (entryX >= 0 && entryY >= 0) {
            this.entradaX = entryX;
            this.entradaY = entryY;
        }

        // Posición inicial del jugador
        int startX, startY;
        if (desdePasillo1) {
            startX = escalaManager.escalaX(BASE_POSICION_IZQUIERDA_X);
            startY = escalaManager.escalaY(BASE_POSICION_IZQUIERDA_Y);
        } else {
            startX = escalaManager.escalaX(BASE_POSICION_DERECHA_X);
            startY = escalaManager.escalaY(BASE_POSICION_DERECHA_Y);
        }
        player = new jugador(startX, startY);

        spawnObjetosAleatorios();

        // Añadir KeyListener por compatibilidad, aunque usamos bindings
        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        // Inicializar label de mensaje temporal con la misma estética usada en otros paneles
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

        gameLoop = new Timer(16, e -> {
            updateGame();
        });
        gameLoop.start();
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        escalaManager.configurarEscala(width, height);
        // Actualizar posición del mensaje temporal cuando cambia el tamaño
        if (mensajeLabel != null) {
            int msgW = escalaManager.escalaAncho(400);
            int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
            int msgY = escalaManager.escalaY(100);
            int msgH = escalaManager.escalaAlto(60);
            mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
        }
    }
    
    private void updateGame() {
        int oldX = player.getX();
        int oldY = player.getY();

        if (upPressed)    player.moveUp();
        if (downPressed)  player.moveDown();
        if (leftPressed)  player.moveLeft();
        if (rightPressed) player.moveRight();

        if (!ignoreCollisions && colisiones != null) {
            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }
        }

        Rectangle bounds = new Rectangle(0, 0,
                escalaManager.getAnchoActual(),
                escalaManager.getAltoActual());
        player.clampTo(bounds);

        checkPickups();
        verificarSalidas();

        repaint();
    }
    
    private Image crearFondoPorDefecto() {
        int width = Math.max(escalaManager.getAnchoActual(), 800);
        int height = Math.max(escalaManager.getAltoActual(), 600);
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Fondo temático para comedor
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(80, 60, 40),
            width, height, new Color(50, 35, 25)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Texto de advertencia
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        String text = "Comedor - Fondo no disponible";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        g2d.dispose();
        return img;
    }

    private void spawnObjetosAleatorios() {
        String scene = "comedor";
        String[] objetos = new String[]{
            "Bandana de Capuchino Assasino.png",
            "Cascara de Chimpanzini Bananini.png",
            "Palo de Tung Tung.png",
            "Rueda de Boneca Ambalabu.png",
            "zapa.png"
        };

        try {
            List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
            if (list == null) list = new ArrayList<>();

            // Solo spawnear si no hay objetos existentes
            if (list.isEmpty()) {
                int[] pos = obtenerPosicionSpawnSegura(scene);
                
                if (pos != null) {
                    int idx = (int) (Math.random() * objetos.length);
                    EstadoJuego.SpawnedObject newObj = new EstadoJuego.SpawnedObject(
                        objetos[idx], 
                        pos[0], // Coordenadas base (no escaladas)
                        pos[1]  // Coordenadas base (no escaladas)
                    );
                    list.add(newObj);
                    EstadoJuego.setSpawnedObjects(scene, list);
                    System.out.println("Objeto spawnedo en comedor: " + objetos[idx] + " en " + pos[0] + "," + pos[1]);
                } else {
                    System.out.println("No se pudo encontrar posición segura para objeto en comedor");
                }
            } else {
                System.out.println("Ya existen objetos en comedor: " + list.size());
            }

            crearLabelsObjetos(scene, list);
            
        } catch (Exception e) {
            System.out.println("Error en spawnObjetosAleatorios: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private int[] obtenerPosicionSpawnSegura(String scene) {
        // Primero intentar con el sistema de spawn seguro
        if (colisiones != null) {
            int[] pos = SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos != null) {
                System.out.println("Spawn seguro encontrado: " + pos[0] + "," + pos[1]);
                return pos;
            }
        }
        
        // Si no funciona, intentar spawn aleatorio
        int[] pos = SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
        if (pos != null) {
            System.out.println("Spawn aleatorio encontrado: " + pos[0] + "," + pos[1]);
            return pos;
        }
        
        // Si todo falla, usar posición por defecto para comedor
        System.out.println("Usando spawn por defecto para comedor");
        return new int[]{600, 500}; // Posición central por defecto
    }
    
    private void crearLabelsObjetos(String scene, List<EstadoJuego.SpawnedObject> soList) {
        // Limpiar labels existentes
        for (JLabel lbl : objetosLabels.values()) {
            this.remove(lbl);
        }
        objetosLabels.clear();
        
        if (soList == null || soList.isEmpty()) {
            System.out.println("No hay objetos para crear labels en comedor");
            return;
        }
        
        int size = escalaManager.escalaUniforme(64);
        int objetosCreados = 0;
        
        for (EstadoJuego.SpawnedObject so : soList) {
            if (so.recogido) {
                System.out.println("Objeto " + so.nombre + " ya fue recogido, omitiendo");
                continue;
            }
            
            try {
                Image img = cargarImagenObjeto(so.nombre);
                if (img == null) {
                    System.out.println("No se pudo cargar imagen para: " + so.nombre);
                    continue;
                }
                
                Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                JLabel itemLabel = new JLabel(new ImageIcon(scaled));
                
                // Convertir coordenadas base a coordenadas de pantalla escaladas
                int x = escalaManager.escalaX(so.x) - size/2;
                int y = escalaManager.escalaY(so.y) - size/2;
                
                itemLabel.setBounds(x, y, size, size);
                this.add(itemLabel);
                objetosLabels.put(so, itemLabel);
                objetosCreados++;
                
                System.out.println("Label creado para objeto: " + so.nombre + " en posición escalada " + x + "," + y);
                
            } catch (Exception ex) {
                System.out.println("Error creando label para " + so.nombre + ": " + ex.getMessage());
            }
        }
        
        System.out.println("Total labels creados en comedor: " + objetosCreados);
        this.revalidate();
        this.repaint();
    }
    
    private Image cargarImagenObjeto(String nombre) {
        try {
            // Intentar desde recursos
            if (getClass().getResource("/resources/images/" + nombre) != null) {
                return new ImageIcon(getClass().getResource("/resources/images/" + nombre)).getImage();
            } else {
                // Intentar desde sistema de archivos
                return new ImageIcon("src/resources/images/" + nombre).getImage();
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar imagen: " + nombre);
            return crearIconoObjetoPorDefecto();
        }
    }
    
    private Image crearIconoObjetoPorDefecto() {
        int size = escalaManager.escalaUniforme(64);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(5, 5, size-10, size-10);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(5, 5, size-10, size-10);
        g2d.drawString("?", size/2-5, size/2+5);
        
        g2d.dispose();
        return img;
    }

    private void checkPickups() {
        if (player == null) return;
        
        Rectangle playerBounds = player.getBounds();
        String scene = "comedor";
        nearbyObject = null;
        
        List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list == null) return;
        
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            
            if (playerBounds.intersects(lbl.getBounds())) {
                nearbyObject = so;
                // Mostrar el mensaje con la misma estética/posición y duración que en otros paneles
                mostrarMensajeDuracion("Presiona E para recoger " + so.nombre, 1000);
                 break;
            }
        }
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        System.out.println("Recogiendo objeto en comedor: " + nearbyObject.nombre);
        
        String scene = "comedor";
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        
        nearbyObject = null;
        this.revalidate();
        this.repaint();
        System.out.println("Evidencia recogida en comedor: total=" + EstadoJuego.getObjetosRecogidos());
    }

    private void verificarSalidas() {
        if (player == null) return;
        
        Rectangle jugadorBounds = player.getBounds();
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        int margenSalida = escalaManager.escalaX(5);

        if (jugadorBounds.x <= margenSalida) {
            volverAPasillo1();
        }
        if (jugadorBounds.x + jugadorBounds.width >= panelWidth - margenSalida) {
            volverAPasillo2();
        }
    }

    private void volverAPasillo1() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        if (parentFrame != null) {
            try {
                pasillo1 siguientePanel;
                if (entradaX != null && entradaY != null) {
                    int desplazamiento = escalaManager.escalaY(100);
                    int targetY = entradaY + desplazamiento;
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

                // Configurar escala para el nuevo panel
                escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
                
                siguientePanel.requestFocusInWindow();
            } catch (Exception e) {
                System.out.println("Error al cambiar a pasillo1: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void volverAPasillo2() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        if (parentFrame != null) {
            try {
                pasillo2 siguientePanel;
                if (entradaX != null && entradaY != null) {
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

                // Configurar escala para el nuevo panel
                escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
                
                siguientePanel.requestFocusInWindow();
            } catch (Exception e) {
                System.out.println("Error al cambiar a pasillo2: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Dibujar fondo escalado
        if (fondo != null) {
            g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        }
        
        if (player != null) player.draw(g2);
        
        // El indicador de recogida se muestra mediante el mensaje temporal (mensajeLabel)
        
        // Debug: mostrar posición del jugador
        if (DEBUG) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, escalaManager.escalaFuente(12)));
            g2.drawString("Pos: " + player.getX() + "," + player.getY(), 10, getHeight() - 10);
        }
    }

    // Mostrar un mensaje temporal en el panel con la misma estética y posición que en CasaPrincipal/calle
    private void mostrarMensajeDuracion(String mensaje, int ms) {
        if (mensajeLabel == null) return;
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            mensajeTimer.stop();
        }
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
        Timer temp = new Timer(ms, e -> {
            mensajeLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        });
        temp.setRepeats(false);
        temp.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_E && nearbyObject != null) {
            collectNearbyObject();
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

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "toggle.collision");
        am.put("toggle.collision", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ignoreCollisions = !ignoreCollisions;
                System.out.println("Colisiones: " + (!ignoreCollisions ? "ACTIVADAS" : "DESACTIVADAS"));
            }
        });

        am.put("up.press", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { upPressed = true; } 
        });
        am.put("up.release", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { upPressed = false; } 
        });
        am.put("down.press", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { downPressed = true; } 
        });
        am.put("down.release", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { downPressed = false; } 
        });
        am.put("left.press", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { leftPressed = true; } 
        });
        am.put("left.release", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { leftPressed = false; } 
        });
        am.put("right.press", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { rightPressed = true; } 
        });
        am.put("right.release", new AbstractAction() { 
            @Override
            public void actionPerformed(ActionEvent e) { rightPressed = false; } 
        });
    }
}
