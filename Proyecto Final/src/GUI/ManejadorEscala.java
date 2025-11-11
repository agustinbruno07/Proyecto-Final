package GUI;

public class ManejadorEscala {
    private static final int BASE_WIDTH = 1366;
    private static final int BASE_HEIGHT = 768;
    
    private static int currentWidth = BASE_WIDTH;
    private static int currentHeight = BASE_HEIGHT;
    
    
    public static void setResolution(int width, int height) {
        currentWidth = width;
        currentHeight = height;
    }
    
    
    public static double getScaleX() {
        return (double) currentWidth / BASE_WIDTH;
    }
    
    
    public static double getScaleY() {
        return (double) currentHeight / BASE_HEIGHT;
    }
    
    
    public static int scaleX(int x) {
        return (int) Math.round(x * getScaleX());
    }
    
    
    public static int scaleY(int y) {
        return (int) Math.round(y * getScaleY());
    }
    
    
    public static int scaleWidth(int width) {
        return (int) Math.round(width * getScaleX());
    }
    
   
    public static int scaleHeight(int height) {
        return (int) Math.round(height * getScaleY());
    }
   
    public static java.awt.Rectangle scaleBounds(int x, int y, int width, int height) {
        return new java.awt.Rectangle(
            scaleX(x),
            scaleY(y),
            scaleWidth(width),
            scaleHeight(height)
        );
    }
    
    
    public static java.awt.Rectangle scaleBounds(java.awt.Rectangle bounds) {
        return scaleBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    
    public static int getCurrentWidth() {
        return currentWidth;
    }
    
    
    public static int getCurrentHeight() {
        return currentHeight;
    }
    
    
    public static int scaleFont(int fontSize) {
        double avgScale = (getScaleX() + getScaleY()) / 2.0;
        return Math.max(1, (int) Math.round(fontSize * avgScale));
    }
}