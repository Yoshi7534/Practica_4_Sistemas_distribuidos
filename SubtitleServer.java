import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class SubtitleServer {
    private static final String SUBTITLE_ENDPOINT = "/subtitle";

    private final int port;
    private HttpServer server;

    public static void main(String[] args) {
        int serverPort = 8080;
        if (args.length == 1) {
            serverPort = Integer.parseInt(args[0]);
        }

        SubtitleServer subtitleServer = new SubtitleServer(serverPort);
        subtitleServer.startServer();

        System.out.println("Servidor de subtítulos escuchando en el puerto " + serverPort);
    }

    public SubtitleServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HttpContext subtitleContext = server.createContext(SUBTITLE_ENDPOINT);
        subtitleContext.setHandler(this::handleSubtitleRequest);

        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
    }

    private void handleSubtitleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("get")) {
            exchange.close();
            return;
        }

        // Obtener el nombre de la película desde la URL
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getRawQuery();
        String movieName = null;

        if (query != null) {
            String[] queryParams = query.split("&");
            for (String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equalsIgnoreCase("movieName")) {
                    movieName = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.toString());
                    String modifiedMovieName = movieName.replace("_", " ");
                    System.out.println("Pelicula pedida: " + modifiedMovieName);
                    break;
                }
            }
        }

        if (movieName == null || movieName.isEmpty()) {
            sendResponse("Nombre de película no proporcionado", exchange);
            return;
        }

        // Construir el nombre del archivo de subtítulos
        String subtitleFileName = movieName + ".srt";

        // Leer el contenido del archivo de subtítulos
        byte[] subtitleBytes;
        try {
            subtitleBytes = readSubtitleFile(subtitleFileName);
        } catch (IOException e) {
            sendResponse("Subtitulos no encontrados para la pelicula: " + movieName, exchange);
            return;
        }

        // Enviar los subtítulos como respuesta
        sendResponse(subtitleBytes, exchange);

    }

    private byte[] readSubtitleFile(String subtitleFileName) throws IOException {
        // Suponiendo que los archivos de subtítulos están en el directorio "subtitles"
        String subtitlesDirectory = "D:\\tareas\\Sistemas Distribuidos\\P4\\peliculas";
        String fullPath = subtitlesDirectory + File.separator + subtitleFileName;
    
        try (FileInputStream fileInputStream = new FileInputStream(fullPath);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
    
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
    
            return byteArrayOutputStream.toByteArray();
        }
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
        exchange.close();
    }

    private void sendResponse(String responseString, HttpExchange exchange) throws IOException {
        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
        sendResponse(responseBytes, exchange);
    }

}
