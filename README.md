# Mongo Chat

**o que é**  
um mini app de chat que roda no terminal. você cria uma conta, faz login, envia mensagens e lê o que recebeu.

**o que dá pra fazer**  
- criar conta e entrar  
- enviar mensagens para outro e-mail  
- ver suas mensagens recebidas  

**como ele protege você**  
- senhas não ficam em texto; são guardadas com segurança  
- mensagens ficam “trancadas” com uma chave simétrica
- tudo é salvo em um banco de dados  

**como usar**  
1. tenha o **java** instalado  
2. No `secrets.properties` da raiz coloque suas infos:
   ```properties
   CHAT_AES_KEY=1234567890123456 # Pode manter essa
   MONGO_URL=mongodb+srv://usuario:senha@cluster.mongodb.net/.....
3. rode a classe principal `Main` pela sua ide ou via linha de comando
