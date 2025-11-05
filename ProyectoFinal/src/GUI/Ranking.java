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
        setLayout(null);

        try {
            imagenFondo = new ImageIcon("src/resources/images/fondo.png").getImage();
        } catch (Exception e) {
            imagenFondo = null;
        }

        JLabel lblTitulo = new JLabel("RANKING", JLabel.CENTER);
        lblTitulo.setBounds(200, 40, 800, 80);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 48));
        lblTitulo.setForeground(Color.YELLOW);
        add(lblTitulo);

        // Cargar ranking y mostrarlo
        java.util.List<RankingManager.Entry> entries = RankingManager.loadRanking();
        DefaultListModel<String> model = new DefaultListModel<>();
        int max = Math.min(entries.size(), 20);
        for (int i = 0; i < max; i++) {
            RankingManager.Entry en = entries.get(i);
            String line = String.format("%2d. %s - %s", i + 1, en.name, RankingManager.formatMillis(en.millis));
            model.addElement(line);
        }
        if (entries.isEmpty()) {
            model.addElement("(No hay resultados todavía)");
        }

        JList<String> list = new JList<>(model);
        list.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JScrollPane sp = new JScrollPane(list);
        sp.setBounds(250, 140, 600, 320);
        add(sp);

        JButton btnVolver = new JButton("Volver");
        btnVolver.setBounds(550, 400, 200, 50);
        btnVolver.setFont(new Font("Arial", Font.BOLD, 14));
        btnVolver.setBackground(new Color(50, 100, 200));
        btnVolver.setForeground(Color.WHITE);
        btnVolver.setFocusPainted(false);
        btnVolver.setBorderPainted(false);
        add(btnVolver);

        btnVolver.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnVolver.setBackground(new Color(70, 130, 230));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnVolver.setBackground(new Color(50, 100, 200));
            }
        });
        btnVolver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // 🔹 SONIDO AL PRESIONAR
                Musica.reproducir("src/resources/sonidos/sonidoInicio.wav");
                volverAVentanaInicio();
            }
        });
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
            setBackground(new Color(20, 20, 40));
        }
    }
}