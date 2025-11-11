package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CasaPrincipal extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Image imagenCofre;
    private javax.swing.Timer gameLoop;
    private colisiones colisiones;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new java.util.HashMap<>();
    private javax.swing.Timer pickupTimer;
    private EstadoJuego.SpawnedObject nearbyObject = null;

    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosCoordLabels = new java.util.HashMap<>();
    private boolean modoEdicionObjetos = false;

    private static final String OBJETO_FIJO_CASAPRINCIPAL = "Rueda de Boneca Ambalabu.png";

    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    private static final int BASE_PLAYER_X = 590;
    private static final int BASE_PLAYER_Y = 140;
    
    private static final int BASE_PUERTA1_X = 270;
    private static final int BASE_PUERTA1_Y = 100;
    private static final int BASE_PUERTA1_W = 80;
    private static final int BASE_PUERTA1_H = 120;
    
    private static final int BASE_PUERTA2_X = 1120;
    private static final int BASE_PUERTA2_Y = 110;
    private static final int BASE_PUERTA2_W = 80;
    private static final int BASE_PUERTA2_H = 120;

    private static final int BASE_PUERTA_PASILLO1_X = 270;
    private static final int BASE_PUERTA_PASILLO1_Y = 580;
    private static final int BASE_PUERTA_PASILLO1_W = 80;
    private static final int BASE_PUERTA_PASILLO1_H = 120;

    private static final int BASE_PUERTA_PASILLO2_X = 1100;
    private static final int BASE_PUERTA_PASILLO2_Y = 590;
    private static final int BASE_PUERTA_PASILLO2_W = 80;
    private static final int BASE_PUERTA_PASILLO2_H = 120;
   
    private static final int BASE_COFRE_X = 820;
    private static final int BASE_COFRE_Y = 100;
    private static final int BASE_COFRE_W = 100;
    private static final int BASE_COFRE_H = 100;
   
    public static final int BASE_RETORNO_PUERTA1_X = 280;
    public static final int BASE_RETORNO_PUERTA1_Y = 160;

    public static final int BASE_RETORNO_PUERTA2_X = 1120;
    public static final int BASE_RETORNO_PUERTA2_Y = 160;

    public static final int BASE_RETORNO_PASILLO1_X = 270;
    public static final int BASE_RETORNO_PASILLO1_Y = 630;

    public static final int BASE_RETORNO_PASILLO2_X = 1100;
    public static final int BASE_RETORNO_PASILLO2_Y = 630;

    private boolean estaEnPuerta1 = false;
    private boolean estaEnPuerta2 = false;
    private boolean estaEnPuertaPasillo1 = false;
    private boolean estaEnPuertaPasillo2 = false;
    private boolean estaEnCofre = false;
    private boolean cofreAbierto = false;
    
    private JLabel mensajeLabel;
    private javax.swing.Timer mensajeTimer;
    private JFrame parentFrame;

    private boolean prevEstaEnPuerta1 = false;
    private boolean prevEstaEnPuerta2 = false;
    private boolean prevEstaEnPuertaPasillo1 = false;
    private boolean prevEstaEnPuertaPasillo2 = false;
    private boolean prevNearby = false;

    public CasaPrincipal(JFrame parentFrame) {
        this(parentFrame, BASE_PLAYER_X, BASE_PLAYER_Y);
    }

    public CasaPrincipal(JFrame parentFrame, int baseX, int baseY) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        fondo = new ImageIcon("src/resources/images/casaPrincipal REWORK.png").getImage();
        colisiones = new colisiones("src/resources/images/casaPrincipalMascara REWORK.png");

        // Cargar imagen del cofre según su estado
        if (EstadoJuego.isCofreCasaPrincipalAbierto()) {
            imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            cofreAbierto = true;
        } else {
            imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
        }

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
        
        spawnObjetosAleatorios();
        
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

            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }

            Rectangle bounds = new Rectangle(0, 0, 
                    escalaManager.getAnchoActual(), 
                    escalaManager.getAltoActual());
            player.clampTo(bounds);
            
            actualizarPosicionLabels();
            verificarPosicionPuertas();
            verificarPosicionCofre();
            verificarSalidaInferior();
            actualizarIndicadorProximidad();
            
            repaint();
        });
        gameLoop.start();
        
        pickupTimer = new javax.swing.Timer(120, ev -> checkPickups());
        pickupTimer.start();

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
    }

    private void actualizarIndicadorProximidad() {
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            return;
        }

        if (estaEnCofre && !cofreAbierto) {
            mensajeLabel.setText("E para abrir");
            mensajeLabel.setVisible(true);
            return;
        }

        if (nearbyObject != null) {
            mensajeLabel.setText("Presiona E para recoger");
            mensajeLabel.setVisible(true);
            return;
        }

        if (estaEnPuerta1) {
            if (EstadoJuego.isCofreComedorAbierto()) {
                mensajeLabel.setText("Presiona E para entrar");
            } else {
                mensajeLabel.setText("Necesito una llave");
            }
            mensajeLabel.setVisible(true);
            return;
        }

        if (estaEnPuerta2) {
            if (EstadoJuego.isCofreHabitacion1Abierto()) {
                mensajeLabel.setText("Presiona E para entrar");
            } else {
                mensajeLabel.setText("Necesito una llave");
            }
            mensajeLabel.setVisible(true);
            return;
        }

        if (estaEnPuertaPasillo1 || estaEnPuertaPasillo2) {
            if (EstadoJuego.isCofreCasaPrincipalAbierto()) {
                mensajeLabel.setText("Presiona E para entrar");
            } else {
                mensajeLabel.setText("Necesito una llave");
            }
            mensajeLabel.setVisible(true);
            return;
        }

        mensajeLabel.setVisible(false);
    }

    private void verificarPosicionPuertas() {
        Rectangle jugadorBounds = player.getBounds();
        
        double scaleX = (double) getWidth() / BASE_WIDTH;
        double scaleY = (double) getHeight() / BASE_HEIGHT;
        
        int puerta1X = (int) Math.round(BASE_PUERTA1_X * scaleX);
        int puerta1Y = (int) Math.round(BASE_PUERTA1_Y * scaleY);
        int puerta1W = (int) Math.round(BASE_PUERTA1_W * scaleX);
        int puerta1H = (int) Math.round(BASE_PUERTA1_H * scaleY);
        
        int puerta2X = (int) Math.round(BASE_PUERTA2_X * scaleX);
        int puerta2Y = (int) Math.round(BASE_PUERTA2_Y * scaleY);
        int puerta2W = (int) Math.round(BASE_PUERTA2_W * scaleX);
        int puerta2H = (int) Math.round(BASE_PUERTA2_H * scaleY);

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
        
        prevEstaEnPuerta1 = estaEnPuerta1;
        prevEstaEnPuerta2 = estaEnPuerta2;
        prevEstaEnPuertaPasillo1 = estaEnPuertaPasillo1;
        prevEstaEnPuertaPasillo2 = estaEnPuertaPasillo2;
    }

    private void abrirCofre() {
        if (!cofreAbierto) {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreCasaPrincipalAbierto.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
                }
            } catch (Exception e) {
                System.out.println("Error cargando imagen del cofre abierto: " + e.getMessage());
                imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            }
            
            cofreAbierto = true;
            EstadoJuego.setCofreCasaPrincipalAbierto(true);
            
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.stop();
            }
            mensajeLabel.setText("Encontre una llave");
            mensajeLabel.setVisible(true);
            if (mensajeTimer != null) {
                mensajeTimer.setInitialDelay(2000);
                mensajeTimer.restart();
            }
            
            repaint();
            System.out.println("Cofre abierto - Llave encontrada");
        }
    }

    private void mostrarMensaje(String mensaje) {
        if (mensajeLabel.isVisible() && mensaje.equals(mensajeLabel.getText())) {
            return;
        }
        mensajeLabel.setText(mensaje);
        posicionarMensajeSobreObjeto();
        if (!mensajeLabel.isVisible()) {
            mensajeLabel.setVisible(true);
            mensajeTimer.start();
        } else {
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.restart();
            }
        }
    }

    private void volverACalle() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (mensajeTimer != null && mensajeTimer.isRunning()) mensajeTimer.stop();
        
        calle callePanel = new calle(641, 500);
        
        parentFrame.getContentPane().removeAll();
        parentFrame.getContentPane().add(callePanel);
        parentFrame.revalidate();
        parentFrame.repaint();
        
        SwingUtilities.invokeLater(callePanel::requestFocusInWindow);
    }

    private void cambiarAHabitacion1() {
        if (!EstadoJuego.isCofreComedorAbierto()) {
            System.out.println("Intento acceder a Habitación 1 sin llave del comedor");
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.stop();
            }
            mensajeLabel.setText("Necesito una llave");
            mensajeLabel.setVisible(true);
            if (mensajeTimer != null) {
                mensajeTimer.setInitialDelay(2000);
                mensajeTimer.restart();
            }
            return;
        }
        
        System.out.println("Accediendo a Habitación 1 con llave del comedor");
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
        if (!EstadoJuego.isCofreHabitacion1Abierto()) {
            System.out.println("Intento acceder a Habitación 2 sin llave de habitación 1");
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.stop();
            }
            mensajeLabel.setText("Necesito una llave");
            mensajeLabel.setVisible(true);
            if (mensajeTimer != null) {
                mensajeTimer.setInitialDelay(2000);
                mensajeTimer.restart();
            }
            return;
        }
        
        System.out.println("Accediendo a Habitación 2 con llave de habitación 1");
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
        if (!EstadoJuego.isCofreCasaPrincipalAbierto()) {
            System.out.println("Intento acceder a Pasillo 1 sin llave de casa principal");
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.stop();
            }
            mensajeLabel.setText("Necesito una llave");
            mensajeLabel.setVisible(true);
            if (mensajeTimer != null) {
                mensajeTimer.setInitialDelay(2000);
                mensajeTimer.restart();
            }
            return;
        }
        
        System.out.println("Accediendo a Pasillo 1 con llave de casa principal");
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
        if (!EstadoJuego.isCofreCasaPrincipalAbierto()) {
            System.out.println("Intento acceder a Pasillo 2 sin llave de casa principal");
            if (mensajeTimer != null && mensajeTimer.isRunning()) {
                mensajeTimer.stop();
            }
            mensajeLabel.setText("Necesito una llave");
            mensajeLabel.setVisible(true);
            if (mensajeTimer != null) {
                mensajeTimer.setInitialDelay(2000);
                mensajeTimer.restart();
            }
            return;
        }
        
        System.out.println("Accediendo a Pasillo 2 con llave de casa principal");
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

        g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        int cofreX = escalaManager.escalaX(BASE_COFRE_X);
        int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
        int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
        int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
        g.drawImage(imagenCofre, cofreX, cofreY, cofreW, cofreH, this);
        
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
                if (estaEnCofre && !cofreAbierto) {
                    abrirCofre();
                } else if (nearbyObject != null) {
                    collectNearbyObject();
                } else if (estaEnPuerta1) {
                    cambiarAHabitacion1();
                } else if (estaEnPuerta2) {
                    cambiarAHabitacion2();
                } else if (estaEnPuertaPasillo1) {
                    cambiarAPasillo1();
                } else if (estaEnPuertaPasillo2) {
                    cambiarAPasillo2();
                }
            }
            case KeyEvent.VK_P -> {
                modoEdicionObjetos = !modoEdicionObjetos;
                for (JLabel l : objetosCoordLabels.values()) {
                    l.setVisible(modoEdicionObjetos);
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

    private void spawnObjetosAleatorios() {
        String scene = "casaprincipal";
        String[] objetos = new String[]{
            "Bandana de Capuchino Assasino.png",
            "Cascara de Chimpanzini Bananini.png",
            "Palo de Tung Tung.png",
            "Rueda de Boneca Ambalabu.png",
            "zapa.png"
        };

        java.util.List<EstadoJuego.SpawnedObject> tmp = EstadoJuego.getSpawnedObjects(scene);
        if (tmp == null) {
            tmp = new java.util.ArrayList<>();
        }
        final java.util.List<EstadoJuego.SpawnedObject> list = tmp;
        boolean found = false;
        java.util.Iterator<EstadoJuego.SpawnedObject> it = list.iterator();
        while (it.hasNext()) {
            EstadoJuego.SpawnedObject so = it.next();
            if (!OBJETO_FIJO_CASAPRINCIPAL.equals(so.nombre)) {
                it.remove();
            } else {
                so.nombre = OBJETO_FIJO_CASAPRINCIPAL;
                so.x = BASE_PLAYER_X;
                so.y = BASE_PLAYER_Y;
                found = true;
            }
        }
        if (!found) {
            list.clear();
            list.add(new EstadoJuego.SpawnedObject(OBJETO_FIJO_CASAPRINCIPAL, BASE_PLAYER_X, BASE_PLAYER_Y));
        }
        EstadoJuego.setSpawnedObjects(scene, list);

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

                itemLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (!modoEdicionObjetos) return;
                        String current = so.x + "," + so.y;
                        String input = JOptionPane.showInputDialog(CasaPrincipal.this, "Editar coordenadas X,Y:", current);
                        if (input == null) return;
                        input = input.trim();
                        String[] parts = input.split(",");
                        if (parts.length != 2) {
                            JOptionPane.showMessageDialog(CasaPrincipal.this, "Formato inválido. Use: X,Y", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            int newX = Integer.parseInt(parts[0].trim());
                            int newY = Integer.parseInt(parts[1].trim());
                            so.x = newX;
                            so.y = newY;
                            int nx = escalaManager.escalaX(so.x) - size/2;
                            int ny = escalaManager.escalaY(so.y) - size/2;
                            itemLabel.setBounds(nx, ny, size, size);
                            JLabel coord = objetosCoordLabels.get(so);
                            if (coord != null) {
                                coord.setText(so.x + "," + so.y);
                                coord.setBounds(nx, ny - 16, 100, 16);
                            }
                            EstadoJuego.setSpawnedObjects(scene, list);
                            revalidate();
                            repaint();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(CasaPrincipal.this, "Valores inválidos. Deben ser enteros.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });

                this.add(itemLabel);
                objetosLabels.put(so, itemLabel);

                JLabel coordLabel = new JLabel(so.x + "," + so.y);
                coordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                coordLabel.setForeground(Color.WHITE);
                coordLabel.setBackground(new Color(0,0,0,160));
                coordLabel.setOpaque(true);
                coordLabel.setVisible(modoEdicionObjetos);
                coordLabel.setBounds(x, y - 16, 100, 16);
                this.add(coordLabel);
                objetosCoordLabels.put(so, coordLabel);

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
        boolean nowNearby = false;
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            Rectangle objBounds = new Rectangle(lbl.getX(), lbl.getY(), lbl.getWidth(), lbl.getHeight());
            if (jugadorBounds.intersects(objBounds)) {
                nearbyObject = so;
                nowNearby = true;
                break;
            }
        }
        prevNearby = nowNearby;
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        int objetosAntes = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos antes de recoger: " + objetosAntes);
        
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
        
        int objetosDespues = EstadoJuego.getObjetosRecogidos();
        System.out.println("Evidencia recogida: total=" + objetosDespues);
        
        if (objetosAntes == 4 && objetosDespues == 5) {
            mostrarMensajeDuracion("Tendre que hablar con Brr brr patapim", 4000);
            EstadoJuego.setPuedeMostrarDialogosEspeciales(true);
        } else {
            mostrarMensajeDuracion("Evidencia recogida", 1200);
        }
    }

    private void mostrarMensajeDuracion(String mensaje, int ms) {
        if (mensajeTimer == null) {
            mensajeTimer = new javax.swing.Timer(ms, e -> {
                mensajeLabel.setVisible(false);
                ((javax.swing.Timer)e.getSource()).stop();
            });
            mensajeTimer.setRepeats(false);
        }

        if (mensajeLabel.isVisible() && mensaje.equals(mensajeLabel.getText())) {
            posicionarMensajeSobreObjeto();
            mensajeTimer.stop();
            mensajeTimer.setInitialDelay(ms);
            mensajeTimer.restart();
            return;
        }

        mensajeLabel.setText(mensaje);
        posicionarMensajeSobreObjeto();
        if (!mensajeLabel.isVisible()) mensajeLabel.setVisible(true);
        mensajeTimer.stop();
        mensajeTimer.setInitialDelay(ms);
        mensajeTimer.restart();
    }

    private void posicionarMensajeSobreObjeto() {
        SwingUtilities.invokeLater(() -> {
            JLabel target = null;
            for (JLabel l : objetosLabels.values()) {
                if (l != null && l.isShowing()) {
                    target = l;
                    break;
                }
            }
            if (target == null) {
                int msgW = escalaManager.escalaAncho(400);
                int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
                int msgY = escalaManager.escalaY(100);
                int msgH = escalaManager.escalaAlto(60);
                mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
            } else {
                int lblW = target.getWidth();
                int lblH = target.getHeight();
                int desiredW = Math.max(escalaManager.escalaAncho(200), lblW * 2);
                int desiredH = escalaManager.escalaAlto(40);
                int lx = target.getX();
                int ly = target.getY();
                int mx = lx + (lblW - desiredW) / 2;
                int my = ly - desiredH - escalaManager.escalaAlto(8);
                if (my < 0) my = ly + lblH + escalaManager.escalaAlto(8);
                mensajeLabel.setBounds(mx, my, desiredW, desiredH);
            }
            if (mensajeLabel.getParent() == this) {
                setComponentZOrder(mensajeLabel, 0);
            }
            revalidate();
            repaint();
        });
    }
}