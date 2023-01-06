package fr.dvrc.types;

import org.json.simple.JSONObject;

public class Conference extends Type{
	public final static int WORKSHOP = 3;
	public final static int DEMO = 2;
	public final static int LONG = 1;
	public int confType = LONG;

	public String shortName = null;
	public String CORE_RANK = null;

	public String FoR = null;
	public Conference(String shortName) {
		this.shortName = shortName;
	}

	public String toString () {
		return shortName +" ("+CORE_RANK+") "+FoR;
	}

	public JSONObject toJSON () {
		JSONObject o = new JSONObject ();
		setJSON (o);
		o.put("type", "conference");
		if(shortName != null)
			o.put("shortName", shortName);
		if(CORE_RANK != null)
			o.put("CORE", CORE_RANK);
		if(FoR != null)
			o.put("FoR", FoR);
		switch(confType) {
		case LONG: o.put("conf_type","long");break;
		case DEMO: o.put("conf_type","demo");break;
		case WORKSHOP: o.put("conf_type","workshop");break;
		}

		return o;
	}
}
