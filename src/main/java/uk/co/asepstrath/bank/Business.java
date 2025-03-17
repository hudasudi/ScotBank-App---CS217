package uk.co.asepstrath.bank;

public class Business {
	private final String id;
	private final String name;
	private final String category;
	private final boolean sanctioned;

	public Business(String id, String name, String category, boolean sanctioned) {
		this.id = id;
		this.name = name;
		this.category = category;
		this.sanctioned = sanctioned;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getCategory() {
		return this.category;
	}

	public boolean isSanctioned() {
		return this.sanctioned;
	}

	public String toString() {
		return "Business(\"" + this.id + "\", \"" + this.name + "\", \"" + this.category + "\", " + this.sanctioned + ")";
	}
}
