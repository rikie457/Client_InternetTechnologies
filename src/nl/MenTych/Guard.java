package nl.MenTych;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;

public class Guard {
    private KeyPairGenerator keyGen;
    private KeyPair pair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    final static String PRIVATEKEYPATH = "KeyPair/privateKey";
    final static String PUBLICKEYPATH = "KeyPair/publicKey";

    public Guard(int keylength) {
        try {
            this.keyGen = KeyPairGenerator.getInstance("RSA");
            this.keyGen.initialize(keylength);
            this.createKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public void createKeys() {
        this.pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();

        try {
            this.writeToFile(PUBLICKEYPATH, this.getPublicKey().getEncoded());
            this.writeToFile(PRIVATEKEYPATH, this.getPrivateKey().getEncoded());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void writeToFile(String path, byte[] key) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(key);
        fos.flush();
        fos.close();
    }
}
