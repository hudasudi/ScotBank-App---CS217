package uk.co.asepstrath.bank.object_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.Business;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessTests {
	private Business b;

	@BeforeEach
	void setUp() {
		b = new Business("IDE", "NAME", "CATEGORY", false);
	}

	@Test
	public void createBusiness() {
		assertNotNull(b);
	}

	@Test
	public void hasID() {
		assertNotNull(b.getId());
		assertEquals("IDE", b.getId());
	}

	@Test
	public void hasName() {
		assertNotNull(b.getName());
		assertEquals("NAME", b.getName());
	}

	@Test
	public void hasCategory() {
		assertNotNull(b.getCategory());
		assertEquals("CATEGORY", b.getCategory());
	}

	@Test
	public void hasSanctioned() {
		assertFalse(b.isSanctioned());
	}

	@Test
	public void toStringTest() {
		assertNotNull(b.toString());
		assertEquals("Business(\"IDE\", \"NAME\", \"CATEGORY\", false)", b.toString());
	}
}