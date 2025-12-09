package com.ra2.users.users.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class CustomLogging {

    private static final String LOGS_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "aplicacio-";
    private static final String LOG_FILE_EXTENSION = ".log";
    
    // Format de data per al contingut del log (Ex: 2025-11-26 11:15:42)
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Mètode per registrar informació (INFO)
     * Format: [DATETIME] LEVEL - CLASS - METHOD - DESCRIPTION
     */
    public void info(String classe, String method, String description) {
        escriureLog("INFO", classe, method, description);
    }

    /**
     * Mètode per registrar errors (ERROR)
     * Format: [DATETIME] LEVEL - CLASS - METHOD - DESCRIPTION
     */
    public void error(String classe, String method, String description) {
        escriureLog("ERROR", classe, method, description);
    }

    /**
     * Genera la ruta del fitxer basat en la data actual
     * Ex: logs/aplicacio-2025-11-26.log
     */
    private String obtenirFitxerAvui() {
        String dataAvui = LocalDate.now().toString(); // Format per defecte YYYY-MM-DD
        return LOGS_DIR + File.separator + LOG_FILE_PREFIX + dataAvui + LOG_FILE_EXTENSION;
    }

    /**
     * Mètode privat que realitza l'escriptura física al fitxer.
     * Crea el directori i el fitxer si no existeixen.
     */
    private void escriureLog(String nivell, String classe, String method, String description) {
        // 1. Assegurar que el directori logs existeix
        File directory = new File(LOGS_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        // 2. Obtenir el fitxer d'avui
        String fitxerAvui = obtenirFitxerAvui();
        Path currentFile = Paths.get(fitxerAvui);

        // 3. Escriure al fitxer (append = true per no esborrar logs anteriors)
        try (BufferedWriter writer = Files.newBufferedWriter(
                currentFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            
            String timeStamp = LocalDateTime.now().format(dateTimeFormatter);
            
            // Construcció del missatge segons el format requerit
            String entradaLog = String.format("[%s] %s - %s - %s - %s", timeStamp, nivell, classe, method, description);
            
            writer.write(entradaLog);
            writer.newLine(); // Salt de línia
            
        } catch (Exception e) {
            // Si falla el log, ho mostrem per consola per no perdre l'error
            System.err.println("Error escrivint al log: " + e.getMessage());
        }
    }
}