package GUI;

import javax.swing.JFrame;
import java.util.*;
import java.awt.Point;

public class EstadoJuego {
    private static boolean cofreAbierto = false;
    private static boolean puertaAbierta = false;
    // Nuevo flag: indica si ya se mostró el mensaje inicial de la calle
    private static boolean mensajeCalleMostrado = false;

    // Temporizador global (ms)
    private static long timerStartMillis = 0;
    private static long timerAccumulatedMillis = 0; // acumulado cuando se pausa
    private static boolean timerRunning = false;

    // Overlay singleton
    private static TimerOverlay timerOverlay = null;

    public static boolean isCofreAbierto() {
        return cofreAbierto;
    }

    public static void setCofreAbierto(boolean abierto) {
        cofreAbierto = abierto;
    }

    public static boolean isPuertaAbierta() {
        return puertaAbierta;
    }

    public static void setPuertaAbierta(boolean abierta) {
        puertaAbierta = abierta;
    }

    // Getter/Setter para el nuevo flag
    public static boolean isMensajeCalleMostrado() {
        return mensajeCalleMostrado;
    }

    public static void setMensajeCalleMostrado(boolean mostrado) {
        mensajeCalleMostrado = mostrado;
    }

    // Flag: si ya se mostró el mensaje al entrar en CasaPrincipal
    private static boolean mensajeCasaPrincipalMostrado = false;

    public static boolean isMensajeCasaPrincipalMostrado() {
        return mensajeCasaPrincipalMostrado;
    }

    public static void setMensajeCasaPrincipalMostrado(boolean mostrado) {
        mensajeCasaPrincipalMostrado = mostrado;
    }

    // Temporizador global: iniciar
    public static void startTimer() {
        if (!timerRunning) {
            timerStartMillis = System.currentTimeMillis();
            timerRunning = true;
        }
    }

    // Pausar/stop
    public static void stopTimer() {
        if (timerRunning) {
            long now = System.currentTimeMillis();
            timerAccumulatedMillis += now - timerStartMillis;
            timerRunning = false;
        }
    }

    // Resetear
    public static void resetTimer() {
        timerStartMillis = 0;
        timerAccumulatedMillis = 0;
        timerRunning = false;
    }

    // Devuelve ms transcurridos
    public static long getElapsedMillis() {
        if (timerRunning) {
            long now = System.currentTimeMillis();
            return timerAccumulatedMillis + (now - timerStartMillis);
        } else {
            return timerAccumulatedMillis;
        }
    }

    public static boolean isTimerRunning() {
        return timerRunning;
    }

    // Formatea en mm:ss
    public static String getFormattedElapsed() {
        long elapsed = getElapsedMillis() / 1000; 
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Overlay helpers
    public static void createTimerOverlayIfNeeded(JFrame frame) {
        if (timerOverlay == null && frame != null) {
            try {
                timerOverlay = new TimerOverlay(frame);
                timerOverlay.attachToFrame();
            } catch (Exception e) {
                System.err.println("Error al crear TimerOverlay: " + e.getMessage());
            }
        }
    }

    public static void removeTimerOverlayIfExists() {
        if (timerOverlay != null) {
            try {
                timerOverlay.detachFromFrame();
            } catch (Exception e) {
                // ignore
            }
            timerOverlay = null;
        }
    }

    public static boolean isTimerOverlayPresent() {
        return timerOverlay != null;
    }
    private static boolean spawnRandomActivado = true;

    public static boolean isSpawnRandomActivado() {
        return spawnRandomActivado;
    }

    public static void setSpawnRandomActivado(boolean activado) {
        spawnRandomActivado = activado;
    }
    
    // -------------------- Persistencia de spawns y contador de recogidos --------------------
    public static class SpawnedObject {
        public String nombre; // nombre de la imagen
        public int x; // coordenadas base (sin escalar)
        public int y;
        public boolean recogido = false;

        public SpawnedObject(String nombre, int x, int y) {
            this.nombre = nombre;
            this.x = x;
            this.y = y;
        }
    }

    private static final Map<String, List<SpawnedObject>> spawnedObjectsPerScene = new HashMap<String, List<SpawnedObject>>();
    private static int objetosRecogidos = 0;

    // Devuelve la lista (crea si no existe). La lista puede estar vacía si no se generaron aún.
    public static List<SpawnedObject> getSpawnedObjects(String scene) {
        String key = scene == null ? "" : scene.toLowerCase();
        List<SpawnedObject> list = spawnedObjectsPerScene.get(key);
        if (list == null) {
            list = new ArrayList<SpawnedObject>();
            spawnedObjectsPerScene.put(key, list);
        }
        return list;
    }

    // Registra/actualiza la lista de spawned objects para una escena
    public static void setSpawnedObjects(String scene, List<SpawnedObject> list) {
        spawnedObjectsPerScene.put(scene.toLowerCase(), list);
    }

    // Marca un objeto como recogido (si no estaba) y aumenta el contador
    public static void markObjectCollected(String scene, SpawnedObject obj) {
        if (obj == null) return;
        if (!obj.recogido) {
            obj.recogido = true;
            objetosRecogidos++;
        }
    }

    public static int getObjetosRecogidos() {
        return objetosRecogidos;
    }

    // Opcional: resetear spawned objects (solo para debug/tests)
    public static void resetSpawnedObjects() {
        spawnedObjectsPerScene.clear();
        objetosRecogidos = 0;
    }
}