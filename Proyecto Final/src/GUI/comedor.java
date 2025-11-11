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
    private Image imagenCofre;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    private Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;

    private boolean ignoreCollisions = false;
    private static final boolean DEBUG = false;
    private JLabel debugLabel;

    private boolean showObjectPositions = false;
    private java.util.Map<JLabel, EstadoJuego.SpawnedObject> labelToObject = new HashMap<>();
    private Point dragOffset = null;
    private JLabel coordOverlay = null;

    private java.util.Map<EstadoJuego.SpawnedObject, JLabel> objetosCoordLabels = new HashMap<>();

    private static final String CASCARA_NAME = "Cascara de Chimpanzini Bananini.png";
    private static final int CASCARA_X = 660;
    private static final int CASCARA_Y = 265;

    // Coordenadas del cofre
    private static final int BASE_COFRE_X = 1110;
    private static final int BASE_COFRE_Y = 240;
    private static final int BASE_COFRE_W = 100;
    private static final int BASE_COFRE_H = 100;

    // Variables para el cofre
    private boolean estaEnCofre = false;
    private boolean cofreAbierto = false;

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

        SwingUtilities.invokeLater(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                escalaManager.configurarEscala(getWidth(), getHeight());
            }
        });

        setupKeyBindings();

        // Cargar fondo
        try {
            fondo = new ImageIcon("src/resources/images/comedor REWORK.png").getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar fondo: " + e.getMessage());
            fondo = crearFondoPorDefecto();
        }

        // Cargar imagen del cofre según su estado
        if (EstadoJuego.isCofreComedorAbierto()) {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreComedorAbierto.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
                }
            } catch (Exception e) {
                imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            }
            cofreAbierto = true;
        } else {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreComedor.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
                }
            } catch (Exception e) {
                imagenCofre = new ImageIcon("src/resources/images/cofre.png").getImage();
            }
        }

        // Cargar colisiones
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

        // Crear mensaje temporal
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

        spawnObjetosAleatorios();

        addKeyListener(this);
        SwingUtilities.invokeLater(this::requestFocusInWindow);

        gameLoop = new Timer(16, e -> updateGame());
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
        actualizarPosicionesObjetos();
    }

    private void updateGame() {
        int oldX = player.getX();
        int oldY = player.getY();

        if (upPressed) player.moveUp();
        if (downPressed) player.moveDown();
        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight();

        if (!ignoreCollisions && colisiones != null) {
            if (colisiones.hayColision(player.getBounds())) {
                player.setPosition(oldX, oldY);
            }
        }

        Rectangle bounds = new Rectangle(0, 0, escalaManager.getAnchoActual(), escalaManager.getAltoActual());
        player.clampTo(bounds);

        verificarPosicionCofre();
        checkPickups();
        verificarSalidas();
        actualizarIndicadorProximidad();

        repaint();
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
            mensajeLabel.setText("E para recoger");
            mensajeLabel.setVisible(true);
            return;
        }

        mensajeLabel.setVisible(false);
    }

    private void abrirCofre() {
        if (!cofreAbierto) {
            try {
                imagenCofre = new ImageIcon("src/resources/images/cofreComedorAbierto.png").getImage();
                if (imagenCofre == null || imagenCofre.getWidth(null) <= 0) {
                    imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
                }
            } catch (Exception e) {
                System.out.println("Error cargando imagen del cofre abierto: " + e.getMessage());
                imagenCofre = new ImageIcon("src/resources/images/cofreAbierto.png").getImage();
            }
            
            cofreAbierto = true;
            EstadoJuego.setCofreComedorAbierto(true);
            
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
            System.out.println("Cofre abierto en comedor - Llave encontrada");
        }
    }

    private Image crearFondoPorDefecto() {
        int width = Math.max(escalaManager.getAnchoActual(), 800);
        int height = Math.max(escalaManager.getAltoActual(), 600);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        GradientPaint gradient = new GradientPaint(0, 0, new Color(80, 60, 40), width, height, new Color(50, 35, 25));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

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
        try {
            List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
            if (list == null) list = new ArrayList<>();

            boolean foundCascara = false;
            for (EstadoJuego.SpawnedObject so : list) {
                if (CASCARA_NAME.equals(so.nombre)) {
                    so.nombre = CASCARA_NAME;
                    so.x = CASCARA_X;
                    so.y = CASCARA_Y;
                    foundCascara = true;
                    break;
                }
            }

            if (!foundCascara) {
                list.add(new EstadoJuego.SpawnedObject(CASCARA_NAME, CASCARA_X, CASCARA_Y));
                System.out.println("Cáscara añadida en comedor: " + CASCARA_NAME + " en " + CASCARA_X + "," + CASCARA_Y);
            }

            EstadoJuego.setSpawnedObjects(scene, list);

            final List<EstadoJuego.SpawnedObject> spawnList = list;
            if (getWidth() <= 0 || getHeight() <= 0) {
                SwingUtilities.invokeLater(() -> crearLabelsObjetos(scene, spawnList));
            } else {
                crearLabelsObjetos(scene, spawnList);
            }
        } catch (Exception e) {
            System.out.println("Error en spawnObjetosAleatorios (comedor): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int[] obtenerPosicionSpawnSegura(String scene) {
        if (colisiones != null) {
            int[] pos = SistemaSpawnJuego.obtenerSpawnSeguro(scene, colisiones, true);
            if (pos != null) {
                System.out.println("Spawn seguro encontrado: " + pos[0] + "," + pos[1]);
                return pos;
            }
        }
        int[] pos = SistemaSpawnJuego.obtenerSpawnAleatorio(scene, true);
        if (pos != null) {
            System.out.println("Spawn aleatorio encontrado: " + pos[0] + "," + pos[1]);
            return pos;
        }
        System.out.println("Usando spawn por defecto para comedor");
        return new int[]{600, 500};
    }

    private void crearLabelsObjetos(String scene, List<EstadoJuego.SpawnedObject> soList) {
        final String sceneFinal = scene;
        final List<EstadoJuego.SpawnedObject> soListFinal = soList;
         
         for (JLabel lbl : objetosLabels.values()) this.remove(lbl);
         objetosLabels.clear();
         labelToObject.clear();

         for (JLabel cl : objetosCoordLabels.values()) this.remove(cl);
         objetosCoordLabels.clear();

         if (soList == null || soList.isEmpty()) {
             System.out.println("No hay objetos para crear labels en comedor");
             return;
         }

         int size = escalaManager.escalaUniforme(64);
         int objetosCreados = 0;

         for (EstadoJuego.SpawnedObject so : soList) {
             if (so.recogido) continue;

             try {
                 Image img = cargarImagenObjeto(so.nombre);
                 if (img == null) continue;

                 Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                 JLabel itemLabel = new JLabel(new ImageIcon(scaled));

                 int x = escalaManager.escalaX(so.x) - size / 2;
                 int y = escalaManager.escalaY(so.y) - size / 2;
                 itemLabel.setBounds(x, y, size, size);

                 if (!CASCARA_NAME.equals(so.nombre)) {
                     itemLabel.addMouseListener(new MouseAdapter() {
                         @Override
                         public void mousePressed(MouseEvent e) {
                             if (!showObjectPositions) return;
                             dragOffset = e.getPoint();
                             if (coordOverlay == null) {
                                 coordOverlay = new JLabel("", JLabel.CENTER);
                                 coordOverlay.setFont(new Font("Arial", Font.PLAIN, escalaManager.escalaFuente(12)));
                                 coordOverlay.setForeground(Color.WHITE);
                                 coordOverlay.setBackground(new Color(0, 0, 0, 150));
                                 coordOverlay.setOpaque(true);
                                 add(coordOverlay);
                             }
                             EstadoJuego.SpawnedObject soLocal = objetosLabels.entrySet().stream()
                                     .filter(en -> en.getValue() == itemLabel)
                                     .map(en -> en.getKey()).findFirst().orElse(null);
                             if (soLocal != null) {
                                 coordOverlay.setText("Base: " + soLocal.x + "," + soLocal.y + " - " + soLocal.nombre);
                                 coordOverlay.setBounds(itemLabel.getX(), itemLabel.getY() - 25, 140, 20);
                                 coordOverlay.setVisible(true);
                             }
                         }

                         @Override
                         public void mouseReleased(MouseEvent e) {
                             if (!showObjectPositions) return;
                             dragOffset = null;
                             if (coordOverlay != null) coordOverlay.setVisible(false);
                         }
                     });

                     itemLabel.addMouseMotionListener(new MouseMotionAdapter() {
                         @Override
                         public void mouseDragged(MouseEvent e) {
                             if (!showObjectPositions || dragOffset == null) return;
                             Point mouse = SwingUtilities.convertPoint(itemLabel, e.getPoint(), comedor.this);
                             int newX = mouse.x - dragOffset.x;
                             int newY = mouse.y - dragOffset.y;
                             itemLabel.setLocation(newX, newY);

                             EstadoJuego.SpawnedObject soLocal = objetosLabels.entrySet().stream()
                                     .filter(en -> en.getValue() == itemLabel)
                                     .map(en -> en.getKey()).findFirst().orElse(null);
                             if (soLocal != null) {
                                 int centerX = newX + itemLabel.getWidth() / 2;
                                 int centerY = newY + itemLabel.getHeight() / 2;
                                 int baseW = 1366, baseH = 768;
                                 int panelW = Math.max(getWidth(), 1);
                                 int panelH = Math.max(getHeight(), 1);
                                 int px = (int) Math.round((double) centerX / (double) panelW * baseW);
                                 int py = (int) Math.round((double) centerY / (double) panelH * baseH);
                                 soLocal.x = px;
                                 soLocal.y = py;
                                 if (coordOverlay != null) coordOverlay.setText("Base: " + soLocal.x + "," + soLocal.y + " - " + soLocal.nombre);
                                 JLabel coord = objetosCoordLabels.get(soLocal);
                                 if (coord != null) {
                                     coord.setText(soLocal.x + "," + soLocal.y);
                                     coord.setBounds(newX, newY - 18, 140, 18);
                                 }
                                 EstadoJuego.setSpawnedObjects(sceneFinal, soListFinal);
                             }
                             revalidate();
                             repaint();
                         }
                     });
                 }

                 this.add(itemLabel);
                 objetosLabels.put(so, itemLabel);
                 labelToObject.put(itemLabel, so);
                 objetosCreados++;

                 if (!CASCARA_NAME.equals(so.nombre)) {
                     JLabel coordLabel = new JLabel(so.x + "," + so.y);
                     coordLabel.setFont(new Font("Arial", Font.PLAIN, escalaManager.escalaFuente(12)));
                     coordLabel.setForeground(Color.WHITE);
                     coordLabel.setBackground(new Color(0, 0, 0, 160));
                     coordLabel.setOpaque(true);
                     coordLabel.setVisible(showObjectPositions);
                     coordLabel.setBounds(x, y - 18, 140, 18);
                     this.add(coordLabel);
                     objetosCoordLabels.put(so, coordLabel);
                 }

             } catch (Exception ex) {
                 System.out.println("Error creando label para " + so.nombre + ": " + ex.getMessage());
             }
         }

         if (mensajeLabel != null && mensajeLabel.getParent() == this) setComponentZOrder(mensajeLabel, 0);

         actualizarPosicionesObjetos();

         System.out.println("Total labels creados en comedor: " + objetosCreados);
         this.revalidate();
         this.repaint();
     }

    private void actualizarPosicionesObjetos() {
        if (objetosLabels == null || objetosLabels.isEmpty()) return;
        int size = escalaManager.escalaUniforme(64);
        for (java.util.Map.Entry<EstadoJuego.SpawnedObject, JLabel> en : objetosLabels.entrySet()) {
            EstadoJuego.SpawnedObject so = en.getKey();
            JLabel lbl = en.getValue();
            if (lbl == null || so == null) continue;
            int nx = escalaManager.escalaX(so.x) - size / 2;
            int ny = escalaManager.escalaY(so.y) - size / 2;
            lbl.setBounds(nx, ny, size, size);
            JLabel coord = objetosCoordLabels.get(so);
            if (coord != null) {
                coord.setText(so.x + "," + so.y);
                coord.setBounds(nx, ny - 18, 140, 18);
            }
        }
    }

    private Image cargarImagenObjeto(String nombre) {
        try {
            if (getClass().getResource("/resources/images/" + nombre) != null) {
                return new ImageIcon(getClass().getResource("/resources/images/" + nombre)).getImage();
            } else {
                return new ImageIcon("src/resources/images/" + nombre).getImage();
            }
        } catch (Exception e) {
            System.out.println("No se pudo cargar imagen: " + nombre + " -> " + e.getMessage());
            return crearIconoObjetoPorDefecto();
        }
    }

    private Image crearIconoObjetoPorDefecto() {
        int size = escalaManager.escalaUniforme(64);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        g2d.setColor(Color.YELLOW);
        g2d.fillOval(5, 5, size - 10, size - 10);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(5, 5, size - 10, size - 10);
        g2d.drawString("?", size / 2 - 5, size / 2 + 5);

        g2d.dispose();
        return img;
    }

    private void mostrarPosicionesObjetos() {
        String scene = "comedor";
        java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list == null || list.isEmpty()) {
            System.out.println("No hay objetos en comedor");
            return;
        }
        System.out.println("Posiciones base de objetos en comedor:");
        for (EstadoJuego.SpawnedObject so : list) {
            System.out.println(" - " + so.nombre + " -> x=" + so.x + ", y=" + so.y);
        }
    }

    private void checkPickups() {
        if (player == null) return;

        Rectangle playerBounds = player.getBounds();
        String scene = "comedor";
        nearbyObject = null;

        List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list == null) {
            return;
        }

        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;

            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;

            if (playerBounds.intersects(lbl.getBounds())) {
                nearbyObject = so;
                break;
            }
        }
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        int objetosAntes = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos antes de recoger: " + objetosAntes);
        
        String scene = "comedor"; 
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        
        JLabel coord = objetosCoordLabels.remove(nearbyObject);
        if (coord != null) {
            this.remove(coord);
        }
        
        nearbyObject = null;
        this.revalidate();
        this.repaint();
        
        int objetosDespues = EstadoJuego.getObjetosRecogidos();
        System.out.println("Objetos después de recoger: " + objetosDespues);
        
        if (objetosAntes == 4 && objetosDespues == 5) {
            mostrarMensajeDuracion("Tendre que hablar con Brr brr patapim", 4000);
            EstadoJuego.setPuedeMostrarDialogosEspeciales(true);
        } else {
            mostrarMensajeDuracion("Evidencia recogida", 1200);
        }
    }

    private void verificarSalidas() {
        if (player == null) return;

        Rectangle jugadorBounds = player.getBounds();
        int panelWidth = getWidth();
        int margenSalida = escalaManager.escalaX(5);

        if (jugadorBounds.x <= margenSalida) {
            volverAPasillo1();
        }
        if (jugadorBounds.x + jugadorBounds.width >= panelWidth - margenSalida) {
            volverAPasillo2();
        }
    }

    private void volverAPasillo1() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (parentFrame != null) {
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
            escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
            siguientePanel.requestFocusInWindow();
        }
    }

    private void volverAPasillo2() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        if (parentFrame != null) {
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
            escalaManager.configurarEscala(siguientePanel.getWidth(), siguientePanel.getHeight());
            siguientePanel.requestFocusInWindow();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (fondo != null) g2.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        
        // Dibujar cofre escalado
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

    private void mostrarMensajeDuracion(String mensaje, int ms) {
        if (mensajeLabel == null) {
            System.out.println("ERROR: mensajeLabel es null");
            return;
        }
        
        System.out.println("Mostrando mensaje: '" + mensaje + "' por " + ms + "ms");
        
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
            System.out.println("Ocultando mensaje: '" + mensaje + "'");
            mensajeLabel.setVisible(false);
            ((Timer)e.getSource()).stop();
        });
        tempTimer.setRepeats(false);
        tempTimer.start();
        
        mensajeTimer = tempTimer;
        
        revalidate();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) upPressed = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) downPressed = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_E) {
            // Priorizar cofre > objetos
            if (estaEnCofre && !cofreAbierto) {
                abrirCofre();
            } else if (nearbyObject != null) {
                collectNearbyObject();
            }
        }
        if (key == KeyEvent.VK_P) {
            showObjectPositions = !showObjectPositions;
            System.out.println("Modo edición posiciones objetos: " + (showObjectPositions ? "ON" : "OFF"));
            if (!showObjectPositions && coordOverlay != null) coordOverlay.setVisible(false);
            if (showObjectPositions) mostrarPosicionesObjetos();
            for (JLabel l : objetosCoordLabels.values()) l.setVisible(showObjectPositions);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) upPressed = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) downPressed = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) leftPressed = false;
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
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "action.pickup");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0, false), "debug.spawnObject");

        am.put("toggle.collision", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ignoreCollisions = !ignoreCollisions;
                System.out.println("Colisiones: " + (!ignoreCollisions ? "ACTIVADAS" : "DESACTIVADAS"));
            }
        });

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

        am.put("debug.spawnObject", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String scene = "comedor";
                    java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
                    if (list == null) list = new java.util.ArrayList<>();
                    int centerX = player.getX() + player.getAncho()/2;
                    int centerY = player.getY() + player.getAlto()/2;
                    int panelW = Math.max(getWidth(), 1);
                    int panelH = Math.max(getHeight(), 1);
                    int baseW = 1366, baseH = 768;
                    int px = (int) Math.round((double) centerX / (double) panelW * baseW);
                    int py = (int) Math.round((double) centerY / (double) panelH * baseH);
                    EstadoJuego.SpawnedObject dbg = new EstadoJuego.SpawnedObject("zapa.png", px, py);
                    list.add(dbg);
                    EstadoJuego.setSpawnedObjects(scene, list);
                    crearLabelsObjetos(scene, list);
                    System.out.println("Debug: spawn zapa.png en base=" + px + "," + py);
                } catch (Exception ex) {
                    System.out.println("Error spawn debug: " + ex.getMessage());
                }
            }
        });

        am.put("up.press", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { upPressed = true; } });
        am.put("up.release", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { upPressed = false; } });
        am.put("down.press", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { downPressed = true; } });
        am.put("down.release", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { downPressed = false; } });
        am.put("left.press", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { leftPressed = true; } });
        am.put("left.release", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { leftPressed = false; } });
        am.put("right.press", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { rightPressed = true; } });
        am.put("right.release", new AbstractAction() { @Override public void actionPerformed(ActionEvent e) { rightPressed = false; } });
    }
}