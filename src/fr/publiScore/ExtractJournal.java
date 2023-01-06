package fr.publiScore;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fr.dvrc.ES;
import fr.dvrc.types.Journal;
import fr.dvrc.types.Quartile;

public class ExtractJournal {
	private Map<String, Journal> journals = new HashMap<String, Journal> ();
	private Map<String, Journal> eISSN = new HashMap<String, Journal> ();
	private Map<String, String> categories = new HashMap<String, String> ();
	

	public ExtractJournal() {
		readWoSCategories (ES.getConfig("categories"));
		readWoS (ES.getConfig("WoS"));
		readSJR (ES.getConfig("SJR"));
	}
	
	private void readWoS (String file) {
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String line;
			String [] columns;
			String [] header = br.readLine().split("\",\"");
			while ((line = br.readLine()) != null) {
				columns = line.split("\",\"");
				Journal j = new Journal ();
				j.WoS = true;
				for(int i=0; i<header.length; i++) {
					String h;
					if(header[i].startsWith("\""))
						h = header[i].substring(1);
					else
						h = header[i];
					if(h.endsWith("\""))
						h = h.substring(0, h.length() - 1);
					String c;
					if(columns[i].startsWith("\""))
						c = columns[i].substring(1);
					else
						c = columns[i];
					if(c.endsWith("\""))
						c = c.substring(0, c.length() - 1);

					if(h.compareTo("Journal title") == 0)
						j.name = c;
					else if(h.compareTo("ISSN") == 0)
						j.ISSN = c;
					else if(h.compareTo("eISSN") == 0)
						j.eISSN = c;
					else if(h.compareTo("Publisher name") == 0)
						j.publisher = c;
					else if(h.compareTo("Web of Science Categories") == 0) {
						for(String s : c.split(" \\| ")) {
							if(!j.WoS_E) {
								String g = categories.get(s.toUpperCase());
								if(g != null)
									j.WoS_E = true;
							}
							j.categories.add(s);
						}
					} else if(h.compareTo("Publisher name") == 0)
						j.publisher = c;
					else if(h.compareTo("Publisher name") == 0)
						j.publisher = c;
				}
				journals.put(j.ISSN, j);
				eISSN.put(j.eISSN, j);
			}
			br.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	private void readWoSCategories (String file) {
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String line;
			while ((line = br.readLine()) != null) {
				String [] l = line.split(";");
				categories.put(l[0], l[1]);
			}
			br.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	private void readSJR (String file) {
		try {
			BufferedReader br = new BufferedReader (new FileReader (file));
			String line;
			String [] columns;
			String [] header = br.readLine().split(";");
			while ((line = br.readLine()) != null) {
				columns = line.split(";");
				if(columns[3].compareTo("journal") != 0)
					continue;
				String [] ISSNs = columns[4].split(",");
				//System.out.println(ISSNs[0]);
				String ISSN = null;
				if(ISSNs[0].length() >= 8) {
					ISSN = ISSNs[0].substring(1, 5)+"-"+ISSNs[0].substring(5, 9);
				}
				Journal j = journals.get(ISSN);
				if(j == null) {
					j = eISSN.get(ISSN);
					if(j == null) {
						j = new Journal();
						j.ISSN = ISSN;
					}
				}
				for(int i=0; i<header.length; i++) {
					String h;
					if(header[i].startsWith("\""))
						h = header[i].substring(1);
					else
						h = header[i];
					if(h.endsWith("\""))
						h = h.substring(0, h.length() - 1);
					String c;
					if(columns[i].startsWith("\""))
						c = columns[i].substring(1);
					else
						c = columns[i];
					if(c.endsWith("\""))
						c = c.substring(0, c.length() - 1);

					if(h.compareTo("Title") == 0 && j.name == null)
						j.name = c;
					else if(h.compareTo("Publisher") == 0 && j.publisher == null)
						j.publisher = c;
					else if(h.compareTo("Categories") == 0) {
						for(String s : c.split(";")) {
							String [] cat = s.split(" \\(Q");
							if(cat.length == 2) {
								Quartile q = new Quartile(cat[0], new Integer (cat[1].substring(0, 1)));
								j.SJR.put(q.category, q);
							}
						}
					} else if(h.compareTo("H index") == 0)
						try{j.h_index = new Integer(c).intValue();} catch (Exception e) {}
				}
				journals.put(j.ISSN, j);
			}
			br.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	public Set<String> keySet (){
		return journals.keySet();
	}

	public Journal get (String j) {
		return journals.get(j);
	}
}
