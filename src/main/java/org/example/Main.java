package org.example;

import org.example.entities.Message;
import org.example.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MongoController mongo = new MongoController(); // nome de variável em minúsculo


        // Pega a CHAVE de CRIPTO/DESCRIPTO das mensagens....
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("secrets.properties"))) {
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String key = p.getProperty("CHAT_AES_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente CHAT_AES_KEY");
        }

        System.out.println("=== Chat Seguro ===");

        User user = null;

        // Cadastro ou login
        while (user == null) {
            System.out.print("Você já tem um usuário? (s/n): ");
            String temUsuario = sc.nextLine().trim().toLowerCase();

            if (temUsuario.equals("n")) {
                // Cadastro
                System.out.println("=== Cadastro de Usuário ===");
                System.out.print("Digite seu nickname: ");
                String nickname = sc.nextLine();
                System.out.print("Digite seu email: ");
                String email = sc.nextLine().trim().toLowerCase();
                System.out.print("Digite sua senha: ");
                String password = sc.nextLine();

                // checa existência antes (UX); índice único ainda protege corrida
                if (mongo.userExistsByEmail(email)) {
                    System.out.println("Já existe uma conta com esse email.");
                    continue;
                }

                user = new User(nickname, email, password);
                boolean created = mongo.saveUser(user);

                if (!created) {
                    System.out.println("Esse email já está cadastrado.");
                    user = null;
                } else {
                    System.out.println("Usuário cadastrado com sucesso!");
                }

            } else {
                // Login
                System.out.println("=== Login ===");
                System.out.print("Digite seu nickname: ");
                String nickname = sc.nextLine();
                System.out.print("Digite seu email: ");
                String email = sc.nextLine().trim().toLowerCase();
                System.out.print("Digite sua senha: ");
                String password = sc.nextLine();

                user = mongo.authenticate(nickname, email, password);

                if (user == null) {
                    System.out.println("Usuário não encontrado ou senha incorreta! Tente novamente.");
                } else {
                    System.out.println("Usuário autenticado: " + user.getNickname());
                }
            }
        }

        // Loop principal do chat
        boolean running = true;

        while (running) {
            System.out.println("\n=== Menu ===");
            System.out.println("1. Enviar mensagem");
            System.out.println("2. Ver mensagens recebidas");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");
            String option = sc.nextLine().trim();

            switch (option) {
                case "1":
                    System.out.print("Digite o email do destinatário: ");
                    String toEmail = sc.nextLine().trim().toLowerCase();

                    // não permitir enviar para si mesmo
                    if (toEmail.equals(user.getEmail().trim().toLowerCase())) {
                        System.out.println("Você não pode enviar mensagem para você mesmo.");
                        break;
                    }

                    System.out.print("Digite a mensagem: ");
                    String text = sc.nextLine();

                    Message message = new Message(user.getEmail(), toEmail, text);
                    boolean sent = mongo.saveMessage(message, key);

                    if (!sent) {
                        System.out.println("Destinatário não encontrado ou erro ao enviar.");
                    } else {
                        System.out.println("Mensagem enviada para " + toEmail + "!");
                    }

                    break;

                case "2":
                    System.out.println("\nMensagens recebidas:");
                    List<Message> messages = mongo.getMessages(user.getEmail(), key);

                    if (messages.isEmpty()) {
                        System.out.println("Nenhuma mensagem encontrada.");
                    } else {
                        for (Message m : messages) {
                            System.out.println("De: " + m.getFromEmail() + " | Mensagem: " + m.getMessage());
                        }
                    }

                    break;

                case "3":
                    running = false;
                    System.out.println("Saindo do chat. Até mais!");
                    break;

                default:
                    System.out.println("Opção inválida! Digite 1, 2 ou 3.");
            }
        }
        sc.close();
    }
}
