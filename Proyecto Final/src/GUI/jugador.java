package GUI;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class jugador {

    public enum Direccion { UP, DOWN, LEFT, RIGHT }

    private static final int BASE_SPRITE_SIZE = 64;
    private static final int BASE_VELOCIDAD   = 5;

    private double escala = 1.0;

    
    private int spriteSize; 
    private int x, y;
    private int ancho, alto; 
    private int velocidad;
    private Direccion dir = Direccion.DOWN;

    private Image sprUp, sprDown, sprLeft, sprRight;

    public jugador(int startX, int startY) {
        this.spriteSize = escalaManager.escalaUniforme(BASE_SPRITE_SIZE);
        this.velocidad  = Math.max(1, escalaManager.escalaUniforme(BASE_VELOCIDAD));

        this.x = startX;
        this.y = startY;

       
        sprDown  = new ImageIcon("src/resources/images/bombritoAbajo.png").getImage();
        sprUp    = new ImageIcon("src/resources/images/bombritoArriba.png").getImage();
        sprLeft  = new ImageIcon("src/resources/images/bombritoIzquierda.png").getImage();
        sprRight = new ImageIcon("src/resources/images/bombritoDerecha.png").getImage();

        this.ancho = spriteSize; 
        this.alto  = spriteSize; 
    }

   
    public void setEscala(double escala) {
        if (escala <= 0) escala = 0.1;
        this.escala = escala;
    }
    public double getEscala() { return escala; }

   
    public void moveUp()    { y -= velocidad; dir = Direccion.UP; }
    public void moveDown()  { y += velocidad; dir = Direccion.DOWN; }
    public void moveLeft()  { x -= velocidad; dir = Direccion.LEFT; }
    public void moveRight() { x += velocidad; dir = Direccion.RIGHT; }


    public void clampTo(Rectangle worldBounds) {
        if (worldBounds == null) return;

        int w = getAncho(); 
        int h = getAlto();

        if (x < worldBounds.x) x = worldBounds.x;
        if (y < worldBounds.y) y = worldBounds.y;

        if (x + w > worldBounds.x + worldBounds.width)
            x = worldBounds.x + worldBounds.width - w;
        if (y + h > worldBounds.y + worldBounds.height)
            y = worldBounds.y + worldBounds.height - h;
    }

    public void update(Rectangle worldBounds) {
        clampTo(worldBounds);
    }

    public void draw(Graphics g) {
        Image img;
        switch (dir) {
            case UP:    img = sprUp; break;
            case DOWN:  img = sprDown; break;
            case LEFT:  img = sprLeft; break;
            case RIGHT: img = sprRight; break;
            default:    img = sprDown; break;
        }
        int w = getAncho(); 
        int h = getAlto();  
        g.drawImage(img, x, y, w, h, null);
    }

    public void setPosition(int x, int y) { this.x = x; this.y = y; }
    public void setVelocidad(int v) { this.velocidad = Math.max(1, v); }
    public int getX() { return x; }
    public int getY() { return y; }

    public int getAncho() { return (int) Math.round(spriteSize * escala); }
    public int getAlto()  { return (int) Math.round(spriteSize * escala); }

    public Rectangle getBounds() {
        return new Rectangle(x, y, getAncho(), getAlto()); 
    }

    public void reescalar() {
        this.spriteSize = escalaManager.escalaUniforme(BASE_SPRITE_SIZE);
        this.velocidad  = Math.max(1, escalaManager.escalaUniforme(BASE_VELOCIDAD));
        this.ancho = spriteSize; 
        this.alto  = spriteSize;
    }
}