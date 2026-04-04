package com.skeeterSoftworks.WorkOrderCentral.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Manual helpers for {@code ENC(...)} values used in {@code application.properties} (e.g. {@code license.key}).
 * Configuration must stay identical to {@link com.skeeterSoftworks.WorkOrderCentral.WorkOrderCentralApplication#stringEncryptor()}.
 * <p>
 * Remove {@link Disabled} from the test you want to run, set the corresponding constant, then run that test from the IDE or Maven.
 */
class JasyptEncryptorDecryptorTest {

	private static final String PLAINTEXT_TO_ENCRYPT = "";

	/**
	 * Either the inner ciphertext only, or the full property value including {@code ENC(...)}.
	 */
	private static final String CIPHERTEXT_OR_ENC_PROPERTY = "";

	private static StringEncryptor runtimeStringEncryptor() {
		PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword("zmmHl223kRttf00dS");
		config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
		config.setKeyObtentionIterations("1000");
		config.setPoolSize("1");
		config.setProviderName("SunJCE");
		config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setStringOutputType("base64");
		encryptor.setConfig(config);
		return encryptor;
	}

	private static String unwrapEncWrapper(String value) {
		if (value == null) {
			return "";
		}
		String v = value.trim();
		if (v.startsWith("ENC(") && v.endsWith(")")) {
			return v.substring(4, v.length() - 1);
		}
		return v;
	}

	@Test
	@Disabled("Remove @Disabled, set PLAINTEXT_TO_ENCRYPT, then run this test to print an ENC(...) value for application.properties.")
	void encryptPlaintextForApplicationProperties() {
		Assumptions.assumeFalse(PLAINTEXT_TO_ENCRYPT.isBlank(), "Set PLAINTEXT_TO_ENCRYPT in this test class first.");
		String encrypted = runtimeStringEncryptor().encrypt(PLAINTEXT_TO_ENCRYPT);
		System.out.println("--- paste into application.properties ---");
		System.out.println("ENC(" + encrypted + ")");
		System.out.println("---");
	}

	@Test
	@Disabled("Remove @Disabled, set CIPHERTEXT_OR_ENC_PROPERTY, then run this test to print the decrypted string.")
	void decryptEncPropertyValue() {
		Assumptions.assumeFalse(CIPHERTEXT_OR_ENC_PROPERTY.isBlank(), "Set CIPHERTEXT_OR_ENC_PROPERTY in this test class first.");
		String cipherPayload = unwrapEncWrapper(CIPHERTEXT_OR_ENC_PROPERTY);
		String decrypted = runtimeStringEncryptor().decrypt(cipherPayload);
		System.out.println("--- decrypted ---");
		System.out.println(decrypted);
		System.out.println("---");
	}
}
