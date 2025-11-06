package GUI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.DisplayMode;

public class config extends JPanel {
    private Image imagenFondo;
    
    private static int volumenEfectos = 50;
    private static boolean pantallaCompleta = true;
    private static String dificultad = "Normal";
    
    private static int resolucionAncho = 1366;
    private static int resolucionAlto = 768;
    // Para restaurar el modo de pantalla original si cambiamos la DisplayMode
    private static DisplayMode previousDisplayMode = null;
    private JComboBox<String> comboResoluciones;
    
    private JSlider sliderEfectos;
    private JButton btnGuardar;
    private JButton btnRestablecer;
    private JButton btnVolver;
    
    private JFrame parentFrame;
    // Referencia opcional al diálogo modal que contiene este panel
    private javax.swing.JDialog ownerDialog = null;
    private Runnable onCloseCallback = null;

    private int originalVolumenEfectos;
    private int originalResolucionAncho;
    private int originalResolucionAlto;

    public config() {
        setLayout(null);
        setBackground(new Color(20, 20, 30));

        // Guardar snapshot de valores al abrir la configuración
        // Ya no hay música en el juego: sólo guardamos efectos
        originalVolumenEfectos = Musica.getVolumenEfectos();
        originalResolucionAncho = resolucionAncho;
        originalResolucionAlto = resolucionAlto;

        try {
            imagenFondo = new ImageIcon("src/resources/images/fondo_config.png").getImage();
        } catch (Exception e) {
            imagenFondo = null;
        }
        
        // Configurar componentes con posiciones fijas pero en una ventana de tamaño fijo
        JLabel lblTitulo = new JLabel("CONFIGURACION", JLabel.CENTER);
        lblTitulo.setBounds(0, 20, 600, 40);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        add(lblTitulo);
        
        JLabel lblAudio = new JLabel("AUDIO");
        lblAudio.setBounds(50, 80, 500, 25);
        lblAudio.setFont(new Font("Arial", Font.BOLD, 18));
        lblAudio.setForeground(new Color(100, 200, 255));
        add(lblAudio);
        
        JLabel lblEfectosTexto = new JLabel("Volumen Efectos:");
        lblEfectosTexto.setBounds(70, 115, 150, 25);
        lblEfectosTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        lblEfectosTexto.setForeground(Color.WHITE);
        add(lblEfectosTexto);
        
        sliderEfectos = new JSlider(0, 100, volumenEfectos);
        sliderEfectos.setBounds(220, 115, 250, 40);
        sliderEfectos.setBackground(new Color(20, 20, 30));
        sliderEfectos.setForeground(new Color(100, 200, 255));
        add(sliderEfectos);
        
        JLabel lblVolumenEfectos = new JLabel(volumenEfectos + "%");
        lblVolumenEfectos.setBounds(480, 115, 50, 25);
        lblVolumenEfectos.setFont(new Font("Arial", Font.BOLD, 14));
        lblVolumenEfectos.setForeground(Color.WHITE);
        add(lblVolumenEfectos);
        
        sliderEfectos.addChangeListener(e -> {
            int val = sliderEfectos.getValue();
            lblVolumenEfectos.setText(val + "%");
            // Aplicar en tiempo real
            Musica.setVolumenEfectos(val);
        });
        
        JLabel lblJuego = new JLabel("JUEGO");
        lblJuego.setBounds(50, 165, 500, 25);
        lblJuego.setFont(new Font("Arial", Font.BOLD, 18));
        lblJuego.setForeground(new Color(100, 200, 255));
        add(lblJuego);
        
        JLabel lblResolTexto = new JLabel("Resolucion:");
        lblResolTexto.setBounds(70, 200, 150, 25);
        lblResolTexto.setFont(new Font("Arial", Font.PLAIN, 14));
        lblResolTexto.setForeground(Color.WHITE);
        add(lblResolTexto);
        
        String[] baseRes = {
        	     // Full HD
        	    "1366x768",   // Base (HD Ready)
        	     // HD
        	};
        comboResoluciones = new JComboBox<>(baseRes);
        comboResoluciones.setEnabled(true);
        comboResoluciones.setBounds(220, 235, 200, 25);
        comboResoluciones.setBackground(new Color(20, 20, 30));
        comboResoluciones.setForeground(Color.WHITE);
        comboResoluciones.setFont(new Font("Arial", Font.PLAIN, 14));

        String actualRes = resolucionAncho + "x" + resolucionAlto;
        // Si la resolución actual no está en la lista, agregarla y seleccionarla
        boolean encontrado = false;
        for (int i = 0; i < comboResoluciones.getItemCount(); i++) {
            if (comboResoluciones.getItemAt(i).equals(actualRes)) {
                comboResoluciones.setSelectedIndex(i);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            comboResoluciones.addItem(actualRes);
            comboResoluciones.setSelectedItem(actualRes);
        }
         
        add(comboResoluciones);
        
        btnGuardar = new JButton("GUARDAR");
        btnGuardar.setBounds(100, 375, 130, 40);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(50, 150, 50));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setBorderPainted(false);
        add(btnGuardar);
        
        btnGuardar.addActionListener(e -> guardarConfiguracion());
        
        btnRestablecer = new JButton("RESTABLECER");
        btnRestablecer.setBounds(240, 375, 130, 40);
        btnRestablecer.setFont(new Font("Arial", Font.BOLD, 14));
        btnRestablecer.setBackground(new Color(180, 100, 50));
        btnRestablecer.setForeground(Color.WHITE);
        btnRestablecer.setFocusPainted(false);
        btnRestablecer.setBorderPainted(false);
        add(btnRestablecer);
        
        btnRestablecer.addActionListener(e -> restablecerValores());
        
        btnVolver = new JButton("VOLVER");
        btnVolver.setBounds(380, 375, 130, 40);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 14));
        btnVolver.setBackground(new Color(150, 50, 50));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        add(btnVolver);
        
        btnVolver.addActionListener(e -> {
            // Revertir cambios realizados en la sesión de configuración y cerrar sin guardar
            revertirCambiosYCerrar();
        });
        
        agregarEfectoHover(btnGuardar, new Color(50, 150, 50), new Color(70, 200, 70));
        agregarEfectoHover(btnRestablecer, new Color(180, 100, 50), new Color(220, 140, 80));
        agregarEfectoHover(btnVolver, new Color(150, 50, 50), new Color(200, 80, 80));
    }
    
    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
    }
    
    public void setOwnerDialog(javax.swing.JDialog dialog) {
        this.ownerDialog = dialog;
    }
    
    public void setOnCloseCallback(Runnable r) {
        this.onCloseCallback = r;
    }
    public static boolean isPantallaCompleta() {
        return pantallaCompleta;
    }
    // Revierte los cambios hechos en la UI de configuración a los valores originales
    private void revertirCambiosYCerrar() {
        // Restaurar volúmenes de efectos
        try { Musica.setVolumenEfectos(originalVolumenEfectos); } catch (Exception ignore) {}

        // Restaurar pantalla completa y resolución internas y aplicar cambios
        resolucionAncho = originalResolucionAncho;
        resolucionAlto = originalResolucionAlto;
        try { aplicarCambiosInmediatos(); } catch (Exception ignore) {}

        // Cerrar la UI de configuración (overlay o diálogo) sin guardar
        try {
            if (onCloseCallback != null) {
                try { onCloseCallback.run(); } catch (Exception ignore) {}
            } else if (ownerDialog != null && ownerDialog.isShowing()) {
                try { ownerDialog.dispose(); } catch (Exception ignore) {}
                ownerDialog = null;
            } else {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null && window instanceof javax.swing.JDialog) {
                    try { window.dispose(); } catch (Exception ignore) {}
                } else {
                    if (parentFrame != null) {
                        try { parentFrame.revalidate(); parentFrame.repaint(); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private void agregarEfectoHover(JButton btn, Color colorNormal, Color colorHover) {
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(colorHover);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(colorNormal);
            }
        });
    }
    
    private void guardarConfiguracion() {
        // Leer el estado real de la casilla de pantalla completa
        volumenEfectos = sliderEfectos.getValue();
        String seleccion = (String) comboResoluciones.getSelectedItem();
        if (seleccion != null && seleccion.contains("x")) {
            String[] partes = seleccion.split("x");
            try {
                resolucionAncho = Integer.parseInt(partes[0]);
                resolucionAlto = Integer.parseInt(partes[1]);
                
                // 🔹 RECONFIGURAR ESCALA MANAGER
                escalaManager.configurarEscala(resolucionAncho, resolucionAlto);
                
            } catch (Exception ex) {
                System.err.println("Error al parsear resolución: " + ex.getMessage());
                resolucionAncho = 1366;
                resolucionAlto = 768;
            }
        }

        // Asegurarse de que Musica tenga los valores guardados (solo efectos)
        Musica.setVolumenEfectos(volumenEfectos);

        // Aplicar cambios inmediatamente
        aplicarCambiosInmediatos();
        try {
            if (onCloseCallback != null) {
                try { onCloseCallback.run(); } catch (Exception ignore) {}
            } else if (ownerDialog != null && ownerDialog.isShowing()) {
                try { ownerDialog.dispose(); } catch (Exception ignore) {}
                ownerDialog = null;
            } else {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null && window instanceof javax.swing.JDialog) {
                    try { window.dispose(); } catch (Exception ignore) {}
                } else {
                    // If it's the main frame (JFrame) or no window, just refresh the parent frame instead of disposing it
                    if (parentFrame != null) {
                        try { parentFrame.revalidate(); parentFrame.repaint(); } catch (Exception ignore) {}
                    }
                }
            }
        } catch (Exception ignore) {}

        // No mostrar diálogo; cerrar simplemente la UI de configuración
        return;
    }
    
    private void aplicarCambiosInmediatos() {
        escalaManager.configurarEscala(resolucionAncho, resolucionAlto);
        if (parentFrame == null) return;

        // Fast path: try to toggle fullscreen on the existing frame without recreating it.
        // This avoids disposing/creating frames which causes repaint flicker, especially on fullscreen.
        try {
            if (applyFullscreenToExistingFrame()) {
                // Volumen y demás ya aplicados por setters anteriores si corresponde.
                return;
            }
        } catch (Exception ignore) {}

        boolean dialogWasOpen = ownerDialog != null && ownerDialog.isShowing();
        // Si el diálogo estaba abierto, quitar el panel del diálogo y cerrarlo temporalmente
        if (dialogWasOpen) {
            try {
                ownerDialog.getContentPane().remove(this);
                ownerDialog.dispose();
            } catch (Exception ignore) {}
            ownerDialog = null;
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();

        // Guardar y remover el contenido existente (si hay) para reusarlo en el nuevo frame
        Component oldContent = null;
        try {
            if (parentFrame.getContentPane().getComponentCount() > 0) {
                oldContent = parentFrame.getContentPane().getComponent(0);
                parentFrame.getContentPane().remove(oldContent);
            }
        } catch (Exception ignore) {}
        // Cerrar frame actual
        parentFrame.dispose();

        // Crear nuevo frame con nueva configuración
        JFrame nuevoFrame = new JFrame("Proyecto Final");
        nuevoFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        nuevoFrame.setUndecorated(pantallaCompleta);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int targetWidth = resolucionAncho;
        int targetHeight = resolucionAlto;

        if (pantallaCompleta) {
            targetWidth = screenSize.width;
            targetHeight = screenSize.height;
        } else {
            targetWidth = Math.min(targetWidth, screenSize.width);
            targetHeight = Math.min(targetHeight, screenSize.height);
        }

        nuevoFrame.setSize(targetWidth, targetHeight);
        nuevoFrame.setLocationRelativeTo(null);
        // Reusar el contenido anterior para preservar estado si es posible
        if (oldContent != null) {
            try {
                nuevoFrame.getContentPane().add(oldContent);
            } catch (Exception ex) {
                nuevoFrame.getContentPane().add(new JPanel());
            }
        } else {
            nuevoFrame.getContentPane().add(new JPanel());
        }
         nuevoFrame.setResizable(false);

        if (pantallaCompleta && gd.isFullScreenSupported()) {
            nuevoFrame.setVisible(true);
            try {
                gd.setFullScreenWindow(nuevoFrame);
            } catch (Exception ex) {
                nuevoFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
            boolean ok = applyDisplayModeIfNeeded(nuevoFrame);
            if (!ok) {
                nuevoFrame.dispose();
                nuevoFrame = createBorderlessFullscreen(gd);
            }
        } else {
            if (pantallaCompleta) nuevoFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            nuevoFrame.setVisible(true);
        }

        applyDisplayModeIfNeeded(nuevoFrame);
        nuevoFrame.requestFocus();

        this.parentFrame = nuevoFrame;

        // Si el diálogo estaba abierto antes, volver a agregar este panel dentro de un nuevo diálogo sobre el nuevo frame
        if (dialogWasOpen) {
            try {
                javax.swing.JDialog newDialog = new javax.swing.JDialog(nuevoFrame);
                newDialog.setTitle("Configuración");
                newDialog.setUndecorated(true);
                newDialog.setDefaultCloseOperation(javax.swing.JDialog.DISPOSE_ON_CLOSE);
                newDialog.setSize(600, 450);
                newDialog.setLocationRelativeTo(nuevoFrame);
                newDialog.getContentPane().add(this);
                this.setOwnerDialog(newDialog);
                // Asegurar arrastre sobre el panel (this)
                final Point[] panelMouse = new Point[1];
                this.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) { panelMouse[0] = e.getPoint(); }
                });
                this.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        Point loc = e.getLocationOnScreen();
                        newDialog.setLocation(loc.x - panelMouse[0].x, loc.y - panelMouse[0].y);
                    }
                });
                // ESC para cerrar
                newDialog.getRootPane().registerKeyboardAction(ev -> { try { newDialog.dispose(); } catch (Exception ignore) {} }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
                newDialog.setVisible(true);
              } catch (Exception ex) {
                  System.out.println("[config] no se pudo reabrir el diálogo de configuración: " + ex.getMessage());
              }
          }
    }

    // Intenta aplicar fullscreen sobre el JFrame actualmente existente (sin recrearlo).
    // Retorna true si aplicó un cambio y evitó la recreación (fast-path), false para usar el fallback recreador.
    private boolean applyFullscreenToExistingFrame() {
        if (parentFrame == null) return false;
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();

            // Si estamos entrando en pantalla completa
            if (pantallaCompleta) {
                // Si ya está en full-screen, nada que hacer
                Window currentFull = gd.getFullScreenWindow();
                if (currentFull == parentFrame) return true;

                // Intentar poner la ventana actual en full-screen sin recrearla
                try {
                    // Some platforms work better if window is visible before setFullScreenWindow
                    parentFrame.setVisible(true);
                    if (gd.isFullScreenSupported()) {
                        gd.setFullScreenWindow(parentFrame);
                    } else {
                        parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }
                    return true;
                } catch (Exception ex) {
                    // fallback to recreate
                    return false;
                }
            } else {
                // Saliendo de pantalla completa
                try {
                    if (gd.getFullScreenWindow() == parentFrame) {
                        try { gd.setFullScreenWindow(null); } catch (Exception ignore) {}
                    }
                    // Restaurar tamaño de ventana a la resolución objetivo
                    parentFrame.setExtendedState(JFrame.NORMAL);
                    parentFrame.setSize(resolucionAncho, resolucionAlto);
                    parentFrame.setLocationRelativeTo(null);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    // Crea un JFrame borderless que ocupa toda la pantalla y devuelve el nuevo frame
    private JFrame createBorderlessFullscreen(GraphicsDevice gd) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
       JFrame frame = new JFrame("Proyecto Final");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setUndecorated(true);
         frame.setResizable(false);
        // No podemos instanciar ventanaInicio aquí (puede no estar compilada), usar un placeholder
         frame.getContentPane().add(new JPanel());
         frame.setBounds(0, 0, screenSize.width, screenSize.height);
       frame.setVisible(true);
        // intentar setFullScreenWindow si es soportado para mejorar compatibilidad
         try {
             if (gd != null && gd.isFullScreenSupported()) gd.setFullScreenWindow(frame);
         } catch (Exception ignore) {}
       return frame;
   }
    
    // Intenta cambiar la DisplayMode del GraphicsDevice a la resolución seleccionada
    // Hacemos pública la función para permitir llamadas desde otros lugares (p.ej. ventanaInicio)
    // Intenta aplicar/restore DisplayMode. Retorna true si cambió el modo (o restauró), false si no
    public static boolean applyDisplayModeIfNeeded(GraphicsDevice gd) {
         if (gd == null) return false;
         try {
            if (gd.isDisplayChangeSupported()) {
                if (pantallaCompleta) {
                    // Buscar el mejor DisplayMode disponible para la resolución solicitada
                    DisplayMode[] modes = gd.getDisplayModes();
                    DisplayMode best = null;
                    int bestScore = Integer.MAX_VALUE;
                    for (DisplayMode m : modes) {
                        int w = m.getWidth();
                        int h = m.getHeight();
                        if (w <= 0 || h <= 0) continue;
                        int score = Math.abs(w - resolucionAncho) + Math.abs(h - resolucionAlto);
                        // Preferir exactitud
                        if (w == resolucionAncho && h == resolucionAlto) {
                            best = m;
                            bestScore = 0;
                            break;
                        }
                        if (score < bestScore) {
                            bestScore = score;
                            best = m;
                        }
                    }
                    if (best != null) {
                        try {
                            if (previousDisplayMode == null) previousDisplayMode = gd.getDisplayMode();
                            System.out.println("[config] changing display mode to: " + best.getWidth() + "x" + best.getHeight() + " @" + best.getRefreshRate() + "Hz, bitDepth=" + best.getBitDepth());
                            gd.setDisplayMode(best);
                            return true;
                        } catch (Exception ex) {
                            // si falla, ignorar y seguir con el modo actual
                            System.out.println("[config] failed to set display mode: " + ex.getMessage());
                        }
                    }
                } else {
                    // Restaurar el modo anterior si existe
                    if (previousDisplayMode != null) {
                        try { gd.setFullScreenWindow(null); } catch (Exception ignore) {}
                        try {
                            System.out.println("[config] restoring previous display mode: " + previousDisplayMode.getWidth() + "x" + previousDisplayMode.getHeight());
                            gd.setDisplayMode(previousDisplayMode);
                            return true;
                        } catch (Exception ex) {
                            System.out.println("[config] failed to restore display mode: " + ex.getMessage());
                        }
                        previousDisplayMode = null;
                    }
                }
            }
         } catch (Exception ex) {
             // No hacemos nada si falla: fallback a full-screen sin cambiar display mode
             System.out.println("[config] applyDisplayModeIfNeeded exception: " + ex.getMessage());
         }
         return false;
      }
    
    // Sobrecarga: intenta aplicar el display mode en el GraphicsDevice asociado a la ventana
    // Retorna true si se pudo aplicar/restaurar, false si no
    public static boolean applyDisplayModeIfNeeded(java.awt.Window window) {
        if (window == null) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            return applyDisplayModeIfNeeded(gd);
        }
        GraphicsDevice gd = null;
        try {
            if (window.getGraphicsConfiguration() != null) {
                gd = window.getGraphicsConfiguration().getDevice();
            }
        } catch (Exception e) {
            gd = null;
        }
        if (gd == null) gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        return applyDisplayModeIfNeeded(gd);
    }
    
    private void restablecerValores() {
        sliderEfectos.setValue(50);
        comboResoluciones.setSelectedItem("1366x768"); // 🔹 Resolución base
        
        // Aplicar restablecimiento
        Musica.setVolumenEfectos(50);
        
        // 🔹 RESETEAR ESCALA A BASE
        resolucionAncho = 1366;
        resolucionAlto = 768;
        escalaManager.configurarEscala(resolucionAncho, resolucionAlto);
        
        JOptionPane.showMessageDialog(this, 
            "Valores restablecidos a predeterminados", 
            "Restablecer", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static int getResolucionAncho() {
        return resolucionAncho;
    }

    public static int getResolucionAlto() {
        return resolucionAlto;
    }
    
    public static void mostrarVentanaConfig(JFrame parentFrame) {
        // Mostrar la configuración como una superposición dentro del mismo JFrame (no crear otra ventana)
        if (parentFrame == null) return;

        config configPanel = new config();
        configPanel.setParentFrame(parentFrame);

        JLayeredPane layered = parentFrame.getLayeredPane();

        // Overlay que cubre toda la ventana con semitransparencia
        JComponent overlay = new JComponent() {};
        overlay.setLayout(null);
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, parentFrame.getWidth(), parentFrame.getHeight());

        // Fondo semitransparente
        JPanel background = new JPanel();
        background.setBackground(new Color(0,0,0,120));
        background.setBounds(0,0, parentFrame.getWidth(), parentFrame.getHeight());
        overlay.add(background);

        // Panel contenedor (imitando diálogo)
        JPanel container = new JPanel(null);
        container.setSize(600, 450);
        int cx = (parentFrame.getWidth() - 600) / 2;
        int cy = (parentFrame.getHeight() - 450) / 2;
        container.setBounds(cx, cy, 600, 450);
        container.setOpaque(true);
        container.setBackground(new Color(20,20,30));

        // Añadir borde suave
        container.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Añadir el panel de configuración dentro del contenedor
        configPanel.setBounds(0,0,600,450);
        container.add(configPanel);
        overlay.add(container);

        // Manejar arrastre sobre el panel (ya tiene listeners en el panel, pero aseguramos)
        final Point[] start = new Point[1];
        container.addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){ start[0] = e.getPoint(); }
        });
        container.addMouseMotionListener(new MouseMotionAdapter(){
            public void mouseDragged(MouseEvent e){
                Point loc = e.getLocationOnScreen();
                SwingUtilities.convertPointFromScreen(loc, parentFrame);
                int nx = loc.x - start[0].x;
                int ny = loc.y - start[0].y;
                container.setLocation(nx, ny);
            }
        });

        // ESC: cerrar overlay
        overlay.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0), "closeOverlay");
        overlay.getActionMap().put("closeOverlay", new AbstractAction(){ public void actionPerformed(ActionEvent e){
            layered.remove(overlay); layered.revalidate(); layered.repaint();
        }});

        // Set close callback for VOLVER button
        configPanel.setOnCloseCallback(() -> {
            layered.remove(overlay); layered.revalidate(); layered.repaint();
        });

        // If parent is resized, update overlay bounds and center the container
        ComponentListener cl = new ComponentAdapter(){
            @Override public void componentResized(ComponentEvent e){
                overlay.setBounds(0,0,parentFrame.getWidth(), parentFrame.getHeight());
                background.setBounds(0,0,parentFrame.getWidth(), parentFrame.getHeight());
                int nx = (parentFrame.getWidth()-container.getWidth())/2;
                int ny = (parentFrame.getHeight()-container.getHeight())/2;
                container.setLocation(nx, ny);
            }
        };
        parentFrame.addComponentListener(cl);

        // Add overlay to layered pane
        layered.add(overlay, JLayeredPane.POPUP_LAYER);
        layered.revalidate(); layered.repaint();

        // When overlay removed, unregister listener
        // We'll attach a simple property change on container visibility to cleanup
        container.addHierarchyListener(ev -> {
            if ((ev.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (!container.isShowing()) {
                    try { parentFrame.removeComponentListener(cl); } catch (Exception ignore) {}
                }
            }
        });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagenFondo != null) {
            g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        }
    }
}