package GUI;
import javax.sound.sampled.*;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;

public class Musica {
    private static Clip clipMusica;
    private static Clip clipEfecto;

    // valores en porcentaje (0-100)
    private static int volumenMusica = 50;
    private static int volumenEfectos = 50;

    // Reproduce un clip una sola vez (efecto)
    public static void reproducir(String ruta) {
        try {
            // Detener y cerrar clip de efecto anterior
            if (clipEfecto != null) {
                try { if (clipEfecto.isRunning()) clipEfecto.stop(); } catch (Exception ignore) {}
                try { clipEfecto.close(); } catch (Exception ignore) {}
                clipEfecto = null;
            }

            AudioInputStream audioStream = openAudioStream(ruta);
            if (audioStream == null) {
                System.err.println("Musica.reproducir: no se pudo abrir audio: " + ruta);
                return;
            }
            clipEfecto = AudioSystem.getClip();
            clipEfecto.open(audioStream);
            aplicarVolumen(clipEfecto, volumenEfectos);
            clipEfecto.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reproduce en bucle (música de fondo)
    public static void reproducirLoop(String ruta) {
        try {
            // Detener y cerrar clip de música anterior
            if (clipMusica != null) {
                try { if (clipMusica.isRunning()) clipMusica.stop(); } catch (Exception ignore) {}
                try { clipMusica.close(); } catch (Exception ignore) {}
                clipMusica = null;
            }

            AudioInputStream audioStream = openAudioStream(ruta);
            if (audioStream == null) {
                System.err.println("Musica.reproducirLoop: no se pudo abrir audio: " + ruta);
                return;
            }
            clipMusica = AudioSystem.getClip();
            clipMusica.open(audioStream);
            aplicarVolumen(clipMusica, volumenMusica);
            clipMusica.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Intentar abrir AudioInputStream desde fichero o classpath
    private static AudioInputStream openAudioStream(String ruta) {
        try {
            // Primero intentar ruta como fichero directo
            try {
                File f = new File(ruta);
                if (f.exists()) {
                    return AudioSystem.getAudioInputStream(f);
                }
            } catch (Exception ignore) {}

            // Normalizar ruta para classpath: si empieza con '/' la usamos tal cual, si no la prefijamos
            String classpathRuta = ruta.startsWith("/") ? ruta : ("/" + ruta);

            InputStream is = Musica.class.getResourceAsStream(classpathRuta);
            if (is == null) {
                // Intentar con mayúscula en carpeta Sonidos si existe (algunas rutas usan resources/sonidos vs resources/Sonidos)
                String alt = classpathRuta.replace("/sonidos/", "/Sonidos/");
                is = Musica.class.getResourceAsStream(alt);
            }
            if (is == null) return null;
            BufferedInputStream bis = new BufferedInputStream(is);
            return AudioSystem.getAudioInputStream(bis);
        } catch (Exception e) {
            // no se pudo abrir
            return null;
        }
    }

    // Detiene la música
    public static void detenerMusica() {
        if (clipMusica != null) {
            try { if (clipMusica.isRunning()) clipMusica.stop(); } catch (Exception ignore) {}
            try { clipMusica.close(); } catch (Exception ignore) {}
            clipMusica = null;
        }
    }

    // Detiene efectos
    public static void detenerEfectos() {
        if (clipEfecto != null) {
            try { if (clipEfecto.isRunning()) clipEfecto.stop(); } catch (Exception ignore) {}
            try { clipEfecto.close(); } catch (Exception ignore) {}
            clipEfecto = null;
        }
    }

    // Detiene todo
    public static void detener() {
        detenerMusica();
        detenerEfectos();
    }

    // Ajusta el volumen de la música (0-100)
    public static void setVolumenMusica(int porcentaje) {
        volumenMusica = Math.max(0, Math.min(100, porcentaje));
        aplicarVolumen(clipMusica, volumenMusica);
    }

    // Ajusta el volumen de los efectos (0-100)
    public static void setVolumenEfectos(int porcentaje) {
        volumenEfectos = Math.max(0, Math.min(100, porcentaje));
        aplicarVolumen(clipEfecto, volumenEfectos);
    }

    public static int getVolumenMusica() {
        return volumenMusica;
    }

    public static int getVolumenEfectos() {
        return volumenEfectos;
    }

    // Aplica un porcentaje de volumen a un clip usando MASTER_GAIN si está disponible
    private static void aplicarVolumen(Clip clip, int porcentaje) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (porcentaje <= 0) {
                    gain.setValue(gain.getMinimum());
                } else {
                    // Convertir porcentaje a decibelios (dB)
                    // 20 * log10(p) donde p es fracción [0,1]
                    float linear = porcentaje / 100.0f;
                    float dB = (float) (20.0 * Math.log10(linear));
                    // Limitar al rango soportado
                    dB = Math.max(dB, gain.getMinimum());
                    dB = Math.min(dB, gain.getMaximum());
                    gain.setValue(dB);
                }
            } else if (clip.isControlSupported(FloatControl.Type.VOLUME)) {
                FloatControl vol = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
                float linear = porcentaje / 100.0f;
                vol.setValue(linear);
            }
        } catch (IllegalArgumentException iae) {
            // Si el control no acepta el valor calculado, ignorar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}