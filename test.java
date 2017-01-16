import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class test {

	public static final String JSON_FILE="leads.json";

	public static void main(String[] args) throws IOException 
	{
		InputStream fis = new FileInputStream(JSON_FILE);



		System.out.println("-----got here------");

		fis.close();
	}

}