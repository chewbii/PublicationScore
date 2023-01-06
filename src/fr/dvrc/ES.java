package fr.dvrc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ES {
	private static JSONParser parser = null;
	private static JSONObject config;
	private static RestClient client = null;

	public static void config () {
		parser = new JSONParser();
		try {
			config = readJSONFile ("config.json");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getConfig (String key) {
		return (String)ES.config.get(key);
	}

	public static JSONObject readJSONFile(String file) throws Exception {
		parser.reset();
		BufferedReader br = new BufferedReader(new FileReader(file));
		Object obj = parser.parse(br);
		return (JSONObject) obj;
	}

	public static void open() {
		if (client == null || !client.isRunning())
			client = RestClient.builder(new HttpHost(ES.getConfig("ES"), new Long((Long)ES.config.get("ES_port")).intValue(), "http"), new HttpHost(ES.getConfig("ES"), ((Long)ES.config.get("ES_port")).intValue(), "http"))
					.build();
		create (ES.getConfig("ES_index_ACL"));
		create (ES.getConfig("ES_index_authors"));
		create (ES.getConfig("ES_index_WikiCfp"));
	}

	public static void close () {
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void create (String index) {
		Request request = new Request("put", "/"+index);
		try{
			Response response = client.performRequest(request);
		} catch (Exception e) {}
	}
	
	public static boolean upload2Elastic(JSONObject json, String id, boolean replace, int i, String index) throws Exception {
		if(id == null || id.length() == 0)
			return true;
		try {
            id = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
		id = id.replaceAll(" ", "_").replaceAll("\\/", "_");
		Request request;
		if(replace)
			request = new Request("POST", "/"+index+"/_doc/"+id);
		else if(!ES_existsDoc(id))
			request = new Request("POST", "/"+index+"/_doc/"+id);
		else {
			if(i%5000 == 0)
				System.out.println(i+ " done");
			return true;
		}

		request.setJsonEntity(json.toString());
		try{
			Response response = client.performRequest(request);
			System.out.println(i+"\t"+response);//+"\t"+txt);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(json);
			System.exit(0);
		}
		return true;
	}

	public static boolean ES_existsDoc (String id) throws Exception {
		Request request;
		request = new Request("GET", "/"+ES.getConfig("ES_index")+"/_doc/"+id+"?_source=false");
		try{
			Response response = client.performRequest(request);
			String responseBody = EntityUtils.toString(response.getEntity());
			JSONObject obj = (JSONObject)parser.parse(responseBody);
			if((Boolean)obj.get("found"))
				return true;
		} catch (Exception e) {}
		return false;
	}

	public static JSONObject query (String query) throws IOException, ParseException {
		Request request = new Request("GET", "/"+ES.getConfig("ES_index_ACL")+"/_search");
		request.setJsonEntity(query);
		Response response = client.performRequest(request);
		String responseBody = EntityUtils.toString(response.getEntity());
		JSONObject obj = (JSONObject)parser.parse(responseBody);
		return obj;
	}
	public ES() {
		// TODO Auto-generated constructor stub
	}

}
