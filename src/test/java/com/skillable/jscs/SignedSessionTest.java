package com.skillable.jscs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.servlet.http.Cookie;

import org.junit.Test;

import com.skillable.jscs.SignedSession;
import com.skillable.jscs.SignedSessionFactory;

public class SignedSessionTest {

	@Test
	public void test() {
		try {
			// Create factory and insert data.
			SignedSessionFactory factory = new SignedSessionFactory("CSPRNG Seed", "SessionCookie", 0L);
			SignedSession session = factory.createSession();
			session.put("1", 12345);
			session.put("2", 12346);
			session.put("3", 12347);
			// Convert to cookie and back, then check.
			Cookie cookie = session.toCookie();
			session = factory.fromCookie(cookie);
			assertEquals(12345, session.get("1"));
			assertEquals(12346, session.get("2"));
			assertEquals(12347, session.get("3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception occurred");
		}
	}

}
