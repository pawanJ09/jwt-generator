package com.generator.jwt;

import com.generator.jwt.util.Constants;
import com.generator.jwt.util.GlobalProps;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.MessageFormat;
import java.util.Objects;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will generate the JWT Bearer token to be used to get the access token with your
 * OAuth Token provider.
 *
 * @author - Pawan Jaiswal
 */
public class JwtGenerator {

  private static final Logger log = LoggerFactory.getLogger(JwtGenerator.class);

  private static final GlobalProps globalProps = GlobalProps.getInstance();

  private static JwtGenerator instance;

  private long timeToExpire = 0L;

  private StringBuffer token;

  private JwtGenerator() {
    token = new StringBuffer();
  }

  /**
   * This method will return instance of the JwtGenerator class.
   *
   * @return instance JwtGenerator
   */
  public static JwtGenerator getInstance() {
    if (Objects.isNull(instance)) {
      log.debug("JwtGenerator instance created");
      instance = new JwtGenerator();
    }
    return instance;
  }

  public static void main(String[] args) {
    JwtGenerator generator = JwtGenerator.getInstance();
    generator.generateJwt();
  }

  /**
   * This method will generate the JWT token based on the client id and ISU.
   *
   * @return token String
   */
  public String generateJwt() {
    String header = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
    String claimTemplate = "'{'\"iss\": \"{0}\", \"sub\": \"{1}\", \"aud\": \"{2}\", "
        + "\"exp\": \"{3}\"'}'";
    try {
      //Determine new JWT token only if time is expired
      log.debug("timeToExpire {}, Current Time {}", timeToExpire,
                (System.currentTimeMillis() / 1000));
      if (timeToExpire < (System.currentTimeMillis() / 1000)) {
        token = new StringBuffer();
        //Encode the JWT Header and add it to our string to sign
        token.append(Base64.encodeBase64URLSafeString(header.getBytes("UTF-8")));
        //Separate with a period
        token.append(".");
        //Create the JWT Claims Object
        String[] claimArray = new String[4];
        if (Objects.isNull(globalProps.getEnv().get(Constants.CLIENT_ID))
            || Objects.isNull(globalProps.getEnv().get(Constants.ISU_NAME))) {
          log.error("Client Id or ISU not found");
          return null;
        } else if (Objects.isNull(globalProps.getEnv().get(Constants.KEYSTORE_LOCATION))
            || Objects.isNull(globalProps.getEnv().get(Constants.KEYSTORE_ALIAS))
            || Objects.isNull(globalProps.getEnv().get(Constants.KEYSTORE_PASSWORD))) {
          log.error("Keystore details not found");
          return null;
        }
        claimArray[0] = globalProps.getEnv().get(Constants.CLIENT_ID);
        claimArray[1] = globalProps.getEnv().get(Constants.ISU_NAME);
        claimArray[2] = globalProps.getEnv().get(Constants.AUD);
        timeToExpire = (System.currentTimeMillis() / 1000)
            + (globalProps.getEnv().get(Constants.JWT_EXPIRE) != null
               ? Integer.parseInt(globalProps.getEnv().get(Constants.JWT_EXPIRE)) :
               Constants.JWT_EXPIRE_DEFAULT);
        claimArray[3] = Long.toString(timeToExpire);
        MessageFormat claims;
        claims = new MessageFormat(claimTemplate);
        String payload = claims.format(claimArray);
        log.debug("Payload {}", payload);
        //Add the encoded claims object
        token.append(Base64.encodeBase64URLSafeString(payload.getBytes("UTF-8")));
        //Load the private key from a keystore
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(new FileInputStream(
                          globalProps.getEnv().get(Constants.KEYSTORE_LOCATION)),
                      globalProps.getEnv().get(Constants.KEYSTORE_PASSWORD).toCharArray());
        PrivateKey privateKey = (PrivateKey) keystore.getKey(
            globalProps.getEnv().get(Constants.KEYSTORE_ALIAS),
            globalProps.getEnv().get(Constants.KEYSTORE_PASSWORD).toCharArray());
        Certificate cert = keystore.getCertificate(
            globalProps.getEnv().get(Constants.KEYSTORE_ALIAS));
        PublicKey publicKey = cert.getPublicKey();
        JWK jwk = new RSAKey.Builder((RSAPublicKey) publicKey).privateKey(privateKey).build();
        log.debug("JWK Generated {}", jwk.toJSONString());
        //Sign the JWT Header + "." + JWT Claims Object
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(token.toString().getBytes("UTF-8"));
        String signedPayload = Base64.encodeBase64URLSafeString(signature.sign());
        //Separate with a period
        token.append(".");
        //Add the encoded signature
        token.append(signedPayload);
        log.debug("Generated JWT {}", token.toString());
      } else {
        log.debug("Used JWT {}", token.toString());
      }
    } catch (Exception e) {
      log.error("Error encountered when generating JWT {}", e.getMessage());
    }
    return token.toString();
  }
}
