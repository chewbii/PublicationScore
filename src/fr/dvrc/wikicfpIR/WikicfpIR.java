package fr.dvrc.wikicfpIR;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import fr.dvrc.ES;
import fr.dvrc.types.WikiCfp;
import fr.publiScore.ExtractConf;

public class WikicfpIR {
    public final static Logger log = Logger.getLogger("WikicfpIR");
    public static int i = 0;
	List<String> wikiCfpFiles = new ArrayList<String> ();
	private Map<String, WikiCfp> wikiCfp = new HashMap<String, WikiCfp> ();
	private ExtractConf confs;
	SimpleDateFormat dateFormatLong =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	SimpleDateFormat dateFormatShort =  new SimpleDateFormat("yyyy-MM-dd");
	//new SimpleDateFormat("MMM d, yyyy");

	public WikicfpIR() {
		ES.config();
		try {
			ES.open();

			confs = new ExtractConf ();
			String folder = ES.getConfig("wikiCfp");
			readWikiCfpFolder(folder);
			for(String file : wikiCfpFiles)
				readWikiCfp(folder+file);

//			int i=0;
//			for(String k : wikiCfp.keySet())
//				ES.upload2Elastic(wikiCfp.get(k).toJSON(), k, true, ++i, ES.getConfig("ES_index_WikiCfp"));
			ES.close ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void readWikiCfpFolder (String folder) {
		try {
			File f = new File(folder);
			File[] listOfFiles = f.listFiles();

			for (File file : listOfFiles) {
			    if (file.isFile()) {
			    	wikiCfpFiles.add(file.getName());
			    }
			}
		} catch (Exception e) {
			
		}
	}

	private String readValue (String v) {
		if(v.length() == 0 || v.indexOf("'") == -1)
			return null;
		try{
			v = v.substring(0, v.lastIndexOf("'"));
			v = v.substring(v.indexOf("'")+1);
			return v;
		} catch (Exception e) {
			log.error(v + e.getStackTrace());
			System.exit(0);
			return "";
		}
		
	}

	private String[] readValues (String v) {
		if(v.length() == 0 || v .indexOf("'") < 0)
			return null;
		v = v.substring(0, v.lastIndexOf("'"));
		v = v.substring(v.indexOf("'")+1);
		return v.split("' b'");
	}

	private Date readDate(String v, SimpleDateFormat dateFormat) {
		v = readValue(v);
		if(v == null || v.compareTo("TBD") == 0)
			return null;
		if(dateFormat == dateFormatShort) {
			String d = v.substring(v.indexOf(", ")+2)+"-";
			d += month(v.substring(0, 3)) + "-";
			v = v.substring(v.indexOf(" ")+1);
			v = d + v.substring(0, v.indexOf(","));
		}
		try{
			return dateFormat.parse(v);
		} catch (Exception e) {
			log.error(v + e.getStackTrace());
			return null;
		}
	}

	private String month (String m) {
		switch(m) {
		case "Jan":return "01";
		case "Feb":return "02";
		case "Mar":return "03";
		case "Apr":return "04";
		case "May":return "05";
		case "Jun":return "06";
		case "Jul":return "07";
		case "Aug":return "08";
		case "Sep":return "09";
		case "Oct":return "10";
		case "Nov":return "11";
		case "Dec":return "12";
		default : return "01";
		}
	}
	
	private void readWikiCfp(String file) {
		String line = null;
		try {
			log.debug(file);
			BufferedReader br = new BufferedReader (new FileReader (file));
			String [] columns;
			String [] header = br.readLine().split(",");

			while ((line = br.readLine()) != null) {
				while (line.indexOf(",,") > 0)
					line = line.replaceAll(",,", ",\"b,");
				while (line.indexOf(",\"\"") > 0)
					line = line.replaceAll(",\"\"", ",\"");
				line = line.replaceAll(",b", ",\"b");
				columns = line.split(",\"b");
				WikiCfp cfp = null;
				for(int i=0; i<header.length; i++) {
					String h = header[i];

					if(test(h, "identifier", columns, i))		cfp = new WikiCfp (readValue(columns[i]));
					else if(test(h, "title", columns, i)) {
						cfp.title = readValue(columns[i]);
						if(cfp.title.indexOf(" ") > 0) {
							cfp.title = cfp.title.substring(0, cfp.title.lastIndexOf(" "));
							cfp.CORE();
						}
					}
					else if(test(h, "description", columns, i))	cfp.description = readValue(columns[i]);
					else if(test(h, "source", columns, i))		cfp.url = readValue(columns[i]);
					else if(test(h, "startDate", columns, i))	cfp.startDate = readDate(columns[i], dateFormatLong);
					else if(test(h, "endDate", columns, i))		cfp.endDate = readDate(columns[i], dateFormatLong);
					else if(test(h, "locality", columns, i))	cfp.locality = readValue(columns[i]);
					else if(test(h, "Submission Deadline", columns, i))	cfp.deadline = readDate(columns[i], dateFormatShort);
					else if(test(h, "Categories", columns, i))	cfp.categories = readValues(columns[i]);
					else if(test(h, "Notification Due", columns, i))	cfp.notification = readDate(columns[i], dateFormatShort);
					else if(test(h, "Final Version Due", columns, i))	cfp.finalVersion = readDate(columns[i], dateFormatShort);
					else if(test(h, "Call For Papers", columns, i))		cfp.CfP = readValue(columns[i]);
					else if(test(h, "Related Resources", columns, i))	cfp.resources= readValue(columns[i]);
					else if(columns.length > i && columns [i] != null)
						log.debug(h+" missed: "+columns[i]);
				}
				if(cfp != null) {
					//wikiCfp.put(cfp.id, cfp);
					ES.upload2Elastic(cfp.toJSON(), cfp.id, true, ++i, ES.getConfig("ES_index_WikiCfp"));
					log.debug(cfp.toJSON());					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.toString() + " " +line);
			System.exit(0);
		}
	}

	public boolean test (String header, String name, String[] columns, int i) {
		if (header.compareTo(name) == 0 && columns.length > i) {
			if (columns[i] == null) {
				System.out.println(header+" is null");
			} else
				return true;
		}
		return false;
	}
	
	public static void main (String [] args) {
		DOMConfigurator.configure("log4j.xml");  
        new WikicfpIR ();
	}
}
