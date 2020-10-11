package online.cal.basePage.model;

import java.security.*;
import java.time.*;
import java.util.*;

import org.bson.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

import com.mongodb.client.*;

@Component
public class JwtByteSource
{
    @Autowired
    DBStore dbStore;
	
	public byte[] getBytes()
	{
		// If Server is started more than 2 days since a token secret was generated, generate
		// new random bits. Otherwise just use the bits in the database.
		MongoCollection<Document> collection = dbStore.getCollection("JWTSecret");
		Document query = new Document("Purpose", "JWTSecret");
		
		Document d = dbStore.findOne("JWTSecret", query);
		Date created = d == null ? null : d.getDate("created");
		byte secretBytes[];
		if (created == null || created.toInstant().plus(Period.ofDays(2)).isBefore(new Date().toInstant()))
		{
			collection.deleteMany(query);
		    SecureRandom random = new SecureRandom();
		    secretBytes = new byte[21];
		    random.nextBytes(secretBytes);
		    String b64 = Base64.getEncoder().encodeToString(secretBytes);
		    Document dbO = query.append("created", new Date()).append("jwtToken", b64);
		    collection.insertOne(dbO);
		}
		else
		{
			String b64 = d.getString("jwtToken");
			secretBytes = Base64.getDecoder().decode(b64);
		}
		return secretBytes;
	}
}
