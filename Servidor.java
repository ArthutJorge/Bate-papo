import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

public class Servidor {
    // cria lista pois é mais fácil de adicionar e remover itens com esse tipo
    private final List<GerenciadorCliente> clientes = new ArrayList<>(); 

    public static void main(String[] args) {
        new Servidor().iniciarServidor(2023);
    }

    public void iniciarServidor(int porta) {
        try (ServerSocket serverSocket = new ServerSocket(porta)) { // cria socket para a porta
            // \u001B[34m é o código para a cor azul
            System.out.println("\u001B[34m Servidor foi aberto na porta " + porta + "\u001B[0m");

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("\u001B[32m Novo cliente conectado: " + clienteSocket + "\u001B[0m");

                GerenciadorCliente gerenciadorCliente = new GerenciadorCliente(clienteSocket);
                clientes.add(gerenciadorCliente); // adiciona na lista cliente
                new Thread(gerenciadorCliente).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mensagensPublicas(String menssagem, GerenciadorCliente enviador) throws IOException {
        for (GerenciadorCliente cliente : clientes) { // envia mensagem para todos usuários
            if (cliente != enviador) { // para o usuário não receber a própria mensagem
                cliente.saida.writeObject(enviador.getUsername() + ": " + menssagem);
            }
        }
    }

    private void mensagensPrivadas(String menssagem, GerenciadorCliente enviador) {
        String[] partes = menssagem.split(" ", 3); // divide a mensagem em várias partes para obter se foi privada ou não
        if (partes.length == 3) {
            String username = partes[1];
            String mensagemPrivada = partes[2];

            for (GerenciadorCliente cliente : clientes) {
                if (cliente != enviador && cliente.getUsername().equals(username)) {
                    try {
                        cliente.saida.writeObject("\u001B[35mMensagem privada de " + enviador.getUsername() + ": " + mensagemPrivada + "\u001B[0m");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            try {
                // \U001B[31m é o código para a cor vermelha
                enviador.saida.writeObject("\u001B[31m O usuário " + username + "não foi encontrado :( \u001B[0m");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removerCliente(GerenciadorCliente cliente) {
        clientes.remove(cliente);
        System.out.println("\u001B[31m cliente desconectado: " + cliente.getclienteSocket() + "\u001B[0m");
    }

    private class GerenciadorCliente implements Runnable {
        private final Socket clienteSocket;
        private final ObjectOutputStream saida;
        private final ObjectInputStream entrada;
        private String username;

        public GerenciadorCliente(Socket socket) throws IOException {
            this.clienteSocket = socket;
            this.saida = new ObjectOutputStream(socket.getOutputStream());
            this.entrada = new ObjectInputStream(socket.getInputStream());
        }

        public Socket getclienteSocket() {
            return clienteSocket;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public void run() {
            try {
                saida.writeObject("Digite o seu username: \u001B[0m");
                this.username = (String) entrada.readObject();
                saida.writeObject("\u001B[32mBem vindo ao bate papo " + username + "! \u001B[0m");

                while (true) {
                    String menssagem = (String) entrada.readObject();
                    String[] partes = menssagem.split(" ", 3);
                    if (partes[0].equals("/sussurrar")) {
                        mensagensPrivadas(menssagem, this);
                    } else {
                        mensagensPublicas(menssagem, this);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                removerCliente(this);
            }
        }
    }
}