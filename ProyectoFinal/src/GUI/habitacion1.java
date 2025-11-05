package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

public class habitacion1 extends JPanel implements KeyListener {

    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean estaEnSalida = false;
    private JFrame parentFrame;
    private colisiones colisiones;
    private Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;

    // Variables para sistema de colisiones
    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;

    // Mensaje temporal / indicador (misma estética que en comedor/CasaPrincipal)
    private JLabel mensajeLabel;
    private Timer mensajeTimer = null;

    // Posicion inicial base del jugador
    private static final int BASE_PLAYER_X = 650;
    private static final int BASE_PLAYER_Y = 680;

    public habitacion1(JFrame parentFrame) {
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

        // Configurar key bindings
        setupKeyBindings();

        // Cargar fondo con manejo de errores
        try {
            fondo = new ImageIcon("src/resources/images/habitacion1 REWORK.png").getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar fondo: " + e.getMessage());
            fondo = crearFondoPorDefecto();
        }

        // Cargar mascara de colisiones con manejo de errores
        try {
            colisiones = new colisiones("src/resources/images/habitacion1Mascara.png");
        } catch (Exception e) {
            System.out.println("Error al cargar colisiones: " + e.getMessage());
        }

        // Crear jugador con posicion escalada
        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);

        // Spawn de objetos en esta seccion (garantizar 1 objeto)
        spawnObjetosAleatorios();

        addKeyListener(this);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());

        // Debug label opcional
        if (DEBUG) {
            debugLabel = new JLabel("", JLabel.LEFT);
            debugLabel.setForeground(Color.WHITE);
            debugLabel.setBackground(new Color(0, 0, 0, 120));
            debugLabel.setOpaque(true);
            debugLabel.setBounds(10, 10, 400, 20);
            add(debugLabel);
        }

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
        // Posicionar mensaje temporal igual que en comedor/CasaPrincipal
        if (mensajeLabel != null) {
            int msgW = escalaManager.escalaAncho(400);
            int msgX = (escalaManager.getAnchoActual() - msgW) / 2;
            int msgY = escalaManager.escalaY(100);
            int msgH = escalaManager.escalaAlto(60);
            mensajeLabel.setBounds(msgX, msgY, msgW, msgH);
        }
    }
    
    private void updateGame() {
        // Guardar posicion anterior
        int oldX = player.getX();
        int oldY = player.getY();

        // Mover jugador
        if (upPressed)    player.moveUp();
        if (downPressed)  player.moveDown();
        if (leftPressed)  player.moveLeft();
        if (rightPressed) player.moveRight();

        // Verificar colisiones y revertir si hay colision
        if (!ignoreCollisions && colisiones != null) {
            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }
        }

        // Limitar a la ventana actual
        Rectangle bounds = new Rectangle(0, 0,
                escalaManager.getAnchoActual(),
                escalaManager.getAltoActual());
        player.clampTo(bounds);

        // Debug info
        if (DEBUG && debugLabel != null) {
            Rectangle p = player.getBounds();
            debugLabel.setText("Posicion: (" + p.x + ", " + p.y + ") - Colisiones: " + (!ignoreCollisions ? "ON" : "OFF"));
        }

        // Comprobar recogidas cada frame
        checkPickups();

        verificarSalidaInferior();

        repaint();
    }
    
    private Image crearFondoPorDefecto() {
        int width = Math.max(escalaManager.getAnchoActual(), 800);
        int height = Math.max(escalaManager.getAltoActual(), 600);
        
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        // Fondo temático para habitación
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(70, 50, 30),
            width, height, new Color(40, 30, 20)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        // Texto de advertencia
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        String text = "Habitación 1 - Fondo no disponible";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        g2d.dispose();
        return img;
    }

    // Metodo para spawnear exactamente un objeto en la habitacion
    private void spawnObjetosAleatorios() {
        String scene = "habitacion1";
        // Obtener la evidencia exclusiva para esta escena
        String evidenciaAsignada = EstadoJuego.getOrAssignUniqueEvidenceForScene(scene);

        String[] objetos = new String[] { evidenciaAsignada };

        try {
            List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
            if (list == null) {
                list = new ArrayList<>();
            }

            // Solo spawnear si no hay objetos existentes
            if (list.isEmpty()) {
                // Fuerzar posición fija solicitada por el usuario: x=620, y=315
                int[] pos = new int[]{620, 315};
                EstadoJuego.SpawnedObject newObj = new EstadoJuego.SpawnedObject(
                    evidenciaAsignada,
                    pos[0], // Coordenadas base (no escaladas)
                    pos[1]  // Coordenadas base (no escaladas)
                );
                list.add(newObj);
                EstadoJuego.setSpawnedObjects(scene, list);
                System.out.println("Objeto spawnedo en habitacion1 (fijo): " + evidenciaAsignada + " en " + pos[0] + "," + pos[1]);
            } else {
                System.out.println("Ya existen objetos en habitacion1: " + list.size());
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
            int[] pos = GUI.SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos != null) {
                System.out.println("Spawn seguro encontrado: " + pos[0] + "," + pos[1]);
                return pos;
            }
        }
        
        // Si no funciona, intentar spawn aleatorio
        int[] pos = GUI.SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
        if (pos != null) {
            System.out.println("Spawn aleatorio encontrado: " + pos[0] + "," + pos[1]);
            return pos;
        }
        
        // Si todo falla, usar posición por defecto para habitación 1
        System.out.println("Usando spawn por defecto para habitacion1");
        return new int[]{600, 600}; // Posición central por defecto
    }
    
    private void crearLabelsObjetos(String scene, List<EstadoJuego.SpawnedObject> soList) {
        // Limpiar labels existentes
        for (JLabel lbl : objetosLabels.values()) {
            this.remove(lbl);
        }
        objetosLabels.clear();
        
        if (soList == null || soList.isEmpty()) {
            System.out.println("No hay objetos para crear labels en habitacion1");
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
                // Tooltip con coordenadas base (para ver rápidamente dónde está)
                itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");

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

                            int res = JOptionPane.showConfirmDialog(habitacion1.this, p,
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
                                    JOptionPane.showMessageDialog(habitacion1.this,
                                            "Valores inválidos. Usa enteros.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (javax.swing.SwingUtilities.isLeftMouseButton(me)) {
                            // Mostrar un diálogo simple con la posición actual (base)
                            JOptionPane.showMessageDialog(habitacion1.this,
                                    "Posición de " + so.nombre + ": (" + so.x + ", " + so.y + ")",
                                    "Posición objeto", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                 
                 this.add(itemLabel);
                 objetosLabels.put(so, itemLabel);
                 objetosCreados++;
                
                System.out.println("Label creado para objeto: " + so.nombre + " en posición escalada " + x + "," + y);
                
            } catch (Exception ex) {
                System.out.println("Error creando label para " + so.nombre + ": " + ex.getMessage());
            }
        }
        
        System.out.println("Total labels creados en habitacion1: " + objetosCreados);
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
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
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
        String scene = "habitacion1";
        nearbyObject = null;
        
        List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list == null) return;
        
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
        
        System.out.println("Recogiendo objeto: " + nearbyObject.nombre);
        
        String scene = "habitacion1";
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        
        nearbyObject = null;
        this.revalidate();
        this.repaint();
        System.out.println("Evidencia recogida: total=" + EstadoJuego.getObjetosRecogidos());
    }

    private void verificarSalidaInferior() {
        if (player == null) return;
        
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = getHeight();
        int margenSalida = escalaManager.escalaY(5);
        
        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - margenSalida) {
            volverACasaPrincipal();
        }
    }

    private void volverACasaPrincipal() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }

        if (parentFrame != null) {
            try {
                // Usar las constantes públicas de CasaPrincipal para la posición de retorno (valores base sin escalar)
                int retornoX_base = CasaPrincipal.BASE_RETORNO_PUERTA1_X;
                // Colocar 50px por debajo en coordenadas base (CasaPrincipal se encargará de escalar)
                int retornoY_base = CasaPrincipal.BASE_RETORNO_PUERTA1_Y + 50;

                CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame, retornoX_base, retornoY_base);

                parentFrame.getContentPane().removeAll();
                parentFrame.getContentPane().add(siguientePanel);
                parentFrame.revalidate();
                parentFrame.repaint();

                // Configurar escala para el nuevo panel
                escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
                
                siguientePanel.requestFocusInWindow();
            } catch (Exception e) {
                System.out.println("Error al cambiar a casa principal: " + e.getMessage());
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

        // Dibujar jugador
        if (player != null) player.draw(g2);
        
        // Mostrar indicador de recogida usando mensajeLabel (misma estética que comedor)
        if (nearbyObject != null) {
            mensajeLabel.setText("E para recoger");
            mensajeLabel.setVisible(true);
        } else {
            if (mensajeLabel != null) mensajeLabel.setVisible(false);
        }
        
        // Debug: mostrar posición del jugador (permanece solo si DEBUG=true)
        if (DEBUG) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, escalaManager.escalaFuente(12)));
            g2.drawString("Pos: " + player.getX() + "," + player.getY(), 10, getHeight() - 10);
        }
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

    // Configurar key bindings (incluye toggle de colisiones con 'C')
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

        // Tecla 'C' para toggle de colisiones (util para debug)
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
        
        // Tecla 'P' para imprimir posiciones de spawn de esta escena (útil para ajustar)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "print.positions");
        am.put("print.positions", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects("habitacion1");
                System.out.println("== Spawn positions for habitacion1 ==");
                if (list != null) {
                    for (EstadoJuego.SpawnedObject so : list) {
                        System.out.println(so.nombre + " -> (" + so.x + "," + so.y + ") collected=" + so.recogido);
                    }
                }
            }
        });
    }
}