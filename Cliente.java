package arc;

import java.util.Random;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Cliente {
    public static void main(String args[]) throws java.io.IOException {
        String host = "localhost";
        int port = 6789;

        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(3000);
        
        // Definimos el tamaño del buffer de entrada y salida 
        byte[] bufferSalida = new byte[512];
        byte[] bufferEntrada = new byte[512];

        // Generador de números aleatorios
        Random random = new Random();

        // Generar coordenadas aleatorias (x, y, z)
        int x = random.nextInt(100);
        int y = random.nextInt(100);
        int z = random.nextInt(100);

        // Convertir las coordenadas en una cadena
        String coordenadas = x + "," + y + "," + z;

        // Convertir la cadena a bytes
        byte[] datosCoordenadas = coordenadas.getBytes(StandardCharsets.US_ASCII);
        
        // Enviar coordenadas al servidor
        DatagramPacket sendPacket = new DatagramPacket(datosCoordenadas, datosCoordenadas.length, new InetSocketAddress(host, port));
        socket.send(sendPacket);
        System.out.println("Enviando coordenadas: " + coordenadas);

        // Esperar respuesta del servidor
        DatagramPacket responsePacket = new DatagramPacket(bufferEntrada, bufferEntrada.length);
        try {
            socket.receive(responsePacket);
            String respuesta = new String(responsePacket.getData(), 0, responsePacket.getLength(), StandardCharsets.US_ASCII);
            System.out.println("Respuesta del servidor: " + respuesta);
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Sin respuesta antes del timeout");
        }

        // Crear un hilo para recibir coordenadas de vecinos
        new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket neighborPacket = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(neighborPacket);
                    String neighborCoordinates = new String(neighborPacket.getData(), 0, neighborPacket.getLength(), StandardCharsets.US_ASCII);
                    System.out.println("Coordenadas de un vecino: " + neighborCoordinates);
                } catch (java.net.SocketTimeoutException e) {
                    // Si no hay respuesta, puedes decidir continuar o salir
                    continue; // Continúa escuchando
                } catch (Exception e) {
                    System.out.println("Error al recibir coordenadas de un vecino: " + e.getMessage());
                    break;
                }
            }
            // Cerrar el socket al salir del hilo
            socket.close();
            System.out.println("Socket cerrado.");
        }).start();

        // Lógica para cerrar el cliente después de un tiempo o por entrada del usuario
        Scanner scanner = new Scanner(System.in);
        System.out.println("Presiona 'Enter' para cerrar el cliente...");
        scanner.nextLine(); // Espera a que el usuario presione Enter

        // Cerrar el socket cuando el usuario presiona Enter
        socket.close();
        System.out.println("Cliente cerrado.");
        scanner.close(); // Cerrar el escáner
    }
}
