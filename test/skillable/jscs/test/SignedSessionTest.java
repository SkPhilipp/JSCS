package skillable.jscs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import skillable.jscs.SignedSession;

public class SignedSessionTest {

	@Test
	public void test() {
		try {
			// Initialize data
			SignedSession session = new SignedSession();
			session.put("1", 12345);
			session.put("2", 12346);
			session.put("3", 12347);
			// Convert to cookie and back
			session = SignedSession.fromCookie(session.toCookie());
			// Check data
			assertEquals(12345, session.get("1"));
			assertEquals(12346, session.get("2"));
			assertEquals(12347, session.get("3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}

}
