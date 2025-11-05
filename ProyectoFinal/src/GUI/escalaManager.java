package GUI;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Sistema de escalado proporcional para múltiples resoluciones.
 * Usa 1366x768 como resolución base de referencia.
 */
public class escalaManager {

    // 🔹 RESOLUCIÓN BASE (la que estás usando para desarrollar)
    public static final int BASE_WIDTH = 1366;
    public static final int BASE_HEIGHT = 768;

    /**
     * Perfil de resolución con factores de escala personalizados.
     * Permite definir un valor único por resolución para sprites, máscaras y assets.
     */
    public static final class ResolucionPerfil {
        private final int width;
        private final int height;
        private final double escalaX;
        private final double escalaY;
        private final double escalaUniforme;
        private final String resourceKey;

        public ResolucionPerfil(int width, int height) {
            this(width, height, null);
        }

        public ResolucionPerfil(int width, int height, String resourceKey) {
            this(width, height,
                 (double) width / BASE_WIDTH,
                 (double) height / BASE_HEIGHT,
                 resourceKey);
        }

        public ResolucionPerfil(int width, int height,
                                 double escalaX, double escalaY,
                                 String resourceKey) {
            this.width = width;
            this.height = height;
            this.escalaX = escalaX;
            this.escalaY = escalaY;
            this.escalaUniforme = Math.min(escalaX, escalaY);
            String key = resourceKey;
            if (key == null || key.trim().isEmpty()) {
                key = generarClave(width, height);
            }
            this.resourceKey = key;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public double getEscalaX() {
            return escalaX;
        }

        public double getEscalaY() {
            return escalaY;
        }

        public double getEscalaUniforme() {
            return escalaUniforme;
        }

        public String getResourceKey() {
            return resourceKey;
        }

        public String getKey() {
            return generarClave(width, height);
        }
    }

    // Tabla de resoluciones soportadas -> factores únicos
    private static final LinkedHashMap<String, ResolucionPerfil> PERFILES = new LinkedHashMap<>();

    static {
        registrarResolucion(new ResolucionPerfil(BASE_WIDTH, BASE_HEIGHT));          // 1366x768 (base)
        registrarResolucion(new ResolucionPerfil(1920, 1080, "1920x1080"));         // Full HD
        registrarResolucion(new ResolucionPerfil(1280, 720, "1280x720"));           // HD
    }

    // Variables de escala actual
    private static double escalaX = 1.0;
    private static double escalaY = 1.0;
    private static double escalaUniforme = 1.0; // Para mantener proporciones

    private static int anchoActual = BASE_WIDTH;
    private static int altoActual = BASE_HEIGHT;
    private static ResolucionPerfil perfilActual = PERFILES.get(generarClave(BASE_WIDTH, BASE_HEIGHT));

    /**
     * Configura la escala según la resolución actual
     */
    public static void configurarEscala(int width, int height) {
        ResolucionPerfil perfil = obtenerPerfil(width, height);
        if (perfil == null) {
            perfil = new ResolucionPerfil(width, height);
            registrarResolucion(perfil);
        }

        perfilActual = perfil;
        anchoActual = width;
        altoActual = height;

        escalaX = perfil.getEscalaX();
        escalaY = perfil.getEscalaY();
        escalaUniforme = perfil.getEscalaUniforme();

        System.out.println("Escala configurada: " + width + "x" + height);
        System.out.println("Factor X: " + escalaX + ", Y: " + escalaY + ", Uniforme: " + escalaUniforme);
    }

    /**
     * Devuelve la lista de resoluciones soportadas (formato ANCHOxALTO).
     */
    public static List<String> getResolucionesSoportadas() {
        return new ArrayList<>(PERFILES.keySet());
    }

    /**
     * Permite acceder a la tabla completa de perfiles (solo lectura).
     */
    public static Map<String, ResolucionPerfil> getPerfilesRegistrados() {
        return Collections.unmodifiableMap(PERFILES);
    }

    public static ResolucionPerfil getResolucionActual() {
        return perfilActual;
    }

    public static String getResolucionActualKey() {
        if (perfilActual != null) {
            return perfilActual.getKey();
        }
        return generarClave(anchoActual, altoActual);
    }

    public static String getResolucionBaseKey() {
        return generarClave(BASE_WIDTH, BASE_HEIGHT);
    }

    /**
     * Escala una coordenada X.
     */
    public static int escalaX(int x) {
        return (int) Math.round(x * escalaX);
    }

    /**
     * Escala una coordenada Y.
     */
    public static int escalaY(int y) {
        return (int) Math.round(y * escalaY);
    }

    /**
     * Escala un ancho manteniendo proporciones.
     */
    public static int escalaAncho(int w) {
        return (int) Math.round(w * escalaX);
    }

    /**
     * Escala un alto manteniendo proporciones.
     */
    public static int escalaAlto(int h) {
        return (int) Math.round(h * escalaY);
    }

    /**
     * Escala uniforme (mantiene proporciones para sprites/personajes).
     */
    public static int escalaUniforme(int valor) {
        return (int) Math.round(valor * escalaUniforme);
    }

    /**
     * Escala un tamaño de fuente.
     */
    public static int escalaFuente(int fontSize) {
        return Math.max(8, (int) Math.round(fontSize * escalaUniforme));
    }

    /**
     * Obtiene un Dimension escalado.
     */
    public static Dimension escalarDimension(int w, int h) {
        return new Dimension(escalaAncho(w), escalaAlto(h));
    }

    // Getters numéricos
    public static double getEscalaX() { return escalaX; }
    public static double getEscalaY() { return escalaY; }
    public static double getEscalaUniforme() { return escalaUniforme; }
    public static int getAnchoActual() { return anchoActual; }
    public static int getAltoActual() { return altoActual; }

    /**
     * Convierte coordenadas de pantalla a coordenadas del juego (útil para colisiones).
     */
    public static int xPantallaAJuego(int x) {
        if (escalaX == 0) return x;
        return (int) Math.round(x / escalaX);
    }

    public static int yPantallaAJuego(int y) {
        if (escalaY == 0) return y;
        return (int) Math.round(y / escalaY);
    }

    /**
     * Resuelve un recurso dependiendo de la resolución actual.
     * Permite tener variantes de imágenes/escenarios por resolución.
     */
    public static String resolverRecursoPorResolucion(String rutaBase) {
        return resolverRecursoPorResolucion(anchoActual, altoActual, rutaBase);
    }

    /**
     * Resuelve un recurso dependiendo de la resolución indicada.
     * Busca archivos con sufijos o carpetas específicas (p. ej. "_1920x1080").
     */
    public static String resolverRecursoPorResolucion(int width, int height, String rutaBase) {
        if (rutaBase == null || rutaBase.isEmpty()) {
            return rutaBase;
        }

        File baseFile = new File(rutaBase);
        ResolucionPerfil perfil = obtenerPerfil(width, height);
        String key = (perfil != null) ? perfil.getResourceKey() : generarClave(width, height);

        String recurso = buscarRecursoAlternativo(baseFile, key);
        if (recurso != null) {
            return recurso;
        }

        if (perfil != null && !Objects.equals(perfil.getResourceKey(), perfil.getKey())) {
            recurso = buscarRecursoAlternativo(baseFile, perfil.getKey());
            if (recurso != null) {
                return recurso;
            }
        }

        return rutaBase;
    }

    // --------- Métodos utilitarios internos ---------

    private static void registrarResolucion(ResolucionPerfil perfil) {
        if (perfil == null) {
            return;
        }
        PERFILES.put(perfil.getKey(), perfil);
    }

    private static ResolucionPerfil obtenerPerfil(int width, int height) {
        return PERFILES.get(generarClave(width, height));
    }

    private static String buscarRecursoAlternativo(File baseFile, String key) {
        if (baseFile == null || key == null) {
            return null;
        }

        File parent = baseFile.getParentFile();
        if (parent == null) {
            return null;
        }

        String fileName = baseFile.getName();
        int dot = fileName.lastIndexOf('.');
        String baseName = (dot >= 0) ? fileName.substring(0, dot) : fileName;
        String extension = (dot >= 0) ? fileName.substring(dot) : "";

        String normalizedKey = key.replaceAll("\\s+", "");
        List<String> candidateNames = new ArrayList<>();
        candidateNames.add(baseName + "_" + normalizedKey + extension);
        candidateNames.add(baseName + "-" + normalizedKey + extension);

        if (normalizedKey.contains("x")) {
            String[] dims = normalizedKey.split("x");
            if (dims.length == 2) {
                String widthKey = dims[0];
                String heightKey = dims[1];
                candidateNames.add(baseName + "_" + widthKey + extension);
                candidateNames.add(baseName + "-" + widthKey + extension);
                candidateNames.add(baseName + "_" + heightKey + extension);
                candidateNames.add(baseName + "-" + heightKey + extension);
            }
        }

        // 1) Buscar archivos con sufijo en el mismo directorio
        for (String candidate : candidateNames) {
            File candidateFile = new File(parent, candidate);
            if (esArchivoValido(candidateFile)) {
                return candidateFile.getPath();
            }
        }

        // 2) Buscar en subcarpetas por resolución
        List<String> candidateFolders = new ArrayList<>();
        candidateFolders.add(key);
        candidateFolders.add(normalizedKey);
        if (normalizedKey.contains("x")) {
            String[] dims = normalizedKey.split("x");
            if (dims.length == 2) {
                candidateFolders.add(dims[0] + "x" + dims[1]);
                candidateFolders.add(dims[0]);
                candidateFolders.add(dims[1]);
            }
        }

        for (String folderKey : candidateFolders) {
            File folder = new File(parent, folderKey);
            if (!folder.isDirectory()) {
                continue;
            }

            File directMatch = new File(folder, fileName);
            if (esArchivoValido(directMatch)) {
                return directMatch.getPath();
            }

            for (String candidate : candidateNames) {
                File candidateFile = new File(folder, candidate);
                if (esArchivoValido(candidateFile)) {
                    return candidateFile.getPath();
                }
            }
        }

        return null;
    }

    private static boolean esArchivoValido(File file) {
        return file != null && file.exists() && file.isFile();
    }

    private static String generarClave(int width, int height) {
        return width + "x" + height;
    }
}
