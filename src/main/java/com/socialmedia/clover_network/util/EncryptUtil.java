packpackage com.socialmedia.clover_network.util;

public class TripleDesEncryptionUtil {
  private static final String DESEDE_ENCRYPTION_SCHEME_CIPHER = "DESede/ECB/PKCS5Padding";
  private static final String UNICODE_FORMAT = "UTF8";
  private static final String DESEDE_ENCRYPTION_SCHEME_KEY = "DESede";

public TripleDesEncryptionUtil() throws Exception {
        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest(encryptionSecretKey.getBytes(UNICODE_FORMAT));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }
        key = new SecretKeySpec(keyBytes, DESEDE_ENCRYPTION_SCHEME_KEY);



        final byte[] digestOfPassword2 = md.digest(encryptionSecretKeyV2.getBytes(UNICODE_FORMAT));
        final byte[] keyBytes2 = Arrays.copyOf(digestOfPassword2, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes2[k++] = keyBytes2[j++];
        }
        key2 = new SecretKeySpec(keyBytes2, DESEDE_ENCRYPTION_SCHEME_KEY);

        iv = new IvParameterSpec(new byte[8]);
    }
  
  public static String encrypt(String key, String str) throws Exception {
        Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME_CIPHER);
        SecretKey secretKey1 = new SecretKeySpec(key.getBytes(UNICODE_FORMAT), DESEDE_ENCRYPTION_SCHEME_KEY);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey1);
        byte[] bytes = cipher.doFinal(str.getBytes(UNICODE_FORMAT));
        byte[] base64Bytes = Base64.encodeBase64(bytes);
        return new String(base64Bytes);
    }


    public static String decrypt(String key, String str) throws Exception {
        byte[] data =Base64.decodeBase64(str);
        SecretKey secretKey1 = new SecretKeySpec(key.getBytes(UNICODE_FORMAT), DESEDE_ENCRYPTION_SCHEME_KEY);
        Cipher cipher = Cipher.getInstance(DESEDE_ENCRYPTION_SCHEME_CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, secretKey1);
        byte[] decryptedBytes = cipher.doFinal(data);
        return new String(decryptedBytes, UNICODE_FORMAT);
    }
}
