package fr.publiScore;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.dvrc.ES;
import fr.dvrc.types.Book;
import fr.dvrc.types.Conference;
import fr.dvrc.types.Journal;
import fr.dvrc.types.Type;

public class ExtractResearcher {
	public ExtractResearcher(String name, boolean isUrl) throws Exception {
		try{
			ES.config();
			ES.open();
			searchResearcher(name, isUrl);
		} finally {
			ES.close();
		}
	}

	public void searchResearcher (String name, boolean isUrl) throws Exception {
		if(!isUrl) {
			String url_name = StringUtils.stripAccents(name).replaceAll(" ", "+");
			/*try {
	            url_name = URLEncoder.encode(url_name, StandardCharsets.UTF_8.toString());
	        } catch (UnsupportedEncodingException ex) {
	            throw new RuntimeException(ex.getCause());
	        }*/
			System.out.println("https://dblp.org/search/author/api?format=xml&q="+url_name);
			Document d = readXML ("https://dblp.org/search/author/api?format=xml&q="+url_name);
			Node n = d.getElementsByTagName("hit").item(0);
			NodeList nl = n.getChildNodes();
			JSONObject author = new JSONObject ();
			author.put("author", name);
			for(int i=0; i<nl.getLength(); i++) {
				if(nl.item(i).getNodeName() == null)
					;
				else if(nl.item(i).getNodeName().compareTo("info") == 0) {
					NodeList info = nl.item(i).getChildNodes();
					for(int j=0; j<info.getLength(); j++) {
						if(info.item(j).getNodeName() == null)
							;
						else if(info.item(j).getNodeName().compareTo("author") == 0)
							System.out.println(info.item(j).getTextContent());
						else if(info.item(j).getNodeName().compareTo("url") == 0) {
							System.out.println(info.item(j).getTextContent());
							searchPublications(info.item(j).getTextContent(), author);
						}
					}
				}
			}
		} else {
			searchPublications (name, new JSONObject());
		}
	}

	public void searchPublications (String url, JSONObject author) throws Exception {
		Document d = readXML (url+".xml");
		NodeList nl = d.getElementsByTagName("r");
		Node n;
		JSONObject o = null, copy = null;

		for(int i=0;i<nl.getLength();i++) {
			n = nl.item(i).getChildNodes().item(0);
			url = null;
			int pages = 0;
			String title = null;
			int year = 0;
			String school = null;
			String publisher = null;
			String booktitle = null;
			NodeList art_nl = n.getChildNodes();
			for(int j=0;j<art_nl.getLength();j++) {
				if(compare(art_nl, j, "year"))
					year = new Integer (getValue(art_nl, j));
				else if(compare(art_nl, j, "title"))
					title = getValue(art_nl, j);
				else if(compare(art_nl, j, "school"))
					school = getValue(art_nl, j);
				else if(compare(art_nl, j, "publisher"))
					publisher = getValue(art_nl, j);
				else if(compare(art_nl, j, "booktitle"))
					booktitle = getValue(art_nl, j);
				else if(compare(art_nl, j, "pages")) {
					String p = getValue(art_nl, j);
					if(p != null && p.contains("-")) {
						String [] ps = p.split("-");
						try{
							pages = new Integer(ps[1]) - new Integer(ps[0])+1;
						} catch (Exception e) {
							System.out.println(p);
						}
					}
				}
				else if(compare(art_nl, j, "url")) {
					url = getValue(art_nl, j);
				}
			}
			if(n.getNodeName().compareTo("article") == 0) {
				if(url != null) {
					o = getJournal(url);
					o.put("year", year);
					if(pages > 0) o.put("pages", pages);
				}
			} else if(n.getNodeName().compareTo("inproceedings") == 0) {
				if(url != null) {
					o = getConference(url);
					o.put("year", year);
					if(pages > 0) o.put("pages", pages);
					if(o.get("CORE") == null)
						o.put("CORE", "unranked");
				}				
			} else if(n.getNodeName().compareTo("book") == 0) {
				Book m = new Book ();
				if(publisher != null) {
					m.type= Book.BOOK;
					m.publisher = publisher;					
				} else if (school != null) {
					m.type= Book.HDR;
					m.university = school;
				}
				m.title = title;
				m.year = year;
				o = m.toJSON();
			} else if(n.getNodeName().compareTo("incollection") == 0) {
				Book m = new Book ();
				m.type= Book.BOOK_CHAPTER;
				m.publisher = publisher;					
				m.title = title;
				m.year = year;
				m.name = booktitle;
				o = m.toJSON();
			} else if(n.getNodeName().compareTo("proceedings") == 0) {
				Book m = new Book ();
				m.type= Book.PROCEEDINGS;
				m.publisher = publisher;					
				m.title = title;
				m.year = year;
				m.name = booktitle;
				o = m.toJSON();
			} else if(n.getNodeName().compareTo("phdthesis") == 0) {
				Book m = new Book ();
				m.type= Book.PHD;
				m.title = title;
				m.year = year;
				m.university = school;
				o = m.toJSON();
			}
			if(o != null) {
				copy = (JSONObject)author.clone();
				copy.put("publication", o);
				if(title !=null)
					o.put("title", title);
				ES.upload2Elastic(copy, (String)copy.get("author")+"_"+o.get("title"), true, i, ES.getConfig("ES_index_authors"));
			}
			o = null;
		}
	}

	private boolean compare (NodeList nl, int i, String nodeName) {
		if(nl.item(i) == null)
			return false;
		return (nl.item(i).getNodeName().compareTo(nodeName) == 0);
	}

	private String getValue (NodeList nl, int i) {
		return nl.item(i).getTextContent();
	}

	public JSONObject getJournal (String url) throws ParserConfigurationException, SAXException, IOException, ParseException {
		url = url.substring(0, url.indexOf(".html"));
		Document d = readXML ("https://dblp.org/"+url+".xml");
		String name = d.getElementsByTagName("ref").item(0).getTextContent();
		return getACL(name, "journal");
	}
	public JSONObject getConference (String url) throws ParserConfigurationException, SAXException, IOException, ParseException {
		url = url.substring(0, url.indexOf(".html"));
		Document d = readXML ("https://dblp.org/"+url+".xml");
		String name = d.getElementsByTagName("ref").item(0).getTextContent();
		return getACL(name, "conference");
	}
	
	public Document readXML (String xml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();  
		//an instance of builder to parse the specified xml file  
		DocumentBuilder db = dbf.newDocumentBuilder();  
		Document doc = db.parse(xml);
		doc.getDocumentElement().normalize();
		return doc;
	}

	protected JSONObject getACL (String name, String type) throws IOException, ParseException {
		String field;
		if(type.compareTo("journal") == 0) field = "name";
		else field = "shortName";
		try{
			JSONObject o = ES.query(query(field, name, type));
			return (JSONObject)((JSONObject)((JSONArray)((JSONObject)o.get("hits")).get("hits")).get(0)).get("_source");
		} catch (Exception e) {
			System.out.println(name+" not found");
			Type t;
			if(type.compareTo("journal") == 0)
				t = new Journal();
			else
				t = new Conference(name);
			t.name = name;
			return t.toJSON();
		}
	}

	protected String query (String field, String name, String type) {
		StringBuffer sb = new StringBuffer ("{\"query\": {\"bool\": {\"must\": [\n\t\t");
		sb.append("{\"match_phrase\":{\""+field+"\": \""+name+"\"}},");
		sb.append("{\"match_phrase\":{\"type\": \""+type+"\"}}");
		sb.append("]}}}");
		return sb.toString();
	}

	
	public static void main (String [] args) {
		try {
			String name="";
			for(String s : args)
				name += s+" ";
			if(name.length() == 0)
				System.out.println("need a name!");
			new ExtractResearcher (name, name.startsWith("http"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
