package skillable.jscs.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import skillable.jscs.SignedSession;

public class SignedSessionTest {

	@Test
	public void test() {
		SignedSession session = new SignedSession();
		session.put("1", 12345);
		session.put("2", 12346);
		session.put("3", 12347);

		try {
			SignedSession session2 = SignedSession.fromCookie(session
					.toCookie());
			assertEquals(12345, session2.get("1"));
			assertEquals(12346, session2.get("2"));
			assertEquals(12347, session2.get("3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}

}
