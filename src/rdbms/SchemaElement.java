package rdbms;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import tools.SimFunctions;

@SuppressWarnings("serial")
public class SchemaElement implements Serializable
{
	public int elementID = 0; 
	public String name = ""; // relation_name or attribute_name
	public String type = ""; // entity, relationship, pk, fk, text, number; 

	public SchemaElement relation; // for pk, fk, text, number; 
	
	public ArrayList<SchemaElement> attributes = new ArrayList<SchemaElement>(); // for entity and relationship; 
	
	public SchemaElement pk; // for entity
	public SchemaElement defaultAttribute; 
	public ArrayList<SchemaElement> inElements = new ArrayList<SchemaElement>(); 
	
	public SchemaElement(int elementID, String name, String type)
	{
		this.elementID = elementID; 
		this.name = name; 
		this.type = type; 
	}
	
	public MappedSchemaElement isSchemaExist(String tag) throws Exception
	{
		if(this.equals(this.relation.defaultAttribute))
		{
			if(SimFunctions.ifSchemaSimilar(this.relation.name, tag) || SimFunctions.ifSchemaSimilar(name, tag))
			{
				MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this); 
				mappedSchemaElement.similarity = SimFunctions.similarity(this.relation.name, tag); 
				mappedSchemaElement.similarity = 1-(1-mappedSchemaElement.similarity)*(1-SimFunctions.similarity(name, tag)); 
				return mappedSchemaElement; 
			}			
		}
		else if(SimFunctions.ifSchemaSimilar(name, tag))
		{
			MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this); 
			mappedSchemaElement.similarity = SimFunctions.similarity(name, tag); 
			return mappedSchemaElement; 
		}
		return null; 
	}
	
	public MappedSchemaElement isTextExist(String value, Connection conn) throws Exception 
	{
		Statement statement = conn.createStatement(); 
		String numberSQL = "SELECT *" + " FROM size WHERE size.relation = '" + this.relation.name + "'"; 
//        System.out.println(numberSQL);
		ResultSet number = statement.executeQuery(numberSQL); 
		number.next(); 
		int size = number.getInt(1); 
		
		String SQL = ""; 
		if(size < 2000)
		{
			SQL = "SELECT " + this.name + " FROM " + this.relation.name; 
		}
		else if(size >= 2000 && size < 100000)
		{
			SQL = "SELECT " + this.name + " FROM " + this.relation.name + " WHERE " + this.name + " LIKE '%" +  value + "%' LIMIT 0, 2000";
		}
		else
		{
			SQL = "SELECT " + this.name + " FROM " + this.relation.name + " WHERE MATCH(" + this.name + ") AGAINST ('" +  value + "') LIMIT 0, 2000"; 
		}
//		System.out.println(SQL);
		ResultSet result = statement.executeQuery(SQL); 
		
		MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this);  
		while(result.next())
		{
			mappedSchemaElement.mappedValues.add(result.getString(1)); 
		}
		if(!mappedSchemaElement.mappedValues.isEmpty())
		{
			return mappedSchemaElement; 
		}
		
		return null;
	}
	
	public MappedSchemaElement isNumExist(String number, String operator, Connection conn) throws Exception 
	{
		Statement statement = conn.createStatement(); 
		String query = "SELECT " + this.name + " FROM " + this.relation.name + " WHERE " + this.name + operator + " " + number + " LIMIT 0, 5"; 

		ResultSet result = statement.executeQuery(query); 
		MappedSchemaElement mappedSchemaElement = new MappedSchemaElement(this);  
		while(result.next())
		{
			int mapNum = result.getInt(1); 
			String mapNumber = "" + mapNum;  
			mappedSchemaElement.mappedValues.add(mapNumber); 
		}
		if(!mappedSchemaElement.mappedValues.isEmpty())
		{
			return mappedSchemaElement; 
		}
		
		return null;
	}

	public String printForCheck() 
	{
		String result = ""; 
		result += relation.name + "." + name + "; "; 
		
		return result;
	}
}
