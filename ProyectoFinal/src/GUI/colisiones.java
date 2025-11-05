package GUI;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class colisiones {

    private BufferedImage mascaraColision;
    private boolean obstacleIsBright = true;
    private static final int BRIGHTNESS_THRESHOLD = 128;

    private double factorMascaraDesdeBaseX = 1.0;
    private double factorMascaraDesdeBaseY = 1.0;

    // 🔹 RESOLUCIÓN BASE DE LA MÁSCARA (debe coincidir con la resolución base del juego)
    private static final int BASE_MASCARA_WIDTH = 1366;
    private static final int BASE_MASCARA_HEIGHT = 768;

    public colisiones(String rutaMascara) {
        try {
            String rutaPreferida = escalaManager.resolverRecursoPorResolucion(
                escalaManager.getAnchoActual(),
                escalaManager.getAltoActual(),
                rutaMascara
            );

            File archivo = new File(rutaPreferida);
            if (!archivo.exists()) {
                rutaPreferida = escalaManager.resolverRecursoPorResolucion(rutaMascara);
                archivo = new File(rutaPreferida);
            }

            if (!archivo.exists()) {
                archivo = new File(rutaMascara);
                rutaPreferida = archivo.getPath();
            }

            mascaraColision = ImageIO.read(archivo);

            if (mascaraColision != null) {
                factorMascaraDesdeBaseX = (double) mascaraColision.getWidth() / BASE_MASCARA_WIDTH;
                factorMascaraDesdeBaseY = (double) mascaraColision.getHeight() / BASE_MASCARA_HEIGHT;

                System.out.println("✅ Máscara de colisión cargada: " + mascaraColision.getWidth() + "x" + mascaraColision.getHeight());
                System.out.println("[colisiones] Ruta preferida: " + rutaPreferida + " (original: " + rutaMascara + ")");

                // Detectar automáticamente si la máscara marca obstáculos con píxeles claros o oscuros
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
                            // ignorar píxeles transparentes
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

        if (mascaraColision.getWidth() <= 0 || mascaraColision.getHeight() <= 0) {
            return false;
        }

        escalaManager.ResolucionPerfil perfil = escalaManager.getResolucionActual();
        double factorBaseAX = (perfil != null) ? perfil.getEscalaX() : (double) escalaManager.getAnchoActual() / BASE_MASCARA_WIDTH;
        double factorBaseAY = (perfil != null) ? perfil.getEscalaY() : (double) escalaManager.getAltoActual() / BASE_MASCARA_HEIGHT;

        double actualABaseX = factorBaseAX == 0 ? 1.0 : 1.0 / factorBaseAX;
        double actualABaseY = factorBaseAY == 0 ? 1.0 : 1.0 / factorBaseAY;

        int baseX = (int) Math.round(jugadorBounds.x * actualABaseX);
        int baseY = (int) Math.round(jugadorBounds.y * actualABaseY);
        int baseWidth = Math.max(1, (int) Math.round(jugadorBounds.width * actualABaseX));
        int baseHeight = Math.max(1, (int) Math.round(jugadorBounds.height * actualABaseY));

        // 🔹 CALCULAR ESCALA ENTRE LA VENTANA ACTUAL Y LA MÁSCARA BASE
        // La máscara puede existir en distintas resoluciones; convertimos desde la resolución base
        int xMascara = (int) Math.round(baseX * factorMascaraDesdeBaseX);
        int yMascara = (int) Math.round(baseY * factorMascaraDesdeBaseY);
        int anchoMascara = Math.max(1, (int) Math.round(baseWidth * factorMascaraDesdeBaseX));
        int altoMascara = Math.max(1, (int) Math.round(baseHeight * factorMascaraDesdeBaseY));

        // Verificar cada píxel dentro del área del jugador
        for (int x = xMascara; x < xMascara + anchoMascara; x++) {
            for (int y = yMascara; y < yMascara + altoMascara; y++) {
                // Comprobar límites de la máscara
                if (x >= 0 && x < mascaraColision.getWidth() &&
                    y >= 0 && y < mascaraColision.getHeight()) {

                    int argb = mascaraColision.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;

                    // 🔹 Si el píxel es completamente transparente, hay colisión
                    if (alpha == 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    
    // 🔹 SOBRECARGA: versión antigua que acepta anchoVentana/altoVentana (deprecated)
    @Deprecated
    public boolean hayColision(Rectangle jugadorBounds, int anchoVentana, int altoVentana) {
        // Redireccionar a la nueva versión
        return hayColision(jugadorBounds);
    }
}