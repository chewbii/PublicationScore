package fr.dvrc.types;

public class Quartile {
	public String category = null;
	public int quartile = 0;

	public Quartile(String category, int quartile) {
		this.category = category;
		this.quartile = quartile;
	}

	public String toString () {
		return category+"(Q"+quartile+")";
	}
}
