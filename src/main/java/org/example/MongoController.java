package org.example;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.MongoWriteException;
import com.mongodb.ErrorCategory;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.entities.Message;
import org.example.entities.User;
import org.example.utils.CryptoUtils;
import org.example.utils.PasswordUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MongoController {

    private final MongoClient client;

    public MongoController() {

        // Pega a URL do MONGO....
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("secrets.properties"))) {
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String mongoUrl = p.getProperty("MONGO_URL");
        if (mongoUrl == null || mongoUrl.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente MONGO_URL");
        }

        this.client = MongoClients.create(mongoUrl);

        // garante índice único por email...
        try {
            MongoDatabase db = connect();
            MongoCollection<Document> users = db.getCollection("users");
            users.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        } catch (Exception ignored) {}
    }

    private MongoDatabase connect() {
        return client.getDatabase("chat");
    }

    // Valida se o email é de um usuario no banco...
    public boolean userExistsByEmail(String email) {
        MongoDatabase db = connect();
        MongoCollection<Document> users = db.getCollection("users");

        String emailNorm = email == null ? null : email.trim().toLowerCase();

        return users.find(Filters.eq("email", emailNorm)).limit(1).first() != null;
    }

    // Salvar usuário no banco...
    public boolean saveUser(User user) {
        MongoDatabase db = connect();
        MongoCollection<Document> users = db.getCollection("users");

        // gera salt + hash
        String saltB64 = PasswordUtils.generateSaltB64();
        String hashB64 = PasswordUtils.hashToB64(user.getPassword(), saltB64);

        Document doc = new Document()
                .append("nickname", user.getNickname())
                .append("email", user.getEmail().trim().toLowerCase())
                .append("password_hash", hashB64)
                .append("password_salt", saltB64)
                .append("password_algo", PasswordUtils.ALGO)
                .append("password_iter", PasswordUtils.ITERATIONS);

        try {
            users.insertOne(doc);
            return true;
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) {
                return false; // email já existe
            }
            throw e;
        }
    }

    // Autenticação do usuário
    public User authenticate(String nickname, String email, String password) {
        MongoDatabase db = connect();
        MongoCollection<Document> users = db.getCollection("users");

        Document doc = users.find(Filters.and(
                Filters.eq("nickname", nickname),
                Filters.eq("email", email.trim().toLowerCase())
        )).first();

        if (doc == null) return null;

        // preferir caminho moderno (hash/salt)
        String hash = doc.getString("password_hash");
        String salt = doc.getString("password_salt");

        boolean ok;
        if (hash != null && salt != null) {
            ok = PasswordUtils.verify(password, salt, hash);
        } else {
            // legado: senha em texto (campo "password")
            String legacy = doc.getString("password");
            ok = legacy != null && legacy.equals(password);

            if (ok) {
                // upgrade transparente para hash/salt e remove o campo antigo
                String newSalt = PasswordUtils.generateSaltB64();
                String newHash = PasswordUtils.hashToB64(password, newSalt);
                ObjectId id = doc.getObjectId("_id");
                users.updateOne(Filters.eq("_id", id), Updates.combine(
                        Updates.set("password_hash", newHash),
                        Updates.set("password_salt", newSalt),
                        Updates.set("password_algo", PasswordUtils.ALGO),
                        Updates.set("password_iter", PasswordUtils.ITERATIONS),
                        Updates.unset("password")
                ));
            }
        }

        if (!ok) return null;

        // senha não é mais necessária no objeto User
        return new User(
                doc.getString("nickname"),
                doc.getString("email"),
                "***"
        );
    }

    // Salvar mensagem criptografada
    public boolean saveMessage(Message message, String key) {
        MongoDatabase db = connect();

        if (!userExistsByEmail(message.getToEmail().trim().toLowerCase())) {
            return false;
        }

        MongoCollection<Document> messages = db.getCollection("message");

        String encryptedText = CryptoUtils.cifrarMensagem(key, message.getMessage());
        if (encryptedText == null) {
            return false;
        }

        Document doc = new Document()
                .append("email_from", message.getFromEmail().trim().toLowerCase())
                .append("email_to", message.getToEmail().trim().toLowerCase())
                .append("message", encryptedText);

        messages.insertOne(doc);

        return true;
    }

    // Recuperar mensagens e descriptografar
    public List<Message> getMessages(String emailTo, String key) {
        MongoDatabase db = connect();
        MongoCollection<Document> messages = db.getCollection("message");

        List<Message> result = new ArrayList<>();
        for (Document doc : messages.find(Filters.eq("email_to", emailTo.trim().toLowerCase()))) {
            String decryptedText = CryptoUtils.decifrarMensagem(key, doc.getString("message"));
            if (decryptedText == null) {
                decryptedText = "(erro ao decifrar)";
            }

            Message msg = new Message(
                    doc.getString("email_from"),
                    doc.getString("email_to"),
                    decryptedText
            );

            result.add(msg);
        }

        return result;
    }
}
