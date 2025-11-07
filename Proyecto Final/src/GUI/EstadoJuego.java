package GUI;

import javax.swing.JFrame;
import java.util.*;

public class EstadoJuego {
    // Estados de cofres y puertas
    private static boolean cofreAbierto = false;
    private static boolean puertaAbierta = false;
    private static boolean cofreCasaPrincipalAbierto = false;
    private static boolean cofreComedorAbierto = false;
    private static boolean cofreHabitacion1Abierto = false;
    
    // Flags de mensajes
    private static boolean mensajeCalleMostrado = false;
    private static boolean mensajeCasaPrincipalMostrado = false;
    
    // Flags de diálogos especiales
    private static boolean puedeMostrarDialogosEspeciales = false;
    private static boolean brrHablado = false;
    
    // Temporizador global
    private static long timerStartMillis = 0;
    private static long timerAccumulatedMillis = 0;
    private static boolean timerRunning = false;
    
    // Overlay singleton
    private static TimerOverlay timerOverlay = null;
    
    // Control de spawn
    private static boolean spawnRandomActivado = true;
    
    // Persistencia de objetos spawneados
    private static final Map<String, List<SpawnedObject>> spawnedObjectsPerScene = new HashMap<>();
    private static int objetosRecogidos = 0;

    // ==================== GETTERS Y SETTERS BÁSICOS ====================
    
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

    public static boolean isCofreCasaPrincipalAbierto() {
        return cofreCasaPrincipalAbierto;
    }

    public static void setCofreCasaPrincipalAbierto(boolean abierto) {
        cofreCasaPrincipalAbierto = abierto;
    }

    public static boolean isCofreComedorAbierto() {
        return cofreComedorAbierto;
    }

    public static void setCofreComedorAbierto(boolean abierto) {
        cofreComedorAbierto = abierto;
    }
    
    public static boolean isCofreHabitacion1Abierto() {
        return cofreHabitacion1Abierto;
    }

    public static void setCofreHabitacion1Abierto(boolean abierto) {
        cofreHabitacion1Abierto = abierto;
    }

    public static boolean isMensajeCalleMostrado() {
        return mensajeCalleMostrado;
    }

    public static void setMensajeCalleMostrado(boolean mostrado) {
        mensajeCalleMostrado = mostrado;
    }

    public static boolean isMensajeCasaPrincipalMostrado() {
        return mensajeCasaPrincipalMostrado;
    }

    public static void setMensajeCasaPrincipalMostrado(boolean mostrado) {
        mensajeCasaPrincipalMostrado = mostrado;
    }

    public static boolean isPuedeMostrarDialogosEspeciales() {
        return puedeMostrarDialogosEspeciales;
    }

    public static void setPuedeMostrarDialogosEspeciales(boolean puede) {
        puedeMostrarDialogosEspeciales = puede;
    }

    public static boolean isBrrHablado() {
        return brrHablado;
    }

    public static void setBrrHablado(boolean hablado) {
        brrHablado = hablado;
    }

    public static boolean isSpawnRandomActivado() {
        return spawnRandomActivado;
    }

    public static void setSpawnRandomActivado(boolean activado) {
        spawnRandomActivado = activado;
    }

    // ==================== TEMPORIZADOR ====================

    public static void startTimer() {
        if (!timerRunning) {
            timerStartMillis = System.currentTimeMillis();
            timerRunning = true;
        }
    }

    public static void stopTimer() {
        if (timerRunning) {
            long now = System.currentTimeMillis();
            timerAccumulatedMillis += now - timerStartMillis;
            timerRunning = false;
        }
    }

    public static void resetTimer() {
        timerStartMillis = 0;
        timerAccumulatedMillis = 0;
        timerRunning = false;
    }

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

    public static String getFormattedElapsed() {
        long elapsed = getElapsedMillis() / 1000; 
        long minutes = elapsed / 60;
        long seconds = elapsed % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // ==================== OVERLAY HELPERS ====================

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

    // ==================== SPAWNED OBJECTS ====================

    public static class SpawnedObject {
        public String nombre;
        public int x;
        public int y;
        public boolean recogido = false;

        public SpawnedObject(String nombre, int x, int y) {
            this.nombre = nombre;
            this.x = x;
            this.y = y;
        }
    }

    private static final String CASCARA_NAME = "Cascara de Chimpanzini Bananini.png";
    private static final int CASCARA_X = 660;
    private static final int CASCARA_Y = 265;

    public static List<SpawnedObject> getSpawnedObjects(String scene) {
        String key = scene == null ? "" : scene.toLowerCase();
        List<SpawnedObject> list = spawnedObjectsPerScene.get(key);
        if (list == null) {
            list = new ArrayList<SpawnedObject>();
            spawnedObjectsPerScene.put(key, list);
        }
        return list;
    }

    public static void setSpawnedObjects(String scene, List<SpawnedObject> list) {
        if (scene == null) return;
        String key = scene.toLowerCase();
        List<SpawnedObject> filtered = new ArrayList<>();
        
        for (SpawnedObject so : list) {
            if (so == null) continue;
            
            // Verificar si la evidencia está reservada para otra escena
            if (so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                String escenaAsignada = evidenciaAsignadaReverse.get(so.nombre);
                if (escenaAsignada != null && !escenaAsignada.equals(key)) {
                    System.out.println("Evito spawn de evidencia reservada '" + so.nombre + 
                                     "' en escena '" + key + "' (reservada para '" + escenaAsignada + "')");
                    continue;
                }
            }
            
            filtered.add(so);
            
            // Mantener reverse map si el ítem pertenece a una escena reservada
            if (so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                evidenciaAsignadaPorEscena.putIfAbsent(key, so.nombre);
                evidenciaAsignadaReverse.putIfAbsent(so.nombre, key);
            }
        }
        
        // Asegurar inmutabilidad de la cáscara en la escena 'comedor'
        if ("comedor".equals(key)) {
            boolean tiene = false;
            for (SpawnedObject so : filtered) {
                if (so != null && CASCARA_NAME.equals(so.nombre)) {
                    so.x = CASCARA_X;
                    so.y = CASCARA_Y;
                    tiene = true;
                    break;
                }
            }
            if (!tiene) {
                filtered.add(new SpawnedObject(CASCARA_NAME, CASCARA_X, CASCARA_Y));
            }
        }

        spawnedObjectsPerScene.put(key, filtered);
    }

    public static void markObjectCollected(String scene, SpawnedObject obj) {
        if (obj == null) return;
        if (!obj.recogido) {
            obj.recogido = true;
            objetosRecogidos++;
            System.out.println("Objetos recogidos: " + objetosRecogidos);
        }
    }

    public static boolean todasLasEvidenciasRecolectadas() {
        return objetosRecogidos >= 5;
    }

    public static int getObjetosRecogidos() {
        return objetosRecogidos;
    }

    public static void resetSpawnedObjects() {
        spawnedObjectsPerScene.clear();
        objetosRecogidos = 0;
        evidenciaAsignadaPorEscena.clear();
        evidenciaAsignadaReverse.clear();
    }

    // ==================== ASIGNACIÓN DE EVIDENCIAS ====================

    private static final java.util.List<String> evidenciaNecesaria = java.util.Arrays.asList(
        "Rueda de Boneca Ambalabu.png",
        "Palo de Tung Tung.png",
        "zapa.png",
        "Bandana de Capuchino Assasino.png",
        "Cascara de Chimpanzini Bananini.png"
    );

    private static final Map<String, String> evidenciaAsignadaPorEscena = new HashMap<>();
    private static final Map<String, String> evidenciaAsignadaReverse = new HashMap<>();

    static {
        java.util.List<String> escenasObligatorias = java.util.Arrays.asList(
            "casaprincipal",
            "laberinto",
            "habitacion1",
            "habitacion2",
            "comedor"
        );
        for (int i = 0; i < escenasObligatorias.size() && i < evidenciaNecesaria.size(); i++) {
            String sc = escenasObligatorias.get(i).toLowerCase();
            String ev = evidenciaNecesaria.get(i);
            evidenciaAsignadaPorEscena.putIfAbsent(sc, ev);
            evidenciaAsignadaReverse.putIfAbsent(ev, sc);
        }
    }

    public static synchronized boolean isEvidenceReservedForOtherScene(String scene, String evidenciaNombre) {
        if (evidenciaNombre == null) return false;
        if (!evidenciaNecesaria.contains(evidenciaNombre)) return false;
        String key = scene == null ? "" : scene.toLowerCase();
        String escenaAsignada = evidenciaAsignadaReverse.get(evidenciaNombre);
        if (escenaAsignada != null) {
            return !escenaAsignada.equals(key);
        }
        for (Map.Entry<String, String> e : evidenciaAsignadaPorEscena.entrySet()) {
            if (e.getValue().equals(evidenciaNombre)) {
                evidenciaAsignadaReverse.putIfAbsent(evidenciaNombre, e.getKey());
                return !e.getKey().equals(key);
            }
        }
        return false;
    }

    public static void reiniciarJuegoCompleto() {
        stopTimer();
        resetTimer();
        
        removeTimerOverlayIfExists();
        
        setPuedeMostrarDialogosEspeciales(false);
        setMensajeCasaPrincipalMostrado(false);
        setMensajeCalleMostrado(false);
        
        resetSpawnedObjects();
        
        System.out.println("Juego reiniciado completamente");
    }
    public static synchronized String getOrAssignUniqueEvidenceForScene(String scene) {
        if (scene == null) return null;
        String key = scene.toLowerCase();
        
        if (evidenciaAsignadaPorEscena.containsKey(key)) {
            return evidenciaAsignadaPorEscena.get(key);
        }
        
        java.util.List<SpawnedObject> existing = getSpawnedObjects(scene);
        for (SpawnedObject so : existing) {
            if (so != null && so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                evidenciaAsignadaPorEscena.put(key, so.nombre);
                evidenciaAsignadaReverse.putIfAbsent(so.nombre, key);
                return so.nombre;
            }
        }

        java.util.Set<String> usadas = new java.util.HashSet<>(evidenciaAsignadaPorEscena.values());
        for (Map.Entry<String, List<SpawnedObject>> entry : spawnedObjectsPerScene.entrySet()) {
            for (SpawnedObject so : entry.getValue()) {
                if (so != null && so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                    usadas.add(so.nombre);
                    evidenciaAsignadaPorEscena.putIfAbsent(entry.getKey(), so.nombre);
                    evidenciaAsignadaReverse.putIfAbsent(so.nombre, entry.getKey());
                }
            }
        }

        String elegido = null;
        for (String ev : evidenciaNecesaria) {
            if (!usadas.contains(ev)) {
                elegido = ev;
                break;
            }
        }
        
        if (elegido == null) {
            return null;
        }
        
        evidenciaAsignadaPorEscena.put(key, elegido);
        evidenciaAsignadaReverse.putIfAbsent(elegido, key);
        return elegido;
    }
}