package GUI;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import GUI.Musica;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

public class ventanaInicio extends JPanel {
    private Image imagenFondo;
    private JFrame parentFrame;
    private Map<String, ImageIcon> iconCache = new HashMap<>();
    private boolean imagenesListas = false;

    public ventanaInicio(JFrame frame) {
        this.parentFrame = frame;
        setLayout(null);
        setBackground(Color.BLACK);
        imagenFondo = null;
        
        Dimension parentSize = parentFrame.getSize();
        int width = parentSize.width <= 0 ? config.getResolucionAncho() : parentSize.width;
        int height = parentSize.height <= 0 ? config.getResolucionAlto() : parentSize.height;
        
        if (width > 0 && height > 0) setPreferredSize(new Dimension(width, height));

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (imagenesListas) {
                    Dimension d = getSize();
                    createOrUpdateUI(d.width, d.height);
                }
            }
        });
    }

    public void iniciarCarga() {
        SwingUtilities.invokeLater(() -> {
            Dimension d = getSize();
            cargarImagenesEnBackground(d.width, d.height);
        });
    }

    private void cargarImagenesEnBackground(int width, int height) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Calcular escalado
                final int BASE_W = 1366;
                final int BASE_H = 768;
                double scale = Math.min((double) width / BASE_W, (double) height / BASE_H);
                if (scale <= 0) scale = 1.0;
                int buttonWidth = Math.max(120, (int) Math.round(300 * scale));
                int buttonHeight = Math.max(32, (int) Math.round(46 * scale));

                // Cargar fondo
                try {
                    if (getClass().getResource("/resources/images/fondo.png") != null) {
                        imagenFondo = new ImageIcon(getClass().getResource("/resources/images/fondo.png")).getImage();
                    } else {
                        imagenFondo = new ImageIcon("src/resources/images/fondo.png").getImage();
                    }
                } catch (Exception ex) {
                    imagenFondo = null;
                }


                cargarIcono("config", buttonWidth, buttonHeight);
                cargarIcono("inicio", buttonWidth, buttonHeight);
                cargarIcono("salir", buttonWidth, buttonHeight);
                cargarIcono("ranking", buttonWidth, buttonHeight);

                return null;
            }

            @Override
            protected void done() {
                imagenesListas = true;
                createOrUpdateUI(width, height);
                
                try {
                    Musica.reproducir("src/resources/sonidos/musicaFondo.wav");
                } catch (Exception e) {
                    System.err.println("Error al reproducir música de fondo: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }

    private void cargarIcono(String nombre, int width, int height) {
        try {
            ImageIcon icon;
            if (getClass().getResource("/resources/images/" + nombre + ".png") != null) {
                icon = new ImageIcon(getClass().getResource("/resources/images/" + nombre + ".png"));
            } else {
                icon = new ImageIcon("src/resources/images/" + nombre + ".png");
            }
            
            if (icon.getImage() != null) {
                Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                iconCache.put(nombre, new ImageIcon(img));
            }
        } catch (Exception e) {
            iconCache.put(nombre, new ImageIcon());
        }
    }

    private void createOrUpdateUI(int width, int height) {
        removeAll();
        if (width > 0 && height > 0) setPreferredSize(new Dimension(width, height));

        // Escalado relativo
        int buttonWidth = escalaManager.escalaAncho(300);
        int buttonHeight = escalaManager.escalaAlto(46);
        int startX = (width - buttonWidth) / 2;
        int startY = escalaManager.escalaY(345); // ~45% de 768
        int spacing = escalaManager.escalaY(10);

        // Botón Iniciar
        JButton btnIniciar = new JButton();
        btnIniciar.setBounds(startX, startY, buttonWidth, buttonHeight);
        btnIniciar.setIcon(iconCache.getOrDefault("inicio", new ImageIcon()));
        btnIniciar.setBorderPainted(false);
        btnIniciar.setContentAreaFilled(false);
        btnIniciar.setFocusPainted(false);
        btnIniciar.setOpaque(false);
        add(btnIniciar);

        btnIniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Musica.reproducir("src/resources/sonidos/inicio.wav");
                
                // Pedir nombre del jugador antes de iniciar los diálogos
                String nombre = javax.swing.JOptionPane.showInputDialog(parentFrame, "Ingrese su nombre:", "Nombre del jugador", javax.swing.JOptionPane.PLAIN_MESSAGE);
                if (nombre == null) { // si cancela, asignar un nombre por defecto
                    nombre = "Jugador";
                }
                EstadoJuego.setPlayerName(nombre);

                // Reiniciar y arrancar temporizador global
                EstadoJuego.resetTimer();
                EstadoJuego.startTimer();
                EstadoJuego.createTimerOverlayIfNeeded(parentFrame);

                parentFrame.getContentPane().removeAll();
                parentFrame.getContentPane().add(new dialogo1(parentFrame)); 
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        // Botón Config
        JButton btnConfig = new JButton();
        int scale = 0;
		btnConfig.setBounds(startX, startY + buttonHeight + (int)(10 * scale), buttonWidth, buttonHeight);
        btnConfig.setIcon(iconCache.getOrDefault("config", new ImageIcon()));
        btnConfig.setBorderPainted(false);
        btnConfig.setContentAreaFilled(false);
        btnConfig.setFocusPainted(false);
        btnConfig.setOpaque(false);
        add(btnConfig);

        btnConfig.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Musica.reproducir("src/resources/sonidos/inicio.wav");
                config.mostrarVentanaConfig(parentFrame);
            }
        });

        // Botón Ranking
        JButton btnRanking = new JButton();
        btnRanking.setBounds(startX, startY + 2*(buttonHeight + (int)(10 * scale)), buttonWidth, buttonHeight);
        btnRanking.setIcon(iconCache.getOrDefault("ranking", new ImageIcon()));
        btnRanking.setBorderPainted(false);
        btnRanking.setContentAreaFilled(false);
        btnRanking.setFocusPainted(false);
        btnRanking.setOpaque(false);
        add(btnRanking);

        btnRanking.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 🔹 SONIDO DE BOTÓN
                Musica.reproducir("src/resources/sonidos/inicio.wav");
                parentFrame.getContentPane().removeAll();
                parentFrame.getContentPane().add(new Ranking(parentFrame));
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });

        // Botón Salir
        JButton btnSalir = new JButton();
        btnSalir.setBounds(startX, startY + 3*(buttonHeight + (int)(10 * scale)), buttonWidth, buttonHeight);
        btnSalir.setIcon(iconCache.getOrDefault("salir", new ImageIcon()));
        btnSalir.setBorderPainted(false);
        btnSalir.setContentAreaFilled(false);
        btnSalir.setFocusPainted(false);
        btnSalir.setOpaque(false);
        add(btnSalir);

        btnSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Musica.reproducir("src/resources/sonidos/inicio.wav");
                System.exit(0);
            }
        });

        setFocusable(true);
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imagenFondo != null) {
            g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public static void mostrarVentana() {
        JFrame frame = new JFrame("Proyecto Final");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        boolean pantalla = config.isPantallaCompleta();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        int width = config.getResolucionAncho();
        int height = config.getResolucionAlto();
        escalaManager.configurarEscala(width, height);
        ventanaInicio panel = new ventanaInicio(frame);
        
        if (pantalla) {
            frame.setUndecorated(true);
            width = screenSize.width;
            height = screenSize.height;
            panel.setPreferredSize(new Dimension(width, height));
            frame.getContentPane().add(panel);
            frame.pack();
            
            frame.setVisible(true);

            
            if (gd.isFullScreenSupported()) {
                try {
                    gd.setFullScreenWindow(frame);
                    config.applyDisplayModeIfNeeded(gd);
                } catch (Exception e) {
                    System.err.println("Error al establecer pantalla completa: " + e.getMessage());
                }
            } else {
                frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            }
        } else {
            frame.setUndecorated(false);
            frame.setResizable(true);
            width = Math.min(width, screenSize.width);
            height = Math.min(height, screenSize.height);
            panel.setPreferredSize(new Dimension(width, height));
            frame.getContentPane().add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            
            frame.setVisible(true);

        }
        
        frame.requestFocus();
        
        panel.iniciarCarga();
    }
}