package GUI;

import javax.swing.JFrame;

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
        long elapsed = getElapsedMillis() / 1000; // segundos
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
}