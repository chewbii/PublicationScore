package fr.dvrc.types;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.dvrc.ES;

public class WikiCfp extends Type{
    public final static Logger log = Logger.getLogger("Wikicfp");
	public String id = null;
	public String CORE_RANK = null;
	public String description = null;
	public String url = null;
	public String FoR = null;
	public String locality;
	public Date startDate = null;
	public Date endDate = null;
	public Date deadline = null;
	public String[] categories = null;
	public String CfP = null;
	public String resources = null;
	public Date finalVersion = null;
	public Date notification = null;

	protected static GeoCoding geocoding = null;
	
	static {
		geocoding = GeoCoding.getInstance();
	}
	
	public WikiCfp(String identifier) {
		id = identifier.substring(identifier.indexOf("eventid=")+8);
	}

	public String toString () {
		return id +" ("+title+": "+CORE_RANK+" / "+startDate+ " / "+endDate + " / "+deadline +") "+categories;
	}

	public JSONObject toJSON () {
		SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		JSONObject o = new JSONObject ();
		putField(o, "id", id);
		putField(o, "title", title);
		try{putField(o, "startDate", sm.format(startDate));}catch(Exception e) {}
		try{putField(o, "endDate", sm.format(endDate));}catch(Exception e) {}
		try{putField(o, "deadline", sm.format(deadline));}catch(Exception e) {}
		try{putField(o, "notification", sm.format(notification));}catch(Exception e) {}
		try{putField(o, "finalVersion", sm.format(finalVersion));}catch(Exception e) {}
		putField(o, "CfP", CfP);
		putField(o, "address", locality);
		putField(o, "coord", geocoding.getCoordinates(locality));
		putArray(o, "categories", categories);
		putField(o, "CORE", CORE_RANK);
		putField(o, "url", url);
		return o;
	}

	public void CORE () {
		JSONObject o = CORE(title);
		if(o == null)
			return;
		CORE_RANK  = (String)o.get("CORE");
	}
	
	protected JSONObject CORE (String name) {
		try{
			JSONObject o = ES.query(query("shortName", name, "conference"));
			JSONArray array = ((JSONArray)((JSONObject)o.get("hits")).get("hits"));
			if(array.size() > 0)
				return (JSONObject)((JSONObject)array.get(0)).get("_source");
			else
				return null;
		} catch (Exception e) {
			log.error(name+" "+e.toString());
			return null;
		}

	}

	protected String query (String field, String name, String type) {
		StringBuffer sb = new StringBuffer ("{\"query\": {\"bool\": {\"must\": [\n\t\t");
		sb.append("{\"match_phrase\":{\""+field+"\": \""+name+"\"}},");
		sb.append("{\"match_phrase\":{\"type\": \""+type+"\"}}");
		sb.append("]}}}");
		return sb.toString();
	}
}
