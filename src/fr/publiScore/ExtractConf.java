package fr.publiScore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.dvrc.ES;
import fr.dvrc.types.Conference;
import fr.dvrc.types.Type;

public class ExtractConf {
	private Map<String, Conference> confs = new HashMap<String, Conference> ();
	private Map<String, String> FoR_correspondance = new HashMap<String, String> ();

	public ExtractConf() {
		readFoR_CORE (ES.getConfig("FoR_CORE"));
		readCORE (ES.getConfig("CORE"));
	}

	private void readFoR_CORE (String file) {
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String line;
			while ((line = br.readLine()) != null) {
				if(line.length() > 0 && line.contains(" ")) {
					String code = line.substring(0, line.indexOf(" "));
					String FoR = line.substring(line.indexOf(" ")+1);
					FoR_correspondance.put(code, FoR);
				}
			}
			br.close();
		} catch (Exception e) {e.printStackTrace();}
	}
	private void readCORE (String file) {
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String line;
			String [] columns;
			String name;
			String shortName;
			while ((line = br.readLine()) != null) {
				name = null;
				if(line.contains("\"")) {
					columns = line.split("\"");
					name = columns[1];
					line = columns[0]+columns[2];
				}
				columns = line.split(",");
				if(name == null)
					name = columns[1];
				shortName = columns[2];
					
				Conference c = confs.get(shortName);
				if(c == null) 
					c = new Conference(shortName);
				c.name = name;
				c.CORE_RANK = columns[4];
				if(c.CORE_RANK.startsWith("National"))
					c.impact = Type.NAT;
				c.FoR = FoR_correspondance.get(columns[6]);
				
				confs.put(c.shortName, c);
			}
			br.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public Set<String> keySet (){
		return confs.keySet();
	}

	public Conference get (String j) {
		return confs.get(j);
	}

}
