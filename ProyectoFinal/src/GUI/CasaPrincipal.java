package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CasaPrincipal extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private javax.swing.Timer gameLoop;
    private colisiones colisiones;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new java.util.HashMap<>();
    private javax.swing.Timer pickupTimer;
    // objeto cercano que se puede recoger con 'E'
    private EstadoJuego.SpawnedObject nearbyObject = null;

    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    private static final int BASE_PLAYER_X = 720;
    private static final int BASE_PLAYER_Y = 800;
    
    private static final int BASE_PUERTA1_X = 270;
    private static final int BASE_PUERTA1_Y = 100;
    private static final int BASE_PUERTA1_W = 80;
    private static final int BASE_PUERTA1_H = 120;
    
    private static final int BASE_PUERTA2_X = 1120;
    private static final int BASE_PUERTA2_Y = 110;
    private static final int BASE_PUERTA2_W = 80;
    private static final int BASE_PUERTA2_H = 120;

    // 🔹 POSICIONES BASE DE LAS PUERTAS (Pasillos)
    private static final int BASE_PUERTA_PASILLO1_X = 270;
    private static final int BASE_PUERTA_PASILLO1_Y = 580;
    private static final int BASE_PUERTA_PASILLO1_W = 80;
    private static final int BASE_PUERTA_PASILLO1_H = 120;

    private static final int BASE_PUERTA_PASILLO2_X = 1100;
    private static final int BASE_PUERTA_PASILLO2_Y = 590;
    private static final int BASE_PUERTA_PASILLO2_W = 80;
    private static final int BASE_PUERTA_PASILLO2_H = 120;
   
    public static final int BASE_RETORNO_PUERTA1_X = 280;
    public static final int BASE_RETORNO_PUERTA1_Y = 160;

    public static final int BASE_RETORNO_PUERTA2_X = 1120;
    public static final int BASE_RETORNO_PUERTA2_Y = 160;

    public static final int BASE_RETORNO_PASILLO1_X = 270;
    public static final int BASE_RETORNO_PASILLO1_Y = 630;

    public static final int BASE_RETORNO_PASILLO2_X = 1100;
   public static final int BASE_RETORNO_PASILLO2_Y = 630;

    // Variables para controlar interacción con puertas
    private boolean estaEnPuerta1 = false;
    private boolean estaEnPuerta2 = false;
    private boolean estaEnPuertaPasillo1 = false;
    private boolean estaEnPuertaPasillo2 = false;
    private JLabel mensajeLabel;
    private javax.swing.Timer mensajeTimer;
    private JFrame parentFrame;

    // 🔹 CONSTRUCTOR
    public CasaPrincipal(JFrame parentFrame) {
        this(parentFrame, BASE_PLAYER_X, BASE_PLAYER_Y);
    }

    public CasaPrincipal(JFrame parentFrame, int baseX, int baseY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 🔹 FORZAR EL FOCO
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        fondo = new ImageIcon("src/resources/images/casaPrincipal.png").getImage();
        colisiones = new colisiones("src/resources/images/casaPrincipalColisiones.png");

        // 🔹 SISTEMA DE SPAWN RANDOM
        // Crear jugador en posición fija o en coordenadas dadas (no usar spawn random para jugador)
        int startX, startY;
        if (baseX >= 0 && baseY >= 0) {
            double scaleX = (double) escalaManager.getAnchoActual() / BASE_WIDTH;
            double scaleY = (double) escalaManager.getAltoActual() / BASE_HEIGHT;
            startX = (int) Math.round(baseX * scaleX);
            startY = (int) Math.round(baseY * scaleY);
        } else {
            startX = escalaManager.escalaX(BASE_PLAYER_X);
            startY = escalaManager.escalaY(BASE_PLAYER_Y);
        }
        player = new jugador(startX, startY);
        
        // Spawn de objetos aleatorios (solo objetos)
        spawnObjetosAleatorios();
        
        // Configurar mensaje para interacciones
        mensajeLabel = new JLabel("", JLabel.CENTER);
        mensajeLabel.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        mensajeLabel.setForeground(Color.YELLOW);
        mensajeLabel.setBackground(new Color(0, 0, 0, 180));
        mensajeLabel.setOpaque(true);
        mensajeLabel.setVisible(false);
        add(mensajeLabel);

        mensajeTimer = new javax.swing.Timer(2000, e -> {
            mensajeLabel.setVisible(false);
            ((javax.swing.Timer)e.getSource()).stop();
        });
        mensajeTimer.setRepeats(false);

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        gameLoop = new javax.swing.Timer(16, e -> {
            int oldX = player.getX();
            int oldY = player.getY();

            if (upPressed)    player.moveUp();
            if (downPressed)  player.moveDown();
            if (leftPressed)  player.moveLeft();
            if (rightPressed) player.moveRight();

            // 🔹 VERIFICAR COLISIÓN
            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }

            // 🔹 LIMITAR A LA VENTANA ACTUAL
            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
            // 🔹 ACTUALIZAR POSICIÓN DEL MENSAJE Y VERIFICAR PUERTAS
            actualizarPosicionLabels();
            verificarPosicionPuertas();
            
            // 🔹 VERIFICAR SI TOCA LA PARTE INFERIOR
            verificarSalidaInferior();
            
            repaint();
        });
        gameLoop.start();
        // iniciar timer para comprobar pickups
        pickupTimer = new javax.swing.Timer(120, ev -> checkPickups());
        pickupTimer.start();

        // Mostrar mensaje al entrar a la casa principal solo la primera vez
        actualizarPosicionLabels();
        if (!EstadoJuego.isMensajeCasaPrincipalMostrado()) {
            mostrarMensaje("genial pude entrar, ahora a investigar");
            EstadoJuego.setMensajeCasaPrincipalMostrado(true);
        }
         
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

    private void verificarPosicionPuertas() {
        Rectangle jugadorBounds = player.getBounds();
        
        // 🔹 ESCALAR ÁREAS DE LAS PUERTAS
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;
        
        // Puertas habitaciones
        int puerta1X = (int) Math.round(BASE_PUERTA1_X * scaleX);
        int puerta1Y = (int) Math.round(BASE_PUERTA1_Y * scaleY);
        int puerta1W = (int) Math.round(BASE_PUERTA1_W * scaleX);
        int puerta1H = (int) Math.round(BASE_PUERTA1_H * scaleY);
        
        int puerta2X = (int) Math.round(BASE_PUERTA2_X * scaleX);
        int puerta2Y = (int) Math.round(BASE_PUERTA2_Y * scaleY);
        int puerta2W = (int) Math.round(BASE_PUERTA2_W * scaleX);
        int puerta2H = (int) Math.round(BASE_PUERTA2_H * scaleY);

        // Puertas pasillos
        int puertaPasillo1X = (int) Math.round(BASE_PUERTA_PASILLO1_X * scaleX);
        int puertaPasillo1Y = (int) Math.round(BASE_PUERTA_PASILLO1_Y * scaleY);
        int puertaPasillo1W = (int) Math.round(BASE_PUERTA_PASILLO1_W * scaleX);
        int puertaPasillo1H = (int) Math.round(BASE_PUERTA_PASILLO1_H * scaleY);

        int puertaPasillo2X = (int) Math.round(BASE_PUERTA_PASILLO2_X * scaleX);
        int puertaPasillo2Y = (int) Math.round(BASE_PUERTA_PASILLO2_Y * scaleY);
        int puertaPasillo2W = (int) Math.round(BASE_PUERTA_PASILLO2_W * scaleX);
        int puertaPasillo2H = (int) Math.round(BASE_PUERTA_PASILLO2_H * scaleY);

        Rectangle puerta1Bounds = new Rectangle(puerta1X, puerta1Y, puerta1W, puerta1H);
        Rectangle puerta2Bounds = new Rectangle(puerta2X, puerta2Y, puerta2W, puerta2H);
        Rectangle puertaPasillo1Bounds = new Rectangle(puertaPasillo1X, puertaPasillo1Y, puertaPasillo1W, puertaPasillo1H);
        Rectangle puertaPasillo2Bounds = new Rectangle(puertaPasillo2X, puertaPasillo2Y, puertaPasillo2W, puertaPasillo2H);
        
        estaEnPuerta1 = jugadorBounds.intersects(puerta1Bounds);
        estaEnPuerta2 = jugadorBounds.intersects(puerta2Bounds);
        estaEnPuertaPasillo1 = jugadorBounds.intersects(puertaPasillo1Bounds);
        estaEnPuertaPasillo2 = jugadorBounds.intersects(puertaPasillo2Bounds);
        
        // Mostrar mensaje cuando está cerca de cualquier puerta
        if (estaEnPuerta1 || estaEnPuerta2 || estaEnPuertaPasillo1 || estaEnPuertaPasillo2) {
            mostrarMensaje("Presiona E para entrar");
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (!mensajeLabel.isVisible()) {
            mensajeLabel.setText(mensaje);
            mensajeLabel.setVisible(true);
            mensajeTimer.start();
        }
    }

    // 🔹 NUEVO MÉTODO: Volver a la calle
    private void volverACalle() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        // 🔹 Aparecer en posición centrada en la calle
        calle callePanel = new calle(641, 500);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(callePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(callePanel::requestFocusInWindow);
    }

    private void cambiarAHabitacion1() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        habitacion1 siguientePanel = new habitacion1(parentFrame);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void cambiarAHabitacion2() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        habitacion2 siguientePanel = new habitacion2(parentFrame);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void cambiarAPasillo1() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        pasillo1 siguientePanel = new pasillo1(parentFrame);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    private void cambiarAPasillo2() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        pasillo2 siguientePanel = new pasillo2(parentFrame);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(siguientePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(siguientePanel::requestFocusInWindow);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 🔹 DIBUJAR FONDO ESCALADO A LA VENTANA ACTUAL
        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        // 🔹 DIBUJAR JUGADOR
        player.draw(g);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed = true;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed = true;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed = true;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = true;
            case KeyEvent.VK_E -> {
                // Priorizar recoger objetos cercanos
                if (nearbyObject != null) {
                    collectNearbyObject();
                    break;
                }
                if (estaEnPuerta1) {
                    cambiarAHabitacion1();
                } else if (estaEnPuerta2) {
                    cambiarAHabitacion2();
                } else if (estaEnPuertaPasillo1) {
                    cambiarAPasillo1();
                } else if (estaEnPuertaPasillo2) {
                    cambiarAPasillo2();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W, KeyEvent.VK_UP    -> upPressed = false;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN  -> downPressed = false;
            case KeyEvent.VK_A, KeyEvent.VK_LEFT  -> leftPressed = false;
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // Método para spawnear objetos aleatoriamente en CasaPrincipal
     private void spawnObjetosAleatorios() {
        String scene = "casaprincipal";
        String[] objetos = new String[]{
            "Bandana de Capuchino Assasino.png",
            "Cascara de Chimpanzini Bananini.png",
            "Palo de Tung Tung.png",
            "Rueda de Boneca Ambalabu.png",
            "zapa.png"
        };

        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list.isEmpty()) {
            // Solo un objeto por sección: intentar obtener una posición segura para un objeto
            int[] pos = SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos == null) {
                pos = SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
            }
            if (pos == null) {
                java.awt.Rectangle[] zonas = SistemaSpawnJuego.obtenerZonasSpawnParaObjetos(scene);
                if (zonas != null && zonas.length > 0) {
                    java.awt.Rectangle z = zonas[0];
                    pos = new int[]{z.x + z.width/2, z.y + z.height/2};
                }
            }
            if (pos != null) {
                int idx = (int) (Math.random() * objetos.length);
                list.add(new EstadoJuego.SpawnedObject(objetos[idx], pos[0], pos[1]));
            }
             EstadoJuego.setSpawnedObjects(scene, list);
         }

        // Agrandar un poco las imágenes de los objetos
        int size = escalaManager.escalaUniforme(64);
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            if (objetosLabels.containsKey(so)) continue;
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

    private void checkPickups() {
        if (player == null) return;
        Rectangle jugadorBounds = player.getBounds();
        String scene = "casaprincipal";
        nearbyObject = null;
        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            Rectangle objBounds = new Rectangle(lbl.getX(), lbl.getY(), lbl.getWidth(), lbl.getHeight());
            if (jugadorBounds.intersects(objBounds)) {
                nearbyObject = so;
                mostrarMensajeDuracion("Presiona E para recoger", 1000);
                break;
            }
        }
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        String scene = "casaprincipal";
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        nearbyObject = null;
        this.revalidate();
        this.repaint();
        mostrarMensajeDuracion("Evidencia recogida", 1200);
        System.out.println("Evidencia recogida: total=" + EstadoJuego.getObjetosRecogidos());
    }

    // Mostrar un mensaje temporal (ms) usando el mensajeLabel existente
    private void mostrarMensajeDuracion(String mensaje, int ms) {
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            mensajeTimer.stop();
        }
        mensajeLabel.setText(mensaje);
        mensajeLabel.setVisible(true);
        javax.swing.Timer tempTimer = new javax.swing.Timer(ms, e -> {
            mensajeLabel.setVisible(false);
            ((javax.swing.Timer)e.getSource()).stop();
        });
        tempTimer.setRepeats(false);
        tempTimer.start();
    }

}