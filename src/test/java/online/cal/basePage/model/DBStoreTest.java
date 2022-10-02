package online.cal.basePage.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import java.util.Arrays;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;

@DataMongoTest(properties = { "spring.data.mongodb.port=1997" })
@ActiveProfiles(profiles = "test")
@ExtendWith(SpringExtension.class)
public class DBStoreTest {

	DBStore underTest;

	final String TEST_COLLECTION = "testCollection";

	@BeforeEach
	public void setup() {
		underTest = new DBStore();
		underTest.dbName = "rock";
		underTest.mongoHost = "localhost";
		underTest.mongoPort = "1997";
		underTest.init();
		DeleteResult r = underTest.db_.getCollection(TEST_COLLECTION).deleteMany(new BasicDBObject());
		List<Document> docs = Arrays.asList(new Document("Number", "One"), new Document("Number", "two"));
		docs.get(0).put("Color", "orange");
		underTest.db_.getCollection(TEST_COLLECTION).insertMany(docs);
	}

	@Test
	public void testgetCollection() {
		MongoCollection<Document> bonkers = underTest.getCollection("bonkers");
		assertEquals(0, bonkers.countDocuments());

		MongoCollection<Document> testDocs = underTest.getCollection(TEST_COLLECTION);
		assertEquals(2, testDocs.countDocuments());
	}

	@Test
	public void testFindOne() {
		Document d = underTest.findOne(TEST_COLLECTION, new Document("Number", "One"));
		assertEquals("orange", d.get("Color"));
	}

	@Test
	public void test_insertOne() {
		Document d = new Document("Name", "Ranger");
		underTest.insertOne(TEST_COLLECTION, d);
		MongoCollection<Document> testDocs = underTest.getCollection(TEST_COLLECTION);
		assertEquals(3, testDocs.countDocuments());
		Document found = underTest.findOne(TEST_COLLECTION, d);
		assertEquals("Ranger", found.get("Name"));
	}

	@Test
	public void test_update() {
		Document d = new Document("Name", "Crab");
		Document query = new Document("Number", "two");

		underTest.update(TEST_COLLECTION, query, d);
		MongoCollection<Document> testDocs = underTest.getCollection(TEST_COLLECTION);
		assertEquals(2, testDocs.countDocuments());
		Document found = underTest.findOne(TEST_COLLECTION, query);
		assertEquals("Crab", found.get("Name"));
		assertEquals("two", found.get("Number"));

	}

}
