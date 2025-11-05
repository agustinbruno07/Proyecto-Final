package GUI;

import java.awt.Rectangle;
import java.util.Random;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

public class SistemaSpawnJuego {
    
    private static final Random random = new Random();
   
    private static final Rectangle[] SPAWN_ZONAS_CALLE = {
        new Rectangle(100, 500, 200, 100),   
        new Rectangle(600, 500, 200, 100),    
        new Rectangle(1066, 500, 200, 100)    
    };
    
    private static final Rectangle[] SPAWN_ZONAS_CASA_PRINCIPAL = {
        new Rectangle(300, 600, 200, 100),    
        new Rectangle(600, 600, 200, 100),   
        new Rectangle(900, 600, 200, 100)     
    };
    
    private static final Rectangle[] SPAWN_ZONAS_PASILLO1 = {
        new Rectangle(200, 500, 150, 100),   
        new Rectangle(600, 500, 150, 100),    
        new Rectangle(1000, 500, 150, 100)    
    };
    
    private static final Rectangle[] SPAWN_ZONAS_PASILLO2 = {
        new Rectangle(200, 500, 150, 100),
        new Rectangle(600, 500, 150, 100), 
        new Rectangle(1000, 500, 150, 100)
    };
    
    private static final Rectangle[] SPAWN_ZONAS_COMEDOR = {
        new Rectangle(300, 500, 200, 100),    
        new Rectangle(600, 500, 200, 100),    
        new Rectangle(900, 500, 200, 100)     
    };

 

    /**
     * Obtiene una posici�n de spawn aleatoria que evite colisiones
     */
    public static int[] obtenerSpawnSeguro(String tipoEscena, colisiones sistemaColisiones) {
        // Mantener compatibilidad: delegar a la nueva sobrecarga (esObjeto = false)
        return obtenerSpawnSeguro(tipoEscena, sistemaColisiones, false);
    }

    /**
     * Nueva sobrecarga: si esObjeto == true, limita las zonas a las permitidas para objetos
     */
    

    public static int[] obtenerSpawnSeguro(String tipoEscena, colisiones sistemaColisiones, boolean esObjeto) {
        Rectangle[] zonasSpawn = esObjeto ? obtenerZonasSpawnParaObjetos(tipoEscena) : obtenerZonasSpawn(tipoEscena);
        int intentosMaximos = 10;

        if (zonasSpawn == null || zonasSpawn.length == 0) {
            // Si estamos pidiendo spawn para un objeto y no hay zonas válidas, retornar null
            if (esObjeto) return null;
            Rectangle zona = new Rectangle(100, 100, 200, 100);
            return new int[]{zona.x + zona.width/2, zona.y + zona.height/2};
        }

        for (int intento = 0; intento < intentosMaximos; intento++) {
            Rectangle zona = zonasSpawn[random.nextInt(zonasSpawn.length)];
            int x = zona.x + random.nextInt(zona.width);
            int y = zona.y + random.nextInt(zona.height);

            Rectangle boundsJugador = new Rectangle(x, y, 50, 50);
            if (!sistemaColisiones.hayColision(boundsJugador)) {
                return new int[]{x, y};
            }
        }

        Rectangle zona = zonasSpawn[0];
        return new int[]{zona.x + zona.width/2, zona.y + zona.height/2};
    }

    // Nuevo método: devuelve una posición aleatoria dentro de las zonas de spawn
    // Si esObjeto == true usa las zonas permitidas para objetos; si no, usa las zonas normales.
    // Retorna null si no hay zonas válidas para objetos (comportamiento esperado por los llamantes).
    public static int[] obtenerSpawnAleatorio(String tipoEscena, boolean esObjeto) {
        Rectangle[] zonas = esObjeto ? obtenerZonasSpawnParaObjetos(tipoEscena) : obtenerZonasSpawn(tipoEscena);
        if (zonas == null || zonas.length == 0) {
            if (esObjeto) return null;
            // devolver posición por defecto en caso de no haber zonas
            Rectangle zonaDef = new Rectangle(100, 100, 200, 100);
            return new int[]{zonaDef.x + zonaDef.width/2, zonaDef.y + zonaDef.height/2};
        }
        Rectangle zona = zonas[random.nextInt(zonas.length)];
        int x = zona.x + random.nextInt(Math.max(1, zona.width));
        int y = zona.y + random.nextInt(Math.max(1, zona.height));
        return new int[]{x, y};
    }

    // Conveniencia: versión sin el parámetro esObjeto (comportamiento por defecto: esObjeto = false)
    public static int[] obtenerSpawnAleatorio(String tipoEscena) {
        return obtenerSpawnAleatorio(tipoEscena, false);
    }

    // Método nuevo: devuelve sólo las zonas permitidas para objetos
    public static Rectangle[] obtenerZonasSpawnParaObjetos(String tipoEscena) {
        switch (tipoEscena.toLowerCase()) {
            case "comedor":
                return SPAWN_ZONAS_COMEDOR;
            case "casaprincipal":
            case "casa_principal":
                return SPAWN_ZONAS_CASA_PRINCIPAL;
            case "habitacion1":
                return new Rectangle[]{new Rectangle(600, 600, 200, 100)};
            case "habitacion2":
                return new Rectangle[]{new Rectangle(600, 600, 200, 100)};
            case "laberinto":
                return new Rectangle[]{new Rectangle(580, 600, 200, 100)};
            default:
                // Para cualquier otra escena (incluyendo los pasillos y casas laterales)
                // devolvemos un arreglo vacío para indicar que no hay zonas válidas para objetos
                return new Rectangle[0];
         }
     }
    
    private static Rectangle[] obtenerZonasSpawn(String tipoEscena) {
        switch (tipoEscena.toLowerCase()) {
            case "calle":
                return SPAWN_ZONAS_CALLE;
            case "casaprincipal":
            case "casa_principal":
                return SPAWN_ZONAS_CASA_PRINCIPAL;
            case "pasillo1":
                return SPAWN_ZONAS_PASILLO1;
            case "pasillo2": 
                return SPAWN_ZONAS_PASILLO2;
            case "comedor":
                return SPAWN_ZONAS_COMEDOR;
            case "habitacion1":
                return new Rectangle[]{new Rectangle(600, 600, 200, 100)};
            case "habitacion2":
                return new Rectangle[]{new Rectangle(600, 600, 200, 100)};
            case "casaizquierda":
                return new Rectangle[]{new Rectangle(300, 600, 200, 100)};
            case "casaderecha":
                return new Rectangle[]{new Rectangle(1000, 600, 200, 100)};
            case "laberinto":
                return new Rectangle[]{new Rectangle(580, 600, 200, 100)};
            default:
                return new Rectangle[]{new Rectangle(100, 100, 200, 100)}; 
        }
    }
    
    public static boolean esPosicionValida(int x, int y, colisiones sistemaColisiones) {
        Rectangle bounds = new Rectangle(x, y, 50, 50);
        return !sistemaColisiones.hayColision(bounds);
    }
    
    public static void dibujarZonasSpawn(Graphics g, String tipoEscena) {
        Rectangle[] zonas = obtenerZonasSpawn(tipoEscena);
        Graphics2D g2 = (Graphics2D) g;

        for (Rectangle zona : zonas) {
            
            int x = escalaManager.escalaX(zona.x);
            int y = escalaManager.escalaY(zona.y);
            int width = escalaManager.escalaAncho(zona.width);
            int height = escalaManager.escalaAlto(zona.height);

            g2.setColor(new Color(0, 255, 0, 80)); 
            g2.fillRect(x, y, width, height);
            g2.setColor(Color.GREEN);
            g2.drawRect(x, y, width, height);
        }
    }
}