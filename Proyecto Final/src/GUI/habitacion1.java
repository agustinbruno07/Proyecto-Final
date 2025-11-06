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
    private Image imagenCofre; // NUEVO: imagen del cofre
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean estaEnSalida = false;
    private JFrame parentFrame;
    private colisiones colisiones;
    private Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;

    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;

    // NUEVO: Variables para el cofre
    private boolean estaEnCofre = false;
    private boolean cofreAbierto = false;

    // NUEVO: Coordenadas del cofre (ajusta según tu diseño)
    private static final int BASE_COFRE_X = 1090;
    private static final int BASE_COFRE_Y = 300;
    private static final int BASE_COFRE_W = 100;
    private static final int BASE_COFRE_H = 100;

    private JLabel mensajeLabel;
    private Timer mensajeTimer = null;
    private static final int BASE_PLAYER_X = 650;
    private static final int BASE_PLAYER_Y = 680;

    public habitacion1(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        SwingUtilities.invokeLater(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                escalaManager.configurarEscala(getWidth(), getHeight());
            }
        });

        setupKeyBindings();

        try {
            fondo = new ImageIcon("src/resources/images/habitacion1 REWORK.png").getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar fondo: " + e.getMessage());
            fondo = crearFondoPorDefecto();
        }

        // NUEVO: Cargar imagen del cofre según su estado
        if (EstadoJuego.isCofreHabitacion1Abierto()) {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreHabitacion1Abierto.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
                }
            } catch (Exception e) {
                imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            }
            cofreAbierto = true;
        } else {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreHabitacion1.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
                }
            } catch (Exception e) {
                imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
            }
        }

        try {
            colisiones = new colisiones("src/resources/images/habitacion1Mascara.png");
        } catch (Exception e) {
            System.out.println("Error al cargar colisiones: " + e.getMessage());
        }

        int startX = escalaManager.escalaX(BASE_PLAYER_X);
        int startY = escalaManager.escalaY(BASE_PLAYER_Y);
        player = new jugador(startX, startY);

        spawnObjetosAleatorios();

        addKeyListener(this);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());

        if (DEBUG) {
            debugLabel = new JLabel("", JLabel.LEFT);
            debugLabel.setForeground(Color.WHITE);
            debugLabel.setBackground(new Color(0, 0, 0, 120));
            debugLabel.setOpaque(true);
            debugLabel.setBounds(10, 10, 400, 20);
            add(debugLabel);
        }

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

        if (DEBUG && debugLabel != null) {
            Rectangle p = player.getBounds();
            debugLabel.setText("Posicion: (" + p.x + ", " + p.y + ") - Colisiones: " + (!ignoreCollisions ? "ON" : "OFF"));
        }

        verificarPosicionCofre(); // NUEVO: verificar proximidad al cofre
        checkPickups();
        verificarSalidaInferior();
        actualizarIndicadorProximidad(); // NUEVO: actualizar mensajes de proximidad

        repaint();
    }

    // NUEVO: Verificar proximidad al cofre
    private void verificarPosicionCofre() {
        Rectangle jugadorBounds = player.getBounds();
        
        int cofreX = escalaManager.escalaX(BASE_COFRE_X);
        int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
        int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
        int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
        
        Rectangle cofreBounds = new Rectangle(cofreX, cofreY, cofreW, cofreH);
        estaEnCofre = jugadorBounds.intersects(cofreBounds);
    }

    // NUEVO: Actualizar indicador de proximidad (prioridad: cofre > objetos)
    private void actualizarIndicadorProximidad() {
        // No sobrescribir mensajes temporales mientras el timer está activo
        if (mensajeTimer != null && mensajeTimer.isRunning()) {
            return;
        }

        // Prioridad 1: Cofre
        if (estaEnCofre && !cofreAbierto) {
            mensajeLabel.setText("E para abrir");
            mensajeLabel.setVisible(true);
            return;
        }

        // Prioridad 2: Objetos cercanos
        if (nearbyObject != null) {
            mensajeLabel.setText("E para recoger");
            mensajeLabel.setVisible(true);
            return;
        }

        // Si no está cerca de nada, ocultar mensaje
        mensajeLabel.setVisible(false);
    }

    // NUEVO: Abrir el cofre
    private void abrirCofre() {
        if (!cofreAbierto) {
            // Intentar cargar la imagen del cofre abierto
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreHabitacion1Abierto.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
                }
            } catch (Exception e) {
                System.out.println("Error cargando imagen del cofre abierto: " + e.getMessage());
                imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            }
            
            cofreAbierto = true;
            EstadoJuego.setCofreHabitacion1Abierto(true);
            
            // Forzar la visualización del mensaje
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
            System.out.println("Cofre abierto en habitacion1 - Llave encontrada");
        }
    }
    
    private Image crearFondoPorDefecto() {
        int width = Math.max(escalaManager.getAnchoActual(), 800);
        int height = Math.max(escalaManager.getAltoActual(), 600);
        
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(70, 50, 30),
            width, height, new Color(40, 30, 20)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        String text = "Habitación 1 - Fondo no disponible";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        g2d.dispose();
        return img;
    }

    private void spawnObjetosAleatorios() {
        String scene = "habitacion1";
        String evidenciaAsignada = EstadoJuego.getOrAssignUniqueEvidenceForScene(scene);

        String[] objetos = new String[] { evidenciaAsignada };

        try {
            List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
            if (list == null) {
                list = new ArrayList<>();
            }

            if (list.isEmpty()) {
                int[] pos = new int[]{620, 315};
                EstadoJuego.SpawnedObject newObj = new EstadoJuego.SpawnedObject(
                    evidenciaAsignada,
                    pos[0],
                    pos[1]
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
        if (colisiones != null) {
            int[] pos = GUI.SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos != null) {
                System.out.println("Spawn seguro encontrado: " + pos[0] + "," + pos[1]);
                return pos;
            }
        }
        
        int[] pos = GUI.SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
        if (pos != null) {
            System.out.println("Spawn aleatorio encontrado: " + pos[0] + "," + pos[1]);
            return pos;
        }
        
        System.out.println("Usando spawn por defecto para habitacion1");
        return new int[]{600, 600};
    }
    
    private void crearLabelsObjetos(String scene, List<EstadoJuego.SpawnedObject> soList) {
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
                
                int x = escalaManager.escalaX(so.x) - size/2;
                int y = escalaManager.escalaY(so.y) - size/2;
                
                itemLabel.setBounds(x, y, size, size);
                itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");

                itemLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(java.awt.event.MouseEvent me) {
                        if (javax.swing.SwingUtilities.isRightMouseButton(me)) {
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
                                    so.x = nx;
                                    so.y = ny;
                                    int nxScaled = escalaManager.escalaX(so.x) - size/2;
                                    int nyScaled = escalaManager.escalaY(so.y) - size/2;
                                    itemLabel.setBounds(nxScaled, nyScaled, size, size);
                                    itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");
                                    EstadoJuego.setSpawnedObjects(scene, EstadoJuego.getSpawnedObjects(scene));
                                    repaint();
                                } catch (NumberFormatException ex) {
                                    JOptionPane.showMessageDialog(habitacion1.this,
                                            "Valores inválidos. Usa enteros.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (javax.swing.SwingUtilities.isLeftMouseButton(me)) {
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
            if (getClass().getResource("/resources/images/" + nombre) != null) {
                return new ImageIcon(getClass().getResource("/resources/images/" + nombre)).getImage();
            } else {
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
        
        int objetosAntes = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos antes de recoger: " + objetosAntes);
        
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
                CasaPrincipal siguientePanel = new CasaPrincipal(parentFrame, CasaPrincipal.BASE_RETORNO_PUERTA1_X, CasaPrincipal.BASE_RETORNO_PUERTA1_Y);

                parentFrame.getContentPane().removeAll();
                parentFrame.getContentPane().add(siguientePanel);
                parentFrame.revalidate();
                parentFrame.repaint();

                escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
                
                siguientePanel.requestFocusInWindow();
            } catch (Exception e) {
                System.out.println("Error al cambiar a casa principal: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

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
        
        System.out.println("Mostrando mensaje en habitacion1: '" + mensaje + "' por " + ms + "ms");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (fondo != null) {
            g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        }

        // NUEVO: Dibujar cofre escalado
        if (imagenCofre != null) {
            int cofreX = escalaManager.escalaX(BASE_COFRE_X);
            int cofreY = escalaManager.escalaY(BASE_COFRE_Y);
            int cofreW = escalaManager.escalaAncho(BASE_COFRE_W);
            int cofreH = escalaManager.escalaAlto(BASE_COFRE_H);
            g2.drawImage(imagenCofre, cofreX, cofreY, cofreW, cofreH, this);
        }

        if (player != null) player.draw(g2);
        
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
        if (key == KeyEvent.VK_E) {
            // MODIFICADO: Priorizar cofre > objetos
            if (estaEnCofre && !cofreAbierto) {
                abrirCofre();
            } else if (nearbyObject != null) {
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

        // Tecla 'C' para toggle de colisiones (util para debug)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "toggle.collision");
        am.put("toggle.collision", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ignoreCollisions = !ignoreCollisions;
                System.out.println("Colisiones: " + (!ignoreCollisions ? "ACTIVADAS" : "DESACTIVADAS"));
            }
        });

        // MODIFICADO: Acción de pickup también prioriza cofre
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "action.pickup");
        am.put("action.pickup", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (estaEnCofre && !cofreAbierto) {
                    abrirCofre();
                } else if (nearbyObject != null) {
                    collectNearbyObject();
                }
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