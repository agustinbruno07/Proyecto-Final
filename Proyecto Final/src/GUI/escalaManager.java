package GUI;

import java.awt.Dimension;


public class escalaManager {
    
    public static final int BASE_WIDTH = 1366;
    public static final int BASE_HEIGHT = 768;
    
    private static double escalaX = 1.0;
    private static double escalaY = 1.0;
    private static double escalaUniforme = 1.0; 
    
    private static int anchoActual = BASE_WIDTH;
    private static int altoActual = BASE_HEIGHT;
   
    public static void configurarEscala(int width, int height) {
        anchoActual = width;
        altoActual = height;
        
        escalaX = (double) width / BASE_WIDTH;
        escalaY = (double) height / BASE_HEIGHT;
        
        escalaUniforme = Math.min(escalaX, escalaY);
        
        System.out.println("Escala configurada: " + width + "x" + height);
        System.out.println("Factor X: " + escalaX + ", Y: " + escalaY + ", Uniforme: " + escalaUniforme);
    }
    
 
    public static int escalaX(int x) {
        return (int) Math.round(x * escalaX);
    }
    
  
  
    public static int escalaY(int y) {
        return (int) Math.round(y * escalaY);
    }
    
  
    public static int escalaAncho(int w) {
        return (int) Math.round(w * escalaX);
    }
    
  
    public static int escalaAlto(int h) {
        return (int) Math.round(h * escalaY);
    }
    
  
    public static int escalaUniforme(int valor) {
        return (int) Math.round(valor * escalaUniforme);
    }
    
   
    public static int escalaFuente(int fontSize) {
        return Math.max(8, (int) Math.round(fontSize * escalaUniforme));
    }
    
   
    public static Dimension escalarDimension(int w, int h) {
        return new Dimension(escalaAncho(w), escalaAlto(h));
    }
    
    public static double getEscalaX() { return escalaX; }
    public static double getEscalaY() { return escalaY; }
    public static double getEscalaUniforme() { return escalaUniforme; }
    public static int getAnchoActual() { return anchoActual; }
    public static int getAltoActual() { return altoActual; }
    
   
    public static int xPantallaAJuego(int x) {
        return (int) Math.round(x / escalaX);
    }
    
    public static int yPantallaAJuego(int y) {
        return (int) Math.round(y / escalaY);
    }
}