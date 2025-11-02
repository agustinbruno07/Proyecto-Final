package GUI;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class colisionCalle {
    
    private BufferedImage mascaraColision;
    
    public colisionCalle(String rutaMascara) {
        try {
            File archivo = new File(rutaMascara);
            mascaraColision = ImageIO.read(archivo);
            
            
        } catch (Exception e) {
            System.err.println("Error al cargar máscara de colisión: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean hayColision(Rectangle jugadorBounds, int anchoVentana, int altoVentana) {
        if (mascaraColision == null) {
            return false;
        }

        double escalaX = (double) mascaraColision.getWidth() / anchoVentana;
        double escalaY = (double) mascaraColision.getHeight() / altoVentana;

        int xMascara = (int)(jugadorBounds.x * escalaX);
        int yMascara = (int)(jugadorBounds.y * escalaY);
        int anchoMascara = Math.max(1, (int)(jugadorBounds.width * escalaX));
        int altoMascara = Math.max(1, (int)(jugadorBounds.height * escalaY));

        int[][] puntosAVerificar = {
            { xMascara + anchoMascara/2, yMascara + altoMascara/2 }, // Centro
            { xMascara + 5, yMascara + 5 },                          // Sup izq
            { xMascara + anchoMascara - 5, yMascara + 5 },           // Sup der
            { xMascara + 5, yMascara + altoMascara - 5 },            // Inf izq
            { xMascara + anchoMascara - 5, yMascara + altoMascara - 5 } // Inf der
        };

        for (int[] punto : puntosAVerificar) {
            int px = punto[0];
            int py = punto[1];

            if (px >= 0 && px < mascaraColision.getWidth() &&
                py >= 0 && py < mascaraColision.getHeight()) {

                int argb = mascaraColision.getRGB(px, py);

                int alpha = (argb >> 24) & 0xFF;
                int red   = (argb >> 16) & 0xFF;
                int green = (argb >> 8)  & 0xFF;
                int blue  = argb & 0xFF;

                if (alpha == 0) {
                    return true;
                }

                int brillo = (red + green + blue) / 3;
                if (brillo > 128) {
                    return true;
                }
            }
        }

        return false; 
    }

}