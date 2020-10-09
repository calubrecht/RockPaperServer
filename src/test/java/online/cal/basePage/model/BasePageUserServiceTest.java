package online.cal.basePage.model;

import static org.mockito.Mockito.*;

import java.security.*;
import java.security.spec.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

import online.cal.basePage.*;

@RunWith(MockitoJUnitRunner.class)
public class BasePageUserServiceTest
{
	@InjectMocks
	BasePageUserService underTest_;

	@Mock
	DBStore dbStore_;

	@Mock
	ChatStore chatStore_;
	
	@Mock
	WebSecurityConfig securityService_;
	
	@After
	public void tearDown()
	{
		BasePageUserService.INSTANCE = null;
	}
	
	@BeforeClass
	public static void prepare()
	{
		BasePageUserService.INSTANCE = null;		
	}

	@Test
	public void testRegister()
	{
		// Do nothing token generator to mock out JwtUtils.generateToken
		underTest_.tokenGenerator_ = userName -> userName;
		BasePageUser user = new BasePageUser("FakeUser", "FakePass");
		user.setColor("puce");
		underTest_.register(user);
		
		assertTrue(underTest_.users_.containsKey("FakeUser"));
		
		assertEquals("puce", underTest_.users_.get("FakeUser").getColor());
		// Stored password should be hashed, not plaintext
		assertNotEquals("FakePass", underTest_.getUsers().get(0).getPassword());
		verify(dbStore_, times(1)).insertOne(eq("users"), any());
	}
	
	@Test
	public void testPasswordHash() throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasePageUser user = underTest_.generateUser("FakeUser", "FakePass");
		assertEquals("FakeUser", user.getUserName());
		assertFalse(user.validatePassword("NotPassword"));
		assertTrue(user.validatePassword("FakePass"));
	}

}
