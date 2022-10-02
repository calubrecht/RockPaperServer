package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BasePageUserTest {

	@Test
	public void testSerDes() throws JsonProcessingException {
		ObjectMapper jsonMapper = new ObjectMapper();

		BasePageUser user = new BasePageUser("Frank", "Henley");
		String userString = jsonMapper.writeValueAsString(user);
		// Empty values should not be serialized;
		assertFalse(userString.contains("salt"));
		assertFalse(userString.contains("color"));
		assertFalse(userString.contains("null"));
		// Password not serialied
		assertFalse(userString.contains("password"));

		BasePageUser deserUser = jsonMapper.readValue(userString, BasePageUser.class);

		assertEquals(user.getUserName(), deserUser.getUserName());
		assertEquals(null, deserUser.getPassword());
	}

	@Test
	public void validatePassword() throws Exception {

		String goodPassword = "good";
		byte[] salt = BasePageUser.generateSalt();
		String saltString = Base64.getEncoder().encodeToString(salt);
		String goodPasswordsHash = BasePageUser.hashPassword(goodPassword, salt);
		BasePageUser user = new BasePageUser("User", goodPasswordsHash, saltString);

		assertTrue(user.validatePassword(goodPassword));
		assertFalse(user.validatePassword("not the password"));
		assertFalse(user.validatePassword(null));
	}

}
