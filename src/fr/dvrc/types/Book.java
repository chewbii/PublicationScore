package fr.dvrc.types;

import org.json.simple.JSONObject;

public class Book extends Type {
	public static int PHD = 1;
	public static int HDR = 2;
	public static int BOOK = 3;
	public static int BOOK_CHAPTER = 4;
	public static int PROCEEDINGS = 5;
	public int type = PHD;

	public String university = null;

	public Book() {
		impact = NAT;
	}
	public JSONObject toJSON () {
		JSONObject o = new JSONObject ();
		setJSON (o);
		if(type == PHD)
			o.put("type", "phdthesis");
		else if(type == HDR)
			o.put("type", "HDR");
		else if(type == BOOK_CHAPTER)
			o.put("type", "BOOK_CHAPTER");
		else if(type == PROCEEDINGS)
			o.put("type", "PROCEEDINGS");
		else
			o.put("type", "BOOK");
		if(university != null)
			o.put("university", university);

		return o;
	}

}
