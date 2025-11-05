package GUI;

import java.io.*;
import java.util.*;

public class RankingManager {
    private static final String DIR = System.getProperty("user.home") + File.separator + ".ProyectoFinal";
    private static final String FILE = DIR + File.separator + "ranking.txt";

    public static class Entry {
        public final String name;
        public final long millis;

        public Entry(String name, long millis) {
            this.name = name;
            this.millis = millis;
        }
    }

    private static void ensureDirExists() {
        try {
            File d = new File(DIR);
            if (!d.exists()) d.mkdirs();
            File f = new File(FILE);
            if (!f.exists()) f.createNewFile();
        } catch (Exception e) {
            System.err.println("RankingManager: error creando directorio/archivo: " + e.getMessage());
        }
    }

    public static synchronized void saveResult(String name, long millis) {
        if (name == null) name = "Jugador";
        ensureDirExists();
        try (FileWriter fw = new FileWriter(FILE, true); BufferedWriter bw = new BufferedWriter(fw)) {
            String line = name.replace(";", "_") + ";" + millis + System.lineSeparator();
            bw.write(line);
            bw.flush();
        } catch (Exception e) {
            System.err.println("RankingManager: error guardando resultado: " + e.getMessage());
        }
    }

    public static synchronized List<Entry> loadRanking() {
        ensureDirExists();
        List<Entry> list = new ArrayList<>();
        try (FileReader fr = new FileReader(FILE); BufferedReader br = new BufferedReader(fr)) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln.trim().isEmpty()) continue;
                String[] parts = ln.split(";", 2);
                if (parts.length == 2) {
                    String name = parts[0];
                    long millis = 0;
                    try { millis = Long.parseLong(parts[1]); } catch (NumberFormatException ex) { millis = 0; }
                    list.add(new Entry(name, millis));
                }
            }
        } catch (FileNotFoundException fnf) {
            // ignorar
        } catch (Exception e) {
            System.err.println("RankingManager: error leyendo ranking: " + e.getMessage());
        }

        // ordenar por tiempo ascendente
        Collections.sort(list, new Comparator<Entry>() {
            public int compare(Entry a, Entry b) {
                return Long.compare(a.millis, b.millis);
            }
        });
        return list;
    }

    // Utilidad para formatear mm:ss
    public static String formatMillis(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", minutes, sec);
    }
}
