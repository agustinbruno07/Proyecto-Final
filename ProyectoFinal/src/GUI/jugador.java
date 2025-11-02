package GUI;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class jugador {

    public enum Direccion { UP, DOWN, LEFT, RIGHT }

    // 🔹 VALORES BASE (en resolución 1366x768)
    private static final int BASE_SPRITE_SIZE = 64;
    private static final int BASE_VELOCIDAD = 5;
    
    // Variables escaladas
    private int spriteSize;
    private int x, y;
    private int ancho, alto;
    private int velocidad;
    private Direccion dir = Direccion.DOWN;

    private Image sprUp, sprDown, sprLeft, sprRight;

    public jugador(int startX, int startY) {
        // 🔹 ESCALAR SPRITE Y VELOCIDAD
        this.spriteSize = escalaManager.escalaUniforme(BASE_SPRITE_SIZE);
        this.velocidad = Math.max(1, escalaManager.escalaUniforme(BASE_VELOCIDAD));
        
        // 🔹 POSICIÓN INICIAL (puede venir ya escalada o escalarla aquí)
        this.x = startX;
        this.y = startY;

        // Cargar sprites
        sprDown  = new ImageIcon("src/resources/images/bombritoAbajo.png").getImage();
        sprUp    = new ImageIcon("src/resources/images/bombritoArriba.png").getImage();
        sprLeft  = new ImageIcon("src/resources/images/bombritoIzquierda.png").getImage();
        sprRight = new ImageIcon("src/resources/images/bombritoDerecha.png").getImage();

        this.ancho = spriteSize;
        this.alto = spriteSize;
    }

    public void moveUp()    { y -= velocidad; dir = Direccion.UP; }
    public void moveDown()  { y += velocidad; dir = Direccion.DOWN; }
    public void moveLeft()  { x -= velocidad; dir = Direccion.LEFT; }
    public void moveRight() { x += velocidad; dir = Direccion.RIGHT; }

    public void clampTo(Rectangle worldBounds) {
        if (worldBounds == null) return;

        if (x < worldBounds.x) x = worldBounds.x;
        if (y < worldBounds.y) y = worldBounds.y;

        if (x + ancho > worldBounds.x + worldBounds.width)
            x = worldBounds.x + worldBounds.width - ancho;
        if (y + alto > worldBounds.y + worldBounds.height)
            y = worldBounds.y + worldBounds.height - alto;
    }

    public void update(Rectangle worldBounds) {
        clampTo(worldBounds);
    }

    public void draw(Graphics g) {
        Image img = switch (dir) {
            case UP    -> sprUp;
            case DOWN  -> sprDown;
            case LEFT  -> sprLeft;
            case RIGHT -> sprRight;
        };
        // 🔹 DIBUJAR CON TAMAÑO ESCALADO
        g.drawImage(img, x, y, spriteSize, spriteSize, null);
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setVelocidad(int v) { this.velocidad = Math.max(1, v); }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public Rectangle getBounds() { return new Rectangle(x, y, spriteSize, spriteSize); }
    
    // 🔹 MÉTODO ÚTIL PARA REESCALAR SI CAMBIA LA RESOLUCIÓN EN RUNTIME
    public void reescalar() {
        this.spriteSize = escalaManager.escalaUniforme(BASE_SPRITE_SIZE);
        this.velocidad = Math.max(1, escalaManager.escalaUniforme(BASE_VELOCIDAD));
        this.ancho = spriteSize;
        this.alto = spriteSize;
    }
}