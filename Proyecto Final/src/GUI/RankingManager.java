package GUI;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;


public class RankingManager {
    private static final Path RANKING_PATH = Paths.get(System.getProperty("user.home"), "ProyectoFinal_ranking.txt");
    private static String playerName = null;

    public static synchronized void setPlayerName(String name) {
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) return;
        if (name.length() > 20) name = name.substring(0, 20);
        playerName = name;
    }

    public static synchronized String getPlayerName() {
        return playerName;
    }

    public static void promptForPlayerName(JFrame parent) {
        if (playerName != null) return; 

        String input = null;
        while (true) {
            input = (String) JOptionPane.showInputDialog(parent,
                    "Ingresa tu nombre (máx 20 caracteres):",
                    "Nombre de jugador",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "");

            if (input == null) {
                int opt = JOptionPane.showConfirmDialog(parent, "¿Deseas continuar sin nombre? Se usará 'ANONIMO'", "Confirmar",
                        JOptionPane.YES_NO_OPTION);
                if (opt == JOptionPane.YES_OPTION) {
                    input = "ANONIMO";
                    break;
                } else {
                    continue; 
                }
            }

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "El nombre no puede quedar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (input.length() > 20) {
                input = input.substring(0, 20);
            }
            break;
        }

        setPlayerName(input);
    }

   
    public static synchronized void saveRecordIfApplicable(long millis) {
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "ANONIMO";
        }

        try {
            List<Record> records = readAll();
            records.add(new Record(playerName, millis));
            Collections.sort(records);
            writeAll(records);
        } catch (IOException e) {
            System.err.println("Error guardando ranking: " + e.getMessage());
        }
    }

    private static List<Record> readAll() throws IOException {
        List<Record> list = new ArrayList<>();
        if (Files.exists(RANKING_PATH)) {
            List<String> lines = Files.readAllLines(RANKING_PATH);
            for (String l : lines) {
                if (l == null || l.trim().isEmpty()) continue;
                String[] parts = l.split("\\|", 2);
                if (parts.length >= 2) {
                    try {
                        long ms = Long.parseLong(parts[0]);
                        String name = parts[1];
                        list.add(new Record(name, ms));
                    } catch (NumberFormatException ex) {
                        
                    }
                }
            }
        } else {
            try {
                Files.createFile(RANKING_PATH);
            } catch (IOException e) {
               
                throw e;
            }
        }
        return list;
    }

    private static void writeAll(List<Record> records) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Record r : records) {
            lines.add(r.millis + "|" + r.name);
        }
        Path tmp = RANKING_PATH.resolveSibling(RANKING_PATH.getFileName().toString() + ".tmp");
        Files.write(tmp, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.move(tmp, RANKING_PATH, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

   
    public static synchronized List<String> getFormattedRecordsDesc() {
        List<String> output = new ArrayList<>();
        try {
            List<Record> records = readAll();
            records.sort((a, b) -> Long.compare(b.millis, a.millis));

            for (Record r : records) {
                output.add(r.name + " - " + formatMillis(r.millis));
            }
        } catch (IOException ex) {
            output.add("No se pudo leer el ranking");
        }

        if (output.isEmpty()) {
            output.add("No hay registros aún");
        }
        return output;
    }

    private static String formatMillis(long ms) {
        long totalSecs = ms / 1000;
        long minutes = totalSecs / 60;
        long seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private static class Record implements Comparable<Record> {
        String name;
        long millis;
        Record(String name, long millis) { this.name = name; this.millis = millis; }
        public int compareTo(Record o) { return Long.compare(this.millis, o.millis); }
    }
}