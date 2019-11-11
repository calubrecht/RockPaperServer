package online.cal.basePage.model;

import javax.annotation.*;

import org.bson.*;
import org.bson.conversions.*;
import org.springframework.stereotype.*;

import com.mongodb.*;
import com.mongodb.client.*;

@Component
public class DBStore
{
	static DBStore instance__;
	MongoClient client_;
	MongoDatabase db_;
	
	public DBStore()
	{
		instance__ = this;
	}
	
	public static DBStore getInstance()
	{
		return instance__;
	}
	
	@PostConstruct
	public void init()
	{
		client_ = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
		db_ = client_.getDatabase("rockP");
	}
	
	public MongoCollection<Document> getCollection(String s)
	{
		return db_.getCollection(s);
	}
	
	public Document findOne(String collection, Bson query)
	{
		MongoCollection<Document> col = getCollection(collection);

		FindIterable<Document> itr = col.find( query);
	    return itr.first();
	}
	
	public void insertOne(String collection, Document d)
	{
		MongoCollection<Document> col = getCollection(collection);
		col.insertOne(d);
	}
	
	public void update(String collection, Document query, Document data)
	{
		db_.getCollection(collection).updateOne(query, new Document().append("$set", data));
	}
	
}
