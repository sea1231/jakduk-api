package com.jakduk.dao;

import org.jongo.Jongo;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
* @author <a href="mailto:phjang1983@daum.net">Jang,Pyohwan</a>
* @company  : http://jakduk.com
* @date     : 2015. 12. 16.
* @desc     :
*/
public class JongoR {
	private Jongo jongo = null;

	public JongoR(MongoTemplate mongoTemplate) {
		jongo = new Jongo(mongoTemplate.getDb());
	}

	public Jongo getJongo() {
		return jongo;
	}

	public void setJongo(Jongo jongo) {
		this.jongo = jongo;
	}
	
}