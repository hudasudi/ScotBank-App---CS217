package uk.co.asepstrath.bank.object_tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.asepstrath.bank.Transaction;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTests {
	private Transaction t;

	@BeforeEach
	void setUp() {
		t = new Transaction("TIME", BigDecimal.ZERO, "SENDER", "ID", "RECEIVER", "TYPE");
	}

	@Test
	public void createTransaction() {
		assertNotNull(t);
	}

	@Test
	public void hasTime() {
		assertNotNull(t.getTimestamp());
		assertEquals("TIME", t.getTimestamp());
	}

	@Test
	public void hasAmount() {
		assertNotNull(t.getAmount());
		assertEquals(BigDecimal.ZERO, t.getAmount());
	}

	@Test
	public void hasSender() {
		assertNotNull(t.getSender());
		assertEquals("SENDER", t.getSender());
	}

	@Test
	public void hasReceiver() {
		assertNotNull(t.getRecipient());
		assertEquals("RECEIVER", t.getRecipient());
	}

	@Test
	public void hasType() {
		assertNotNull(t.getType());
		assertEquals("TYPE", t.getType());
	}

	@Test
	public void toStringTest() {
		assertNotNull(t.toString());
		assertEquals("Transaction(\"TIME\", 0.0, \"SENDER\", \"ID\", \"RECEIVER\", \"TYPE\", false)", t.toString());
	}

	@Test
	public void isProcessedTest() {
		assertFalse(t.isProcessed());
	}

	@Test
	public void setProcessedTest() {
		assertFalse(t.isProcessed());

		t.setProcessed(true);

		assertTrue(t.isProcessed());
	}
}
