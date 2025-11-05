package GUI;

import javax.swing.JFrame;
import java.util.*;
import java.awt.Point;

public class EstadoJuego {
    private static boolean cofreAbierto = false;
    private static boolean puertaAbierta = false;
    // Nuevo flag: indica si ya se mostró el mensaje inicial de la calle
    private static boolean mensajeCalleMostrado = false;

    // Nuevo flag: si ya habló con BRR (brr brr patapim)
    private static boolean habladoConBrr = false;

    // Temporizador global (ms)
    private static long timerStartMillis = 0;
    private static long timerAccumulatedMillis = 0; // acumulado cuando se pausa
    private static boolean timerRunning = false;

    // Overlay singleton
    private static TimerOverlay timerOverlay = null;

    // Nombre del jugador actual (persistente durante la sesión)
    private static String playerName = "Jugador";

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

    // Getter/Setter para hablar con BRR
    public static boolean isHabladoConBrr() {
        return habladoConBrr;
    }

    public static void setHabladoConBrr(boolean hablado) {
        habladoConBrr = hablado;
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

    private static final String CASCARA_NAME = "Cascara de Chimpanzini Bananini.png";
    private static final int CASCARA_X = 660;
    private static final int CASCARA_Y = 265;

    // Registra/actualiza la lista de spawned objects para una escena
    public static void setSpawnedObjects(String scene, List<SpawnedObject> list) {
        if (scene == null) return;
        String key = scene.toLowerCase();
        List<SpawnedObject> filtered = new ArrayList<>();
        for (SpawnedObject so : list) {
            if (so == null) continue;
            // Si la evidencia está en la lista requerida y está asignada a otra escena, evitar que se guarde aquí
            if (so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                String escenaAsignada = evidenciaAsignadaReverse.get(so.nombre);
                if (escenaAsignada != null && !escenaAsignada.equals(key)) {
                    System.out.println("Evito spawn de evidencia reservada '" + so.nombre + "' en escena '" + key + "' (reservada para '" + escenaAsignada + "')");
                    continue;
                }
            }
            filtered.add(so);
            // mantener reverse map si el ítem pertenece a una escena reservada
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
                    // Forzar coordenadas por si alguien las cambió
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

        // Reemplazar la lista almacenada por la versión filtrada
        spawnedObjectsPerScene.put(key, filtered);
    }

    // Marca un objeto como recogido (si no estaba) y aumenta el contador
    public static void markObjectCollected(String scene, SpawnedObject obj) {
        if (obj == null) return;
        // Antes la cáscara en 'comedor' estaba protegida; ahora permitimos recoger cualquier objeto
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
        evidenciaAsignadaPorEscena.clear();
        evidenciaAsignadaReverse.clear();
    }

    // -------------------- Asignación única de evidencias entre escenas --------------------
    // Lista de evidencias requeridas (exactamente 5)
    private static final java.util.List<String> evidenciaNecesaria = java.util.Arrays.asList(
        "Rueda de Boneca Ambalabu.png",
        "Palo de Tung Tung.png",
        "zapa.png",
        "Bandana de Capuchino Assasino.png",
        "Cascara de Chimpanzini Bananini.png"
    );

    // Map que asigna una evidencia a cada escena (clave en minúsculas)
    private static final Map<String, String> evidenciaAsignadaPorEscena = new HashMap<>();

    // Reverse mapping cache para consultas rápidas (evidencia -> escena)
    private static final Map<String, String> evidenciaAsignadaReverse = new HashMap<>();

    // Inicializar asignaciones obligatorias: asegurar que las 5 escenas clave tengan cada una una evidencia distinta
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

    // Comprueba si una evidencia de la lista requerida está reservada para otra escena (true si NO se puede usar aquí)
    public static synchronized boolean isEvidenceReservedForOtherScene(String scene, String evidenciaNombre) {
        if (evidenciaNombre == null) return false;
        if (!evidenciaNecesaria.contains(evidenciaNombre)) return false;
        String key = scene == null ? "" : scene.toLowerCase();
        // Verificar si existe un mapeo explícito en reverse
        String escenaAsignada = evidenciaAsignadaReverse.get(evidenciaNombre);
        if (escenaAsignada != null) {
            return !escenaAsignada.equals(key);
        }
        // Si no hay reverse, pero hay mapping forward, construir reverse
        for (Map.Entry<String, String> e : evidenciaAsignadaPorEscena.entrySet()) {
            if (e.getValue().equals(evidenciaNombre)) {
                evidenciaAsignadaReverse.putIfAbsent(evidenciaNombre, e.getKey());
                return !e.getKey().equals(key);
            }
        }
        return false;
    }

    // Devuelve la evidencia asignada para la escena (crea asignación global si es necesario)
    public static synchronized String getOrAssignUniqueEvidenceForScene(String scene) {
        if (scene == null) return null;
        String key = scene.toLowerCase();
        // Si ya hay una asignación explícita para esta escena, devolverla
        if (evidenciaAsignadaPorEscena.containsKey(key)) {
            return evidenciaAsignadaPorEscena.get(key);
        }
        // Revisar si ya hay spawned objects en esa escena con alguno de las evidencias; si existe, respetarlo
        java.util.List<SpawnedObject> existing = getSpawnedObjects(scene);
        for (SpawnedObject so : existing) {
            if (so != null && so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                evidenciaAsignadaPorEscena.put(key, so.nombre);
                evidenciaAsignadaReverse.putIfAbsent(so.nombre, key);
                return so.nombre;
            }
        }

        // Construir conjunto de evidencias ya usadas
        java.util.Set<String> usadas = new java.util.HashSet<>(evidenciaAsignadaPorEscena.values());
        // Si existen spawnedObjects en otras escenas que correspondan, respetarlas
        for (Map.Entry<String, List<SpawnedObject>> entry : spawnedObjectsPerScene.entrySet()) {
            for (SpawnedObject so : entry.getValue()) {
                if (so != null && so.nombre != null && evidenciaNecesaria.contains(so.nombre)) {
                    usadas.add(so.nombre);
                    evidenciaAsignadaPorEscena.putIfAbsent(entry.getKey(), so.nombre);
                    evidenciaAsignadaReverse.putIfAbsent(so.nombre, entry.getKey());
                }
            }
        }

        // Elegir la primera evidencia no usada
        String elegido = null;
        for (String ev : evidenciaNecesaria) {
            if (!usadas.contains(ev)) {
                elegido = ev;
                break;
            }
        }
        // Si no hay evidencias libres, devolver null para que la escena no spawnee ninguna de las evidencias requeridas
        if (elegido == null) {
            return null;
        }
        evidenciaAsignadaPorEscena.put(key, elegido);
        evidenciaAsignadaReverse.putIfAbsent(elegido, key);
        return elegido;
    }

    public static void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            playerName = "Jugador";
        } else {
            playerName = name.trim();
        }
    }

    public static String getPlayerName() {
        return playerName;
    }

    // Guarda el resultado actual (nombre + tiempo) en el ranking
    public static void savePlayerResult() {
        try {
            long elapsed = getElapsedMillis();
            RankingManager.saveResult(playerName, elapsed);
        } catch (Exception e) {
            System.err.println("Error guardando resultado en ranking: " + e.getMessage());
        }
    }

}