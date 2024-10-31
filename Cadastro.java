import com.google.common.hash.Hashing;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;

public class Cadastro {
    String usuario = "";
    String senhaLocal = "";
    String senhaSemente = "";
    String salt = "321";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String IV = "1234567890123456";

    public void main(String[] args) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.println("Olá, digite seu nome de usuário:");
        this.usuario = scan.next();
        System.out.println("Digite a senha local:");
        this.senhaLocal = hashSenha(scan.next());
        this.senhaLocal = hashSenha(this.senhaLocal + hashSenha(this.salt));
        System.out.println(this.senhaLocal);
        System.out.println("Digite a senha semente:");
        this.senhaSemente = hashSenha(scan.next());
        this.senhaSemente = hashSenha(this.senhaSemente + hashSenha(this.salt));
        encriptar(this.usuario + ":" + this.senhaSemente, this.senhaLocal);
    }

    public void encriptar(String usuarioSemente, String local) throws Exception {
        SecretKey chaveSecreta = criaChave(local);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, chaveSecreta, iv);

        byte[] encryptedBytes = cipher.doFinal(usuarioSemente.getBytes());
        try {
            FileWriter myWriter = new FileWriter("usuario.txt");
            myWriter.write(Base64.getEncoder().encodeToString(encryptedBytes));
            myWriter.close();
            System.out.println("Cadastrado com sucesso.");
        } catch (IOException e) {
            System.out.println("Um erro ocorreu, tente novamente.");
            e.printStackTrace();
        }
    }
    private static SecretKeySpec criaChave(String chave) throws Exception {
        // Gera um hash SHA-256 da string para garantir o tamanho correto da chave (32 bytes)
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