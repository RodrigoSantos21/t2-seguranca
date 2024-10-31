import com.google.common.hash.Hashing;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class Aplicativo {
    String usuario = "";
    String senhaLocal = "";
    String senhaSemente = "";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String IV = "1234567890123456";
    String salt = "321";
    public void main(String[] args) throws Exception {
        StringBuilder texto = new StringBuilder();
        try{
            File arquivo = new File("usuario.txt");
            Scanner scanArquivo = new Scanner(arquivo);
            while (scanArquivo.hasNextLine()) {
                texto.append(scanArquivo.nextLine());
            }
        }catch(FileNotFoundException e){
            System.out.println("Arquivo não encontrado");
        }
        Scanner scan = new Scanner(System.in);
        System.out.println("Olá, digite seu nome de usuário:");
        this.usuario = scan.next();
        System.out.println("Digite a sua senha local para acessar o gerador de senhas:");
        this.senhaLocal = hashSenha(scan.next());
        this.senhaLocal = hashSenha(this.senhaLocal + hashSenha(this.salt));
        String[] infosUser = decriptar(texto.toString(), this.senhaLocal).split(":");
        if(this.usuario.equalsIgnoreCase(infosUser[0])){
            System.out.println("Login efetuado com sucesso!");
            this.senhaSemente = infosUser[1];
        }
        else{
            System.out.println("Usuário ou senha incorretos.");
            return;
        }
        while(true){
            this.senhaSemente += ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            System.out.println("Gerando 5 senhas válidas pelo próximo minuto:");
            for (int i = 0; i < 5; i++) {
                this.senhaSemente = hashSenha(this.senhaSemente);
                System.out.println("Senha " + (i+1) + ": " + this.senhaSemente);
            }
            Thread.sleep(60*1000);
        }
    }

    public String decriptar(String textoEncriptado, String chave) throws Exception {
        try{
            SecretKeySpec secretKey = criaChave(chave);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(textoEncriptado));
            return new String(decryptedBytes);
        }catch(BadPaddingException excecao){
            return "Erro";
        }
    }

    private static SecretKeySpec criaChave(String chave) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(chave.getBytes("UTF-8"));
        return new SecretKeySpec(key, "AES");
    }


    public String hashSenha(String senha) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] chave = sha.digest(senha.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : chave) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
