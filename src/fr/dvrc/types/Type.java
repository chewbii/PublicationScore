package fr.dvrc.types;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public abstract class Type {
	public static int NAT = 1;
	public static int INT = 2;
	public int impact = INT;

	public int year;
	public int pages;
	
	public int h_index = -1;

	public List<String> domains = new ArrayList<String> ();
	public boolean withReviewingCommittee = true;

	public String name;	
	public String publisher = null;
	public String title = null;

	public Type() {
	}
	public Type(JSONObject o) {
		if(((String)o.get("impact")).compareTo("international") == 0)
			impact = INT;
		else
			impact = NAT;
		publisher = (String)o.get("publisher");
		name = (String)o.get("name");
	}

	protected void setJSON (JSONObject o) {
		if(year > 0)
			o.put("year", year);
		o.put("name", name);
		if(title != null)
			o.put("title", title);
		if(publisher != null)
			o.put("publisher", publisher);
		if(impact == INT)
			o.put("impact", "international");
		else
			o.put("impact", "national");
		if(pages > 0)
			o.put("pages", pages);
		if(domains.size() > 0) {
			JSONArray a = new JSONArray ();
			a.addAll(domains);
			o.put("domains", a);
		}
	}

	protected void putField (JSONObject o, String field, Object value) {
		if(value != null) {
			if(value instanceof Date)
				o.put(field, value.toString());
			else
				o.put(field, value);
		}
	}
	protected void putArray (JSONObject o, String field, String [] values) {
		if(values != null) {
			JSONArray array = new JSONArray();
			for(int i = 0; i < values.length; i++) {
				array.add(values[i]);
			}
			o.put(field, array);
		}
	}

	public abstract JSONObject toJSON ();
}
