package uk.co.asepstrath.bank;

import java.math.BigDecimal;

public class Transaction {
	private final String timestamp;
	private final String sender, id, recipient, type;
	private final BigDecimal amount;
	private boolean is_processed;

	public Transaction(String timestamp, BigDecimal amount, String sender, String id, String recipient, String type) {
		this.timestamp = timestamp;
		this.amount = amount;
		this.sender = sender;
		this.id = id;
		this.recipient = recipient;
		this.type = type;
		this.is_processed = false;
	}

	public String getTimestamp() {
		return this.timestamp;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public String getSender() {
		return this.sender;
	}

	public String getID() {
		return this.id;
	}

	public String getRecipient() {
		return this.recipient;
	}

	public String getType() {
		return this.type;
	}

	public boolean isProcessed() {
		return this.is_processed;
	}

	public void setProcessed(boolean processed) {
		this.is_processed = processed;
	}

	public String toString() {
		return "Transaction(\"" + this.timestamp + "\", " + this.getAmount().doubleValue() + ", \"" + this.getSender() + "\", \"" + this.getID() + "\", \"" + this.getRecipient() + "\", \"" + this.getType() + "\", "+this.isProcessed()+")";
	}
}