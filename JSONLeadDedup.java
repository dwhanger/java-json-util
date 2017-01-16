
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonReader;
import javax.json.JsonValue;


public class JSONLeadDedup {

	public static final String JSON_FILE="leads.json";
	public static final String CHANGELOG_FILE="change.log";
	

	public static JsonObject readInJSONFile(String filename)
	{
		JsonObject jsonObject=null;

		try
		{
			InputStream fis = new FileInputStream(filename);
			
			//
			// Create JsonReader object
			//
			JsonReader jsonReader = Json.createReader(fis);
			
			
			//
			// Get JsonObject from JsonReader
			//
			jsonObject = jsonReader.readObject();
			
			//
			// Close IO resource and JsonReader now
			//
			jsonReader.close();
			fis.close();

			return jsonObject;

		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonObject;
	}


	public static HashMap<String, Object> findDupsAndEliminate( JsonObject jsonObject, String idKeyStr, String emailKeyStr, String changeLogFilename)
	{
		HashMap<String, Object> hm = new HashMap();
		HashMap<String, Object> hmEmail = new HashMap();
		FileOutputStream 		fos = null;

		try
		{					
			//
			// Open up our changelog file...
			// 
			fos  = new FileOutputStream(changeLogFilename);

			//
			// Look for the root 'leads' node...
			//
			JsonArray jsonRec = jsonObject.getJsonArray("leads");
			for (JsonObject jObj : jsonRec.getValuesAs(JsonObject.class)) 
			{
				//
				// Check and see if we have seen this id or email previously...
				//
				if ( (! hm.containsKey( jObj.getString(idKeyStr))) && (! hmEmail.containsKey( jObj.getString(emailKeyStr))) )
				{
					//
					// found a unique record, put it in the hash...
					//
					hm.put( jObj.getString("_id"), jObj);
					
					//
					// Email hashmap...
					//
					hmEmail.put( jObj.getString("email"), jObj);
					
					//System.out.println("-----------putting id=="+jObj.getString("_id")+" into hashmap------");
				}
				else
				{
					//
					// We have seen one of the items either id or email...one more check...only allow id records with a date
					// greater than what we have previously found through here...invoking special updating logic...
					//
					if ( hm.containsKey( jObj.getString(idKeyStr)) )
					{
						//
						// Dup key found...but does the hash have the most current...?
						//
						JsonObject jsObjInHash = (JsonObject)hm.get( jObj.getString(idKeyStr));

						if( jsObjInHash.getString("entryDate").compareTo(jObj.getString("entryDate")) < 0 )
						{
							//
							// remove the older record....
							//
							hm.remove( jObj.getString(idKeyStr) );

							//
							// add in the new record
							//
							hm.put( jObj.getString("_id"), jObj);

							//
							// found a more current record...log it...
							//
							String outputStr = "found more current::_id=="+jObj.getString("_id")+", email=="+jObj.getString("email")+
												", firstName=="+jObj.getString("firstName")+", lastName=="+jObj.getString("lastName")+
												", address=="+jObj.getString("address")+", entryDate=="+jObj.getString("entryDate");
							fos.write(outputStr.getBytes());
							fos.write(13);
							fos.write(10);

							//System.out.println("-----found a more current record id=="+jObj.getString("_id")+" updating------");
						}
					}
					else
					{
						//
						// Got a dup...log it...
						//
						String outputStr = "duplicate::_id=="+jObj.getString("_id")+", email=="+jObj.getString("email")+
											", firstName=="+jObj.getString("firstName")+", lastName=="+jObj.getString("lastName")+
											", address=="+jObj.getString("address")+", entryDate=="+jObj.getString("entryDate");
						fos.write(outputStr.getBytes());
						fos.write(13);
						fos.write(10);

						//System.out.println("-----found a dup id=="+jObj.getString("_id")+" logging------");
					}
				}

			}

			//
			// Close our file....
			//
			fos.flush();
			fos.close();

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( fos != null )
				{
					fos.close();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}

		return hm;
	}

	public static void main(String[] args) 
	{
		JsonObject jsonObject = readInJSONFile( JSON_FILE);

		HashMap jsonHash = findDupsAndEliminate( jsonObject, "_id", "email", CHANGELOG_FILE);

		//
		// Get a set of the entries and get our iterator...
		//
		Set set = jsonHash.entrySet();

		Iterator i = set.iterator();

		//
		// Output to stdout our de-duped json...
		// 
		System.out.println("{\"leads\":[");
		while( i.hasNext() ) 
		{
			Map.Entry me = (Map.Entry)i.next();
			System.out.println( (JsonObject)me.getValue() );
		}
		System.out.println("]");
		System.out.println("}");

	}
}