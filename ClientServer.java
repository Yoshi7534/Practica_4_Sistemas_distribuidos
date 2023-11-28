import java.io.File;
import java.util.Scanner;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClientServer {
    public static void main(String[] args) {
        // Ruta donde se encuentran las películas
        String moviesDirectory = "D:\\tareas\\Sistemas Distribuidos\\P4\\peliculas";

        // Mostrar los nombres de las películas disponibles
        String[] movieNames = getMovieNames(moviesDirectory);
        displayMovieNames(movieNames);

        // Obtener la selección del usuario
        int selectedMovieIndex = getUserSelection(movieNames);

        if (selectedMovieIndex != -1) {
            String selectedMovieName = movieNames[selectedMovieIndex];

            // Enviar solicitud al servidor
            sendRequestToServer(selectedMovieName);
        }
    }

    private static String[] getMovieNames(String directoryPath) {
        File directory = new File(directoryPath);

        // Obtener la lista de archivos en el directorio
        File[] files = directory.listFiles();

        if (files == null) {
            System.out.println("Error al leer el directorio.");
            System.exit(1);
        }

        // Filtrar solo los archivos con extensiones reconocibles
        return Arrays.stream(files)
                .filter(file -> file.isFile() && isValidMovieFile(file.getName()))
                .map(file -> removeExtension(file.getName()))
                .toArray(String[]::new);
    }

    private static boolean isValidMovieFile(String fileName) {
        // Agrega extensiones de archivo de películas válidas según tus necesidades
        String[] validExtensions = {".srt"};
        for (String extension : validExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private static String removeExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    private static void displayMovieNames(String[] movieNames) {
        System.out.println("Películas disponibles:");
        for (int i = 0; i < movieNames.length; i++) {
            String modifiedMovieName = movieNames[i].replace("_", " ");
            System.out.println((i + 1) + ". " + modifiedMovieName);
        }
    }

    private static int getUserSelection(String[] movieNames) {
        try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Seleccione el número de la película deseada: ");
            int selectedMovieIndex = scanner.nextInt();

            // Validar la selección del usuario
            if (selectedMovieIndex < 1 || selectedMovieIndex > movieNames.length) {
                System.out.println("Selección no válida. Saliendo.");
                System.exit(1);
            }

            return selectedMovieIndex - 1;
        }
    }

     private static void sendRequestToServer(String selectedMovieName) {
        try {
            // Construir la URL para la solicitud al servidor de subtítulos
            String serverUrl = "http://localhost:80/subtitle?movieName=" + selectedMovieName;
            URL url = new URL(serverUrl);

            // Abrir la conexión HTTP
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configurar la solicitud como GET
            connection.setRequestMethod("GET");

            // Obtener la respuesta del servidor
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Leer la respuesta del servidor
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Mostrar los subtítulos recibidos
                System.out.println("Subtítulos recibidos:\n" + response.toString());
            } else {
                System.out.println("Error al recibir los subtítulos. Código de respuesta: " + responseCode);
            }

            // Cerrar la conexión
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}