import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

public class Servidor {
    String usuario = "";
    String senhaSemente = "";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String IV = "1234567890123456";
    String salt = "321";
    String[] senhasGeradas = new String[5];
    ArrayList<String> listaSenhas = new ArrayList<>();

    public void main (String[]args) throws Exception {
        Scanner scan = new Scanner(System.in);
        System.out.println("Digite 1 para cadastro ou 2 para login");
        if(scan.nextInt() == 1){
            System.out.println("Digite seu nome de usuário:");
            this.usuario = scan.next();
            System.out.println("Digite a senha semente:");
            this.senhaSemente = hashSenha(scan.next());
            this.senhaSemente = hashSenha(this.senhaSemente + hashSenha(this.salt));
            cadastrar(this.usuario + ";" + this.senhaSemente + ";" + hashSenha(this.salt));
        }
        else{
            StringBuilder texto = new StringBuilder();
            try{
                File arquivo = new File("usuario2.txt");
                Scanner scanArquivo = new Scanner(arquivo);
                while (scanArquivo.hasNextLine()) {
                    texto.append(scanArquivo.nextLine());
                }
            }catch(FileNotFoundException e){
                System.out.println("Arquivo não encontrado");
            }
            String[] infosUser = texto.toString().split(";");
            this.senhaSemente = infosUser[1];
            String minutoAtual = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("mm"));
            System.out.println(minutoAtual);
            boolean deletarProximas = false;
            String tentativa = "";
            boolean testarSenha = false;
            int removidos = 0;
            this.senhaSemente += ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            int comp = 5;
            for (int i = 0; i < 5; i++) {
                this.senhaSemente = hashSenha(this.senhaSemente);
                this.listaSenhas.add(this.senhaSemente);
                this.senhasGeradas[i] = this.senhaSemente;
                System.out.println(this.senhaSemente);
            }
            while(true){
                System.out.println("Digite uma senha descartável:");
                tentativa = scan.next();
                if(!minutoAtual.equalsIgnoreCase(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("mm")))){
                    minutoAtual = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("mm"));;
                    System.out.println("Gerando novas senhas...");
                    this.senhaSemente += ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));;
                    this.senhasGeradas = new String[5];
                    this.listaSenhas = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        this.senhaSemente = hashSenha(this.senhaSemente);
                        this.listaSenhas.add(this.senhaSemente);
                    }
                    comp = 5;
                }
                if(this.listaSenhas.isEmpty()){
                    System.out.println("Não há mais possibilidades de senhas, aguarde pelo próximo minuto");
                }
                for (int i = 0; i < comp; i++) {
                    testarSenha = this.listaSenhas.get(i-removidos).equalsIgnoreCase(tentativa);
                    if(testarSenha){
                        System.out.println("Senha válida!");
                        deletarProximas = true;
                    }
                     if (deletarProximas) {
                        System.out.println("Deletando as consecutivas: " + this.listaSenhas.get(i-removidos));
                        this.listaSenhas.remove(i-removidos);
                         removidos++;
                    }
                }
                if(removidos == 0 && !this.listaSenhas.isEmpty()){
                    System.out.println("Senha inválida ou já utilizada!!");
                }
                removidos = 0;
                deletarProximas = false;
                comp = listaSenhas.size();
            }
        }
    }

    public void cadastrar(String baseServidor) throws Exception {
        try {
            FileWriter myWriter = new FileWriter("usuario2.txt");
            myWriter.write(baseServidor);
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
