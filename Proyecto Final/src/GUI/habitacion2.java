package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class habitacion2 extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    
    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new java.util.HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;
    
    // 🔹 VARIABLES PARA SISTEMA DE COLISIONES
    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;
    // Mensaje temporal / indicador (misma estética que en comedor/CasaPrincipal)
    private JLabel mensajeLabel;
    private Timer mensajeTimer = null;
    
    // 🔹 RESOLUCIÓN BASE
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    // 🔹 POSICIÓN INICIAL BASE DEL JUGADOR
    private static final int BASE_PLAYER_X = 650;
    private static final int BASE_PLAYER_Y = 680;

    public habitacion2(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // 🔹 CONFIGURAR KEY BINDINGS
        setupKeyBindings();

        fondo = new ImageIcon("src/resources/images/habitacion2 REWORK.png").getImage();
        
        // 🔹 CARGAR MÁSCARA DE COLISIONES
        colisiones = new colisiones("src/resources/images/habitacion2 Mascara arreglado.png");

        // 🔹 CREAR JUGADOR CON POSICIÓN ESCALADA
        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);

        // Spawn de objetos en esta sección (garantizar 1 objeto)
        spawnObjetosAleatorios();
        
        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        // 🔹 DEBUG LABEL (opcional)
        if (DEBUG) {
            debugLabel = new JLabel("", JLabel.LEFT);
            debugLabel.setForeground(Color.WHITE);
            debugLabel.setBackground(new Color(0, 0, 0, 120));
            debugLabel.setOpaque(true);
            debugLabel.setBounds(10, 10, 400, 20);
            add(debugLabel);
        }

        // Inicializar label de mensaje temporal (misma estética usada en otros paneles)
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
            
            // 🔹 DEBUG INFO
            if (DEBUG && debugLabel != null) {
                Rectangle p = player.getBounds();
                debugLabel.setText("Posición: (" + p.x + ", " + p.y + ") - Colisiones: " + 
                                 (!ignoreCollisions ? "ON" : "OFF"));
            }

            // comprobar recogidas cada frame
            checkPickups();
            
            verificarSalidaInferior();

            repaint();
        });
        gameLoop.start();
    }
    
    // Spawn obligatorio (1 objeto por habitación)
    private void spawnObjetosAleatorios() {
        String scene = "habitacion2";
        // Coordenadas fijas solicitadas
        final int FIXED_X = 790;
        final int FIXED_Y = 225;

        // Obtener la evidencia exclusiva asignada para esta escena
        String evidenciaAsignada = EstadoJuego.getOrAssignUniqueEvidenceForScene(scene);
        
        String[] objetos = new String[]{ evidenciaAsignada };

        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list.isEmpty()) {
            // Forzamos la posición fija para esta escena
            int[] pos = new int[]{FIXED_X, FIXED_Y};
            list.add(new EstadoJuego.SpawnedObject(evidenciaAsignada, pos[0], pos[1]));
            EstadoJuego.setSpawnedObjects(scene, list);
        } else {
            // Si ya hay objetos en la lista, actualizamos su posición a la fija
            boolean changed = false;
            for (EstadoJuego.SpawnedObject so : list) {
                if (!so.recogido) {
                    if (so.x != FIXED_X || so.y != FIXED_Y) {
                        so.x = FIXED_X;
                        so.y = FIXED_Y;
                        changed = true;
                    }
                }
            }
            if (changed) EstadoJuego.setSpawnedObjects(scene, list);
        }

        // Crear labels
        for (JLabel lbl : objetosLabels.values()) this.remove(lbl);
        objetosLabels.clear();
        int size = escalaManager.escalaUniforme(64);
        for (EstadoJuego.SpawnedObject so : EstadoJuego.getSpawnedObjects(scene)) {
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

                // Tooltip con coordenadas base (para ver rápidamente dónde está)
                itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");

                this.add(itemLabel);

                // Permitir editar posición con clic derecho y ver posición con clic izquierdo
                itemLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(java.awt.event.MouseEvent me) {
                        if (javax.swing.SwingUtilities.isRightMouseButton(me)) {
                            // Mostrar diálogo para editar coordenadas base (no escaladas)
                            JTextField fx = new JTextField(Integer.toString(so.x));
                            JTextField fy = new JTextField(Integer.toString(so.y));
                            JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
                            p.add(new JLabel("X:")); p.add(fx);
                            p.add(new JLabel("Y:")); p.add(fy);

                            int res = JOptionPane.showConfirmDialog(habitacion2.this, p,
                                    "Editar posición (" + so.nombre + ")",
                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                            if (res == JOptionPane.OK_OPTION) {
                                try {
                                    int nx = Integer.parseInt(fx.getText().trim());
                                    int ny = Integer.parseInt(fy.getText().trim());
                                    // actualizar datos base
                                    so.x = nx;
                                    so.y = ny;
                                    // recalcular posición escalada y mover label
                                    int nxScaled = escalaManager.escalaX(so.x) - size/2;
                                    int nyScaled = escalaManager.escalaY(so.y) - size/2;
                                    itemLabel.setBounds(nxScaled, nyScaled, size, size);
                                    itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");
                                    // guardar cambios en el EstadoJuego (persistencia en memoria)
                                    EstadoJuego.setSpawnedObjects(scene, EstadoJuego.getSpawnedObjects(scene));
                                    repaint();
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(habitacion2.this,
                                            "Valores inválidos. Usa enteros.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (javax.swing.SwingUtilities.isLeftMouseButton(me)) {
                            // Mostrar un diálogo simple con la posición actual (base)
                            JOptionPane.showMessageDialog(habitacion2.this,
                                    "Posición de " + so.nombre + ": (" + so.x + ", " + so.y + ")",
                                    "Posición objeto", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });

                objetosLabels.put(so, itemLabel);
            } catch (Exception ex) {
                System.out.println("No se pudo cargar objeto: " + so.nombre + " -> " + ex.getMessage());
            }
        }
    }
    
    private void checkPickups() {
        if (player == null) return;
        Rectangle playerBounds = player.getBounds();
        String scene = "habitacion2";
        nearbyObject = null;
        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            if (playerBounds.intersects(lbl.getBounds())) {
                nearbyObject = so;
                // Mostrar mensaje cuando hay un objeto cercano
                mostrarMensajeDuracion("E para recoger", 1000);
                break;
            }
        }
    }
    
    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        int objetosAntes = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos antes de recoger: " + objetosAntes);
        
        String scene = "habitacion2"; 
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
            mostrarMensajeDuracion("Tendre que hablar con Brr brr patapim", 4000);
            
            EstadoJuego.setPuedeMostrarDialogosEspeciales(true);
            
        } else if (EstadoJuego.todasLasEvidenciasRecolectadas()) {
            mostrarMensajeDuracion("Evidencia recogida", 1200);
        } else {
            mostrarMensajeDuracion("Evidencia recogida", 1200);
        }
    }

    private void verificarSalidaInferior() {
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = escalaManager.getAltoActual();

        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - 5) {
            volverACasaPrincipal();
        }
    }

    private void volverACasaPrincipal() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();

        // 🔹 PASAR LA POSICIÓN CORRECTA SEGÚN LA HABITACIÓN/PASILLO
        CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame,
            CasaPrincipal.BASE_RETORNO_PUERTA2_X,
            CasaPrincipal.BASE_RETORNO_PUERTA2_Y);

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
        
        // Posicionar mensaje temporal (centrado, igual estética que otros paneles)
        if (mensajeLabel != null) {
            int msgW = escalaManager.escalaAncho(400);
            int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
            int msgY = escalaManager.escalaY(100);
            int msgH = escalaManager.escalaAlto(60);
            mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
        }
        // Mostrar indicador de recogida mientras haya un objeto cercano
        if (nearbyObject != null) {
            mensajeLabel.setText("E para recoger");
            mensajeLabel.setVisible(true);
        } else {
            if (mensajeLabel != null) mensajeLabel.setVisible(false);
        }
    }

    // Mostrar un mensaje temporal en el panel con la misma estética y posición que en CasaPrincipal/calle
    private void mostrarMensajeDuracion(String mensaje, int ms) {
        if (mensajeLabel == null) return;
        
        // Detener timer anterior
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            mensajeTimer.stop();
        }
        
        mensajeLabel.setText(mensaje);
        
        int msgW = escalaManager.escalaAncho(400);
        int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
        int msgY = escalaManager.escalaY(100);
        int msgH = escalaManager.escalaAlto(60);
        mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
        
        mensajeLabel.setVisible(true);
        
        setComponentZOrder(mensajeLabel, 0);
        
        Timer tempTimer = new Timer(ms, e -> {
            mensajeLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        });
        tempTimer.setRepeats(false);
        tempTimer.start();
        
        mensajeTimer = tempTimer;
        
        revalidate();
        repaint();
        
        System.out.println("Mostrando mensaje en habitacion2: '" + mensaje + "' por " + ms + "ms");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)    upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)  downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)  leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_E) {
            if (nearbyObject != null) collectNearbyObject();
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

        // 🔹 TECLA 'P' PARA IMPRIMIR POSICIONES DE SPAWN DE ESTA ESCENA (útil para ajustar)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "print.positions");
        am.put("print.positions", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects("habitacion2");
                System.out.println("== Spawn positions for habitacion2 ==");
                for (EstadoJuego.SpawnedObject so : list) {
                    System.out.println(so.nombre + " -> (" + so.x + "," + so.y + ") collected=" + so.recogido);
                }
            }
        });
    }
}