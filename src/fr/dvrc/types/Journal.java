package fr.dvrc.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Journal extends Type {
	public boolean WoS = false;
	public boolean WoS_E = false;
	public List<String> categories = new ArrayList<String> ();
	public Map<String, Quartile> SJR = new HashMap<String, Quartile> ();
	public String ISSN = null;
	public String eISSN = null;

	public static List<String> WoS_E_categories = new ArrayList<String> ();
	
	public Journal() {
	}

	public Journal(JSONObject o) {
		super(o);
		WoS = (Boolean)o.get("WoS");
		WoS_E = (Boolean)o.get("WoSE");
		//TODO à finir
	}

	public String toString () {
		return name+" ("+ISSN+" / "+publisher+") "+categories+(WoS_E?" ESILV":"")+" - "+SJR.values();
	}

	public JSONObject toJSON () {
		JSONObject o = new JSONObject ();
		setJSON (o);
		o.put("type", "journal");
		o.put("WoS", new Boolean(WoS));
		o.put("WoSE", new Boolean(WoS_E));
		if(categories.size() > 0) {
			JSONArray a = new JSONArray ();
			a.addAll(categories);
			o.put("categories", a);
		}
		if(eISSN!= null)
			o.put("eISSN", eISSN);
		if(SJR.size() > 0) {
			JSONArray a = new JSONArray ();
			for(String c : SJR.keySet()) {
				JSONObject q = new JSONObject ();
				q.put("category", c);
				q.put("quartile", SJR.get(c).quartile);
				a.add(q);
			}
			o.put("SJR", a);
		}

		return o;
	}
}
