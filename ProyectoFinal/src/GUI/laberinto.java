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

public class laberinto extends JPanel implements KeyListener {
    private jugador player;
    private Image fondo;
    private Timer gameLoop;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private JFrame parentFrame;
    private colisiones colisiones;
    private Map<EstadoJuego.SpawnedObject, JLabel> objetosLabels = new HashMap<>();
    private EstadoJuego.SpawnedObject nearbyObject = null;

    // Mensaje temporal / indicador (misma estética que en comedor/CasaPrincipal)
    private JLabel mensajeLabel;
    private Timer mensajeTimer = null;
    
    public laberinto(JFrame parentFrame) { 
        this.parentFrame = parentFrame;
        setLayout(null);
        setFocusable(true);
        addKeyListener(this);
        setFocusTraversalKeysEnabled(false);
        
        // Configurar escala
        SwingUtilities.invokeLater(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                escalaManager.configurarEscala(getWidth(), getHeight());
            }
        });

        try {
            Musica.detener();
        } catch (Exception e) {
            System.out.println("Error al detener música: " + e.getMessage());
        }

        try {
            fondo = new ImageIcon("src/resources/images/laberinto.png").getImage();
        } catch (Exception e) {
            System.out.println("Error al cargar fondo: " + e.getMessage());
            fondo = crearFondoPorDefecto();
        }

        int playerX = escalaManager.escalaX(580);
        int playerY = escalaManager.escalaY(680);
        player = new jugador(playerX, playerY);
        player.setEscala(0.8);

        try {
            colisiones = new colisiones("src/resources/images/laberinto mascara REWORK 4.png");
        } catch (Exception e) {
            System.out.println("Error al cargar colisiones: " + e.getMessage());
        }

        spawnObjetosAleatorios();

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

        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        
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
        
        if (upPressed) player.moveUp();
        if (downPressed) player.moveDown();
        if (leftPressed) player.moveLeft();
        if (rightPressed) player.moveRight();
        
        if (colisiones != null && colisiones.hayColision(player.getBounds())) {
            player.setPosition(oldX, oldY);
        }
        
        Rectangle bounds = new Rectangle(0, 0, 
                escalaManager.getAnchoActual(), 
                escalaManager.getAltoActual());
        player.clampTo(bounds);
        
        checkPickups();
        verificarSalidaInferior();
        repaint();
    }

    private Image crearFondoPorDefecto() {
        int width = Math.max(escalaManager.getAnchoActual(), 800);
        int height = Math.max(escalaManager.getAltoActual(), 600);
        
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(30, 30, 60),
            width, height, new Color(10, 10, 30)
        );
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, escalaManager.escalaFuente(20)));
        String text = "Laberinto - Fondo no disponible";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2d.drawString(text, (width - textWidth) / 2, height / 2);
        
        g2d.dispose();
        return img;
    }

    private void spawnObjetosAleatorios() {
        String scene = "laberinto";
        // Obtener la evidencia asignada exclusivamente para esta escena
        String evidenciaAsignada = EstadoJuego.getOrAssignUniqueEvidenceForScene(scene);

        String[] objetos = new String[]{
            evidenciaAsignada
        };

        try {
            List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
            if (list == null) list = new ArrayList<>();
            
            // Solo spawnear si no hay objetos existentes
            if (list.isEmpty()) {
                // Forzar posición fija solicitada: x=450, y=85
                int[] pos = new int[]{450, 85};
                EstadoJuego.SpawnedObject newObj = new EstadoJuego.SpawnedObject(
                    evidenciaAsignada,
                    pos[0], // Coordenadas base (no escaladas)
                    pos[1]  // Coordenadas base (no escaladas)
                );
                list.add(newObj);
                EstadoJuego.setSpawnedObjects(scene, list);
                System.out.println("Objeto spawnedo en laberinto (fijo): " + evidenciaAsignada + " en " + pos[0] + "," + pos[1]);
             } else {
                 System.out.println("Ya existen objetos en laberinto: " + list.size());
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
        
        // Si todo falla, usar zonas hardcodeadas específicas para el laberinto
        return obtenerSpawnLaberintoHardcodeado();
    }
    
    private int[] obtenerSpawnLaberintoHardcodeado() {
        // Zonas específicas del laberinto donde es probable que no haya colisiones
        int[][] zonasLaberinto = {
            {580, 600},  // Zona central inferior
            {400, 400},  // Zona izquierda central  
            {760, 400},  // Zona derecha central
            {300, 200},  // Zona superior izquierda
            {860, 200}   // Zona superior derecha
        };
        
        // Verificar colisiones para cada zona
        for (int[] zona : zonasLaberinto) {
            Rectangle bounds = new Rectangle(zona[0], zona[1], 50, 50);
            if (colisiones == null || !colisiones.hayColision(bounds)) {
                System.out.println("Spawn hardcodeado usado: " + zona[0] + "," + zona[1]);
                return zona;
            }
        }
        
        // Último recurso: posición por defecto
        System.out.println("Usando spawn por defecto");
        return new int[]{580, 600};
    }
    
    private void crearLabelsObjetos(String scene, List<EstadoJuego.SpawnedObject> soList) {
        // Limpiar labels existentes
        for (JLabel lbl : objetosLabels.values()) {
            this.remove(lbl);
        }
        objetosLabels.clear();
        
        if (soList == null || soList.isEmpty()) {
            System.out.println("No hay objetos para crear labels");
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
                
                // Mostrar coordenadas base como tooltip
                itemLabel.setToolTipText("coords: (" + so.x + "," + so.y + ")");
                
                // Permitir editar posición con clic derecho y ver posición con clic izquierdo
                itemLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mousePressed(java.awt.event.MouseEvent me) {
                        if (javax.swing.SwingUtilities.isRightMouseButton(me)) {
                            JTextField fx = new JTextField(Integer.toString(so.x));
                            JTextField fy = new JTextField(Integer.toString(so.y));
                            JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
                            p.add(new JLabel("X:")); p.add(fx);
                            p.add(new JLabel("Y:")); p.add(fy);

                            int res = JOptionPane.showConfirmDialog(laberinto.this, p,
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
                                    JOptionPane.showMessageDialog(laberinto.this,
                                            "Valores inválidos. Usa enteros.", "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else if (javax.swing.SwingUtilities.isLeftMouseButton(me)) {
                            JOptionPane.showMessageDialog(laberinto.this,
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
        
        System.out.println("Total labels creados: " + objetosCreados);
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
        String scene = "laberinto";
        nearbyObject = null;
        
        List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects(scene);
        if (list == null) return;
        
        for (EstadoJuego.SpawnedObject so : list) {
            if (so.recogido) continue;
            
            JLabel lbl = objetosLabels.get(so);
            if (lbl == null) continue;
            
            if (playerBounds.intersects(lbl.getBounds())) {
                nearbyObject = so;
                System.out.println("tengo que encontrar la forma de entrar");
                break;
            }
        }
    }

    private void collectNearbyObject() {
        if (nearbyObject == null) return;
        
        System.out.println("Recogiendo objeto: " + nearbyObject.nombre);
        
        String scene = "laberinto";
        EstadoJuego.markObjectCollected(scene, nearbyObject);
        
        JLabel lbl = objetosLabels.get(nearbyObject);
        if (lbl != null) {
            this.remove(lbl);
            objetosLabels.remove(nearbyObject);
        }
        
        nearbyObject = null;
        this.revalidate();
        this.repaint();
    }

    private void verificarSalidaInferior() {
        if (player == null) return;
        
        Rectangle jugadorBounds = player.getBounds();
        int panelHeight = getHeight();
        int margenSalida = escalaManager.escalaY(5);
        
        if (jugadorBounds.y + jugadorBounds.height >= panelHeight - margenSalida) {
            volverACasaIzquierda();
        }
    }
    
    private void volverACasaIzquierda() {
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }
        
        if (parentFrame != null) {
            try {
                int posX = escalaManager.escalaX(25);
                int posY = escalaManager.escalaY(345);
                
                casaIzquierda casaPanel = new casaIzquierda(parentFrame, posX, posY);
                parentFrame.getContentPane().removeAll();
                parentFrame.getContentPane().add(casaPanel);
                parentFrame.revalidate();
                parentFrame.repaint();
                
                escalaManager.configurarEscala(casaPanel.getWidth(), casaPanel.getHeight());
                casaPanel.requestFocusInWindow();
            } catch (Exception e) {
                System.out.println("Error al cambiar a casa izquierda: " + e.getMessage());
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fondo != null) {
            g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
        }
        if (player != null) player.draw(g);
        
        // Mostrar indicador de recogida dentro del mensajeLabel (misma estética que comedor)
        if (nearbyObject != null) {
            mensajeLabel.setText("E para recoger");
            mensajeLabel.setVisible(true);
        } else {
            if (mensajeLabel != null) mensajeLabel.setVisible(false);
        }
    }

    /**
     * Dibuja un recuadro negro (con borde blanco) y el texto en su interior.
     * Mantiene tamaño/espaciado escalado con escalaManager para consistencia.
     */
    private void drawMessageBox(Graphics g, String text, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font("Arial", Font.BOLD, escalaManager.escalaFuente(14));
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();

            int paddingX = escalaManager.escalaUniforme(8);
            int paddingY = escalaManager.escalaUniforme(6);
            int textWidth = fm.stringWidth(text);
            int boxWidth = textWidth + paddingX * 2;
            int boxHeight = fm.getHeight() + paddingY * 2;

            // Ajustar para que el cuadro no salga del panel
            if (x + boxWidth > getWidth()) {
                x = Math.max(10, getWidth() - boxWidth - escalaManager.escalaUniforme(10));
            }
            if (y + boxHeight > getHeight()) {
                y = Math.max(10, getHeight() - boxHeight - escalaManager.escalaUniforme(10));
            }

            // Fondo negro semitransparente
            g2d.setColor(new Color(0, 0, 0, 220));
            int arc = Math.max(6, escalaManager.escalaUniforme(8));
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, arc, arc);

            // Borde blanco
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, arc, arc);

            // Texto en blanco
            g2d.setColor(Color.WHITE);
            int textX = x + paddingX;
            int textY = y + paddingY + fm.getAscent();
            g2d.drawString(text, textX, textY);
        } finally {
            g2d.dispose();
        }
    }
    
    private void drawMessageBoxAtBaseline(Graphics g, String text, int x, int baselineY) {
        // Calcula la posición superior del recuadro para que el texto tenga su baseline en baselineY
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font("Arial", Font.BOLD, escalaManager.escalaFuente(14));
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();

            int paddingX = escalaManager.escalaUniforme(8);
            int paddingY = escalaManager.escalaUniforme(6);
            int textWidth = fm.stringWidth(text);
            int boxWidth = textWidth + paddingX * 2;
            int boxHeight = fm.getHeight() + paddingY * 2;

            // Calcular y superior a partir del baseline
            int y = baselineY - fm.getAscent() - paddingY;

            // Ajustar para que el cuadro no salga del panel
            if (x + boxWidth > getWidth()) {
                x = Math.max(10, getWidth() - boxWidth - escalaManager.escalaUniforme(10));
            }
            if (y < 0) {
                y = 10; // evitar fuera por arriba
            }
            if (y + boxHeight > getHeight()) {
                y = Math.max(10, getHeight() - boxHeight - escalaManager.escalaUniforme(10));
            }

            // Fondo negro semitransparente
            g2d.setColor(new Color(0, 0, 0, 220));
            int arc = Math.max(6, escalaManager.escalaUniforme(8));
            g2d.fillRoundRect(x, y, boxWidth, boxHeight, arc, arc);

            // Borde blanco
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, boxWidth, boxHeight, arc, arc);

            // Texto en blanco (alineado para que la baseline caiga en baselineY)
            g2d.setColor(Color.WHITE);
            int textX = x + paddingX;
            int textY = y + paddingY + fm.getAscent();
            g2d.drawString(text, textX, textY);
        } finally {
            g2d.dispose();
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
        // Tecla 'P' para imprimir posiciones de spawn del laberinto (útil para ajustar)
        if (key == KeyEvent.VK_P) {
            java.util.List<EstadoJuego.SpawnedObject> list = EstadoJuego.getSpawnedObjects("laberinto");
            System.out.println("== Spawn positions for laberinto ==");
            if (list != null) {
                for (EstadoJuego.SpawnedObject so : list) {
                    System.out.println(so.nombre + " -> (" + so.x + "," + so.y + ") collected=" + so.recogido);
                }
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
}