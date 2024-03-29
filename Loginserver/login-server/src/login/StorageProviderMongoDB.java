package login;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * Storage provider for a MongoDB.
 */
class StorageProviderMongoDB {

	private static final String MONGO_URL = "mongodb://141.19.142.59:27017";
	/** URI to the MongoDB instance. */
	private static MongoClientURI connectionString = new MongoClientURI(MONGO_URL);

	/** Client to be used. */
	private static MongoClient mongoClient = new MongoClient(connectionString);

	/** Mongo database. */
	private static MongoDatabase database = mongoClient.getDatabase("userbase");

	/**
	 * @see var.chat.server.persistence.StorageProvider#retrieveMessages(java.lang.String,
	 *      long, boolean)
	 */
	public synchronized User retrieveUser(String username) {

		MongoCollection<Document> collectionAccount = database.getCollection("account");
		// Retreive the hashed password
		Document doc = collectionAccount.find(eq("user", username)).first();
		if (doc == null) {
			System.out.println("-->null");
			return null;
		}
		System.out.println(doc.getString("storedPassword")+" "+doc.getString("pseudonym")+" 11111");
		System.out.println(doc.getString("user"));
		User user = new User(username, doc.getString("storedPassword"), doc.getString("pseudonym"), true);
		return user;
	}

	/**
	 *
	 */
	public synchronized void saveToken(String token, String expirationDate, String pseudonym) {

		MongoCollection<Document> collectionToken = database.getCollection("token");

		// add user to database
		Document doc = new Document("token", ""+token+"").append("expire-date", expirationDate).append("pseudonym",
				pseudonym);

		if (collectionToken.find(eq("pseudonym", pseudonym)).first() != null) {
			collectionToken.updateOne(eq("pseudonym", pseudonym), new Document("$set", doc));
		} else {
			collectionToken.insertOne(doc);
		}
	}

	public synchronized String retrieveToken(String pseudonym, String token) {
		MongoCollection<Document> collection = database.getCollection("token");

		// Retreive the tokeninformation
		Document doc = collection.find(and(eq("pseudonym", pseudonym), eq("token", token))).first();
		if (doc == null) {
			return null;
		}
		return doc.getString("expire-date");
	}

	/**
	 *
	 */
	public synchronized void deleteToken(String token) {
		MongoCollection<Document> collection = database.getCollection("token");
		collection.deleteOne(eq("token", token));
	}

	/**
	 * @see var.chat.server.persistence.StorageProvider#clearForTest()
	 */
	public void clearForTest() {

	}
}