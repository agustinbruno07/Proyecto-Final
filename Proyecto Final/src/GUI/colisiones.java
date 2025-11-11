package GUI;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class colisiones {
    
    private BufferedImage mascaraColision;
    private boolean obstacleIsBright = true;
    private static final int BRIGHTNESS_THRESHOLD = 128;
    
    private static final int BASE_MASCARA_WIDTH = 1366;
    private static final int BASE_MASCARA_HEIGHT = 768;
    
    public colisiones(String rutaMascara) {
        try {
            File archivo = new File(rutaMascara);
            mascaraColision = ImageIO.read(archivo);
            
            if (mascaraColision != null) {
                System.out.println("✅ Máscara de colisión cargada: " + mascaraColision.getWidth() + "x" + mascaraColision.getHeight());
                
                int w = mascaraColision.getWidth();
                int h = mascaraColision.getHeight();
                long brightCount = 0;
                long total = 0;
                int maxSamples = 200_000;
                int step = 1;
                long approx = (long) w * h;
                if (approx > maxSamples) {
                    step = (int) Math.max(1, Math.sqrt((double) (w * h) / maxSamples));
                }
                
                for (int y = 0; y < h; y += step) {
                    for (int x = 0; x < w; x += step) {
                        int argb = mascaraColision.getRGB(x, y);
                        int alpha = (argb >> 24) & 0xFF;
                        int red   = (argb >> 16) & 0xFF;
                        int green = (argb >> 8)  & 0xFF;
                        int blue  = argb & 0xFF;
                        int brillo = (red + green + blue) / 3;
                        
                        if (alpha == 0) {
                        } else {
                            if (brillo > BRIGHTNESS_THRESHOLD) brightCount++;
                            total++;
                        }
                    }
                }
                
                if (total > 0) {
                    double pctBright = (double) brightCount / total;
                    obstacleIsBright = pctBright < 0.5;
                    System.out.println("[colisiones] brightCount=" + brightCount + ", total=" + total + 
                                     ", pctBright=" + pctBright + ", obstacleIsBright=" + obstacleIsBright);
                } else {
                    obstacleIsBright = true;
                    System.out.println("[colisiones] muestreo inválido, usando fallback obstacleIsBright=true");
                }
            } else {
                System.err.println("❌ mascaraColision es NULL");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al cargar máscara de colisión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean hayColision(Rectangle jugadorBounds) {
        if (mascaraColision == null) {
            return false;
        }

     
        
        double escalaX = (double) mascaraColision.getWidth() / escalaManager.getAnchoActual();
        double escalaY = (double) mascaraColision.getHeight() / escalaManager.getAltoActual();

        int xMascara = (int) Math.round(jugadorBounds.x * escalaX);
        int yMascara = (int) Math.round(jugadorBounds.y * escalaY);
        int anchoMascara = Math.max(1, (int) Math.round(jugadorBounds.width * escalaX));
        int altoMascara = Math.max(1, (int) Math.round(jugadorBounds.height * escalaY));

        for (int x = xMascara; x < xMascara + anchoMascara; x++) {
            for (int y = yMascara; y < yMascara + altoMascara; y++) {
                if (x >= 0 && x < mascaraColision.getWidth() &&
                    y >= 0 && y < mascaraColision.getHeight()) {

                    int argb = mascaraColision.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;

                    if (alpha == 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    @Deprecated
    public boolean hayColision(Rectangle jugadorBounds, int anchoVentana, int altoVentana) {
        return hayColision(jugadorBounds);
    }
}