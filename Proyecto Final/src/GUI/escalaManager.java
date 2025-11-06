package GUI;

import java.awt.Dimension;

/**
 * Sistema de escalado proporcional para múltiples resoluciones.
 * Usa 1366x768 como resolución base de referencia.
 */
public class escalaManager {
    
    // 🔹 RESOLUCIÓN BASE (la que estás usando para desarrollar)
    public static final int BASE_WIDTH = 1366;
    public static final int BASE_HEIGHT = 768;
    
    // Variables de escala actual
    private static double escalaX = 1.0;
    private static double escalaY = 1.0;
    private static double escalaUniforme = 1.0; // Para mantener proporciones
    
    private static int anchoActual = BASE_WIDTH;
    private static int altoActual = BASE_HEIGHT;
    
    /**
     * Configura la escala según la resolución actual
     */
    public static void configurarEscala(int width, int height) {
        anchoActual = width;
        altoActual = height;
        
        escalaX = (double) width / BASE_WIDTH;
        escalaY = (double) height / BASE_HEIGHT;
        
        // Escala uniforme (usa la menor para mantener proporciones sin deformar)
        escalaUniforme = Math.min(escalaX, escalaY);
        
        System.out.println("Escala configurada: " + width + "x" + height);
        System.out.println("Factor X: " + escalaX + ", Y: " + escalaY + ", Uniforme: " + escalaUniforme);
    }
    
    /**
     * Escala una coordenada X
     */
    public static int escalaX(int x) {
        return (int) Math.round(x * escalaX);
    }
    
    /**
     * Escala una coordenada Y
     */
    public static int escalaY(int y) {
        return (int) Math.round(y * escalaY);
    }
    
    /**
     * Escala un ancho manteniendo proporciones
     */
    public static int escalaAncho(int w) {
        return (int) Math.round(w * escalaX);
    }
    
    /**
     * Escala un alto manteniendo proporciones
     */
    public static int escalaAlto(int h) {
        return (int) Math.round(h * escalaY);
    }
    
    /**
     * Escala uniforme (mantiene proporciones para sprites/personajes)
     */
    public static int escalaUniforme(int valor) {
        return (int) Math.round(valor * escalaUniforme);
    }
    
    /**
     * Escala un tamaño de fuente
     */
    public static int escalaFuente(int fontSize) {
        return Math.max(8, (int) Math.round(fontSize * escalaUniforme));
    }
    
    /**
     * Obtiene un Dimension escalado
     */
    public static Dimension escalarDimension(int w, int h) {
        return new Dimension(escalaAncho(w), escalaAlto(h));
    }
    
    // Getters
    public static double getEscalaX() { return escalaX; }
    public static double getEscalaY() { return escalaY; }
    public static double getEscalaUniforme() { return escalaUniforme; }
    public static int getAnchoActual() { return anchoActual; }
    public static int getAltoActual() { return altoActual; }
    
    /**
     * Convierte coordenadas de pantalla a coordenadas del juego (útil para colisiones)
     */
    public static int xPantallaAJuego(int x) {
        return (int) Math.round(x / escalaX);
    }
    
    public static int yPantallaAJuego(int y) {
        return (int) Math.round(y / escalaY);
    }
}