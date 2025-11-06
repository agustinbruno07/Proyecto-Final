import GUI.ventanaInicio;
import java.awt.EventQueue;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ventanaInicio.mostrarVentana();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}