package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Ranking extends JPanel {
    private Image imagenFondo;
    private JFrame parentFrame;

    public Ranking(JFrame frame) {
        this.parentFrame = frame;
        // Usar BorderLayout para ocupar todo el espacio del contenedor
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        try {
            imagenFondo = new ImageIcon("src/resources/images/fondo.png").getImage();
        } catch (Exception e) {
            imagenFondo = null;
        }

        // TITULO centrado en la parte superior
        JLabel title = new JLabel("Ranking", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setForeground(Color.RED);
        title.setOpaque(false);
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Panel central con todo centrado verticalmente
        JPanel center = new JPanel();
        center.setBackground(Color.BLACK);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(10, 50, 30, 50));

        // Obtener registros y mostrarlos en un JTextArea (texto rojo, fondo negro)
        List<String> records = RankingManager.getFormattedRecordsDesc();

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String r : records) listModel.addElement(r);

        JList<String> list = new JList<>(listModel);
        list.setBackground(Color.BLACK);
        list.setForeground(Color.RED);
        list.setFont(new Font("Arial", Font.BOLD, 28));
        list.setOpaque(true);
        list.setFocusable(false);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Renderer centrado
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
                lbl.setBackground(Color.BLACK);
                lbl.setForeground(Color.RED);
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBackground(Color.BLACK);
        scroll.getViewport().setBackground(Color.BLACK);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Prefer tamaño relativo a la ventana para ocupar buena parte de la pantalla
        Dimension scrollPref = computeScrollPreferredSize(frame);
        scroll.setPreferredSize(scrollPref);
        scroll.setMaximumSize(new Dimension(scrollPref.width, scrollPref.height));

        // Centrar el scroll y añadir separación
        center.add(Box.createVerticalGlue());
        center.add(scroll);
        center.add(Box.createRigidArea(new Dimension(0, 20)));

        // Botón Volver centrado
        JButton btnVolver = new JButton("Volver");
        btnVolver.setFont(new Font("Arial", Font.BOLD, 18));
        // Ahora el botón es rojo con texto negro (y hover en rojo claro)
        btnVolver.setBackground(new Color(180, 20, 20));
        btnVolver.setForeground(Color.BLACK);
        btnVolver.setOpaque(true);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        btnVolver.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnVolver.setPreferredSize(new Dimension(200, 50));
        btnVolver.setMaximumSize(new Dimension(200, 50));

        btnVolver.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnVolver.setBackground(new Color(220, 60, 60));
                btnVolver.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnVolver.setBackground(new Color(180, 20, 20));
                btnVolver.setForeground(Color.BLACK);
            }
        });
        btnVolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Musica.reproducir("src/resources/sonidos/sonidoInicio.wav");
                volverAVentanaInicio();
            }
        });

        center.add(btnVolver);
        center.add(Box.createVerticalGlue());

        add(center, BorderLayout.CENTER);

        // Ajustar prefered size para ocupar toda la pantalla si el frame está disponible
        if (parentFrame != null) {
            Dimension d = parentFrame.getSize();
            if (d.width > 0 && d.height > 0) setPreferredSize(d);
        }
    }

    private Dimension computeScrollPreferredSize(JFrame frame) {
        Dimension screen = (frame != null && frame.getSize().width > 0) ? frame.getSize() : Toolkit.getDefaultToolkit().getScreenSize();
        int w = (int) (screen.width * 0.6);
        int h = (int) (screen.height * 0.6);
        return new Dimension(w, h);
    }

    private void volverAVentanaInicio() {
        parentFrame.getContentPane().removeAll();

        // 🔹 CREAR NUEVA INSTANCIA DE VENTANA INICIO
        ventanaInicio panelInicio = new ventanaInicio(parentFrame);
        parentFrame.getContentPane().add(panelInicio);

        parentFrame.revalidate();
        parentFrame.repaint();

        // 🔹 IMPORTANTE: INICIAR LA CARGA DE IMÁGENES
        panelInicio.iniciarCarga();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (imagenFondo != null) {
            g.drawImage(imagenFondo, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fondo negro liso
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}