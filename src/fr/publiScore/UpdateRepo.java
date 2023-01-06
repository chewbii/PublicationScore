package fr.publiScore;

import fr.dvrc.ES;

public class UpdateRepo {
	private ExtractJournal journals;
	private ExtractConf confs;

	public UpdateRepo () {
		journals = new ExtractJournal ();
		confs = new ExtractConf ();

		ES.config();
		try {
			
			ES.open();
			int i=0;
			for(String k : journals.keySet())
				ES.upload2Elastic(journals.get(k).toJSON(), k, true, ++i, ES.getConfig("ES_index_ACL"));
			for(String k : confs.keySet())
				ES.upload2Elastic(confs.get(k).toJSON(), k, true, ++i, ES.getConfig("ES_index_ACL"));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String [] args) {
		new UpdateRepo ();
	}
}
