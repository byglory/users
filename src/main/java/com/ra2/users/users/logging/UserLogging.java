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
public class UserLogging {

    private static final String LOGS_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "aplicacio-";
    private static final String LOG_FILE_EXTENSION = ".log";
    
    // Format per a la data dins del log
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Mètodes públics per cridar des del Service
    public void info(String classe, String method, String description) {
        escriureLog("INFO", classe, method, description);
    }

    public void error(String classe, String method, String description) {
        escriureLog("ERROR", classe, method, description);
    }

    // Mètode privat per obtenir el nom del fitxer d'avui 
    private String obtenirFitxerAvui() {
        String dataAvui = LocalDate.now().toString(); // Retorna YYYY-MM-DD
        return LOGS_DIR + File.separator + LOG_FILE_PREFIX + dataAvui + LOG_FILE_EXTENSION;
    }

    private void escriureLog(String nivell, String classe, String method, String description) {
        // Crear directori logs si no existeix 
        File directory = new File(LOGS_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String fitxerAvui = obtenirFitxerAvui();
        Path currentFile = Paths.get(fitxerAvui);

        // Escriptura amb BufferedWriter 
        try (BufferedWriter writer = Files.newBufferedWriter(
                currentFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) { // Append per no esborrar el contingut anterior

            String timeStamp = LocalDateTime.now().format(dateTimeFormatter);
            // Format: [DATETIME] LEVEL - CLASS - METHOD - DESCRIPTION 
            String entradaLog = String.format("[%s] %s - %s - %s - %s", timeStamp, nivell, classe, method, description);
            
            writer.write(entradaLog);
            writer.newLine();

        } catch (Exception e) {
            System.err.println("Error escrivint al log: " + e.getMessage());
        }
    }
}