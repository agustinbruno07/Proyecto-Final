package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

        JLabel lblProximamente = new JLabel("PROXIMAMENTE...", JLabel.CENTER);
        lblProximamente.setBounds(250, 200, 800, 80);
        lblProximamente.setFont(new Font("Arial", Font.BOLD, 48));
        lblProximamente.setForeground(Color.YELLOW);
        add(lblProximamente);

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