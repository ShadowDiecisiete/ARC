package arc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Servidor {
    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);

        // Pedimos los parámetros N, V y S al usuario
        System.out.print("Ingrese el número total de clientes (N): ");
        int N = scanner.nextInt();

        System.out.print("Ingrese el número de vecinos por grupo (V): ");
        int V = scanner.nextInt();

        System.out.print("Ingrese el número de iteraciones de la simulación (S): ");
        int S = scanner.nextInt();

        // Verificamos que V sea divisor de N
        if (N % V != 0) {
            System.out.println("El número de vecinos (V) debe ser un divisor de N. Por favor, reinicie el servidor.");
            return;
        }

        List<ClienteInfo> clientes = new ArrayList<>();

        try {
            // Iniciar el socket del servidor
            DatagramSocket socketUDP = new DatagramSocket(6789);
            byte[] bufer = new byte[1000];

            System.out.println("Servidor iniciado. Esperando paquetes...");

            // Recibir datos de los clientes
            for (int i = 0; i < N; i++) {
                DatagramPacket peticion = new DatagramPacket(bufer, bufer.length);
                socketUDP.receive(peticion);

                System.out.println("Datagrama recibido del host: " + peticion.getAddress());
                System.out.println("Desde el puerto remoto: " + peticion.getPort());

                // Guardar la información del cliente
                clientes.add(new ClienteInfo(peticion.getAddress(), peticion.getPort(), new String(peticion.getData(), 0, peticion.getLength())));

                // Enviar la respuesta de vuelta
                DatagramPacket respuesta = new DatagramPacket(peticion.getData(), peticion.getLength(), peticion.getAddress(), peticion.getPort());
                socketUDP.send(respuesta);
                System.out.println("Respuesta enviada a cliente " + (i + 1));
            }

            // Procesar grupos y enviar coordenadas a los vecinos
            for (int i = 0; i < clientes.size(); i++) {
                ClienteInfo cliente = clientes.get(i);
                String mensaje = "Cliente " + (i + 1) + " coordenadas: " + cliente.coordenadas;

                // Enviar a los vecinos
                for (int j = 1; j <= V; j++) {
                    int vecinoIndex = (i + j) % clientes.size(); // Ciclo a través de los vecinos
                    ClienteInfo vecino = clientes.get(vecinoIndex);
                    DatagramPacket paqueteVecino = new DatagramPacket(mensaje.getBytes(), mensaje.length(), vecino.direccion, vecino.puerto);
                    socketUDP.send(paqueteVecino);
                    System.out.println("Enviando coordenadas a vecino " + (vecinoIndex + 1));
                }
            }

            System.out.println("Simulación completada tras " + S + " iteraciones.");
            socketUDP.close();
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // Clase interna para almacenar la información de cada cliente
    static class ClienteInfo {
        java.net.InetAddress direccion;
        int puerto;
        String coordenadas;

        ClienteInfo(java.net.InetAddress direccion, int puerto, String coordenadas) {
            this.direccion = direccion;
            this.puerto = puerto;
            this.coordenadas = coordenadas;
        }
    }
}
