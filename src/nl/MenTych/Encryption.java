package nl.MenTych;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

public class Encryption {
    private Cipher cipher;

    public Encryption() {
        new Guard(2048);

        try {
            this.cipher = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivate() throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(Guard.PRIVATEKEYPATH).toPath());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public PublicKey getPublic() {
        try {
            byte[] keyBytes = Files.readAllBytes(new File(Guard.PUBLICKEYPATH).toPath());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey getfromStringPublic(String key) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(key.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encryptText(String msg) {
        try {
            this.cipher.init(Cipher.ENCRYPT_MODE, this.getPublic());
            return new String (Base64.encodeBase64(cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String decryptText(String msg) {
        try {
            this.cipher.init(Cipher.DECRYPT_MODE, this.getPrivate());
            return new String(cipher.doFinal(Base64.decodeBase64(msg.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String encryptText(String msg, PublicKey key) {
        try {
            System.out.println("text to encrypt: " + msg);
            this.cipher.init(Cipher.ENCRYPT_MODE, key);
            return new String (Base64.encodeBase64(cipher.doFinal(msg.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
