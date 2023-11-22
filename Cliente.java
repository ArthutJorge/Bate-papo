import java.util.Scanner;

import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;

public class Cliente {
    public static void main(String[] args) {
        new Cliente().iniciarCliente(2023);
    }

    public void iniciarCliente(int porta) {
        try (Socket socket = new Socket("localhost", porta);
             ObjectOutputStream saida = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream entrada = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            // \u001b é o código de cor verde só para fins de estilo
            System.out.println("\u001B[32mDigite /sair caso deseje se desconectar");
            System.out.println("Caso deseje enviar uma mensagem privada, digite /sussurar (username) (mensagem)");

            Thread ThreadMensagens = new Thread(() -> {
                try {
                    while (true) {
                        String menssagem = scanner.nextLine(); // scanner que lê as mensagens do usuário
                        saida.writeObject(menssagem);

                        if ("/sair".equals(menssagem)) { // caso o usuário digite /sair, o programa para
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ThreadMensagens.start(); // inicia a Thread

            while (true) { // recebe as mensagens
                String menssagem = (String) entrada.readObject();
                System.out.println(menssagem);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Servidor não está ligado");
        }
    }
}