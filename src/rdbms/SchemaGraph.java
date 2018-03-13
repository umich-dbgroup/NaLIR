package rdbms;

import java.io.IOException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import tools.BasicFunctions;

public class SchemaGraph 
{
	public static double KeyEdge = 0.99; 
	public static double relEdge = 0.995; 
	public static double AttEdge = 0.995; 
		
	public ArrayList<SchemaElement> schemaElements = new ArrayList<SchemaElement>(); 
	public double [][] weights;  
	public double [][] shortestDistance; 
	public int [][] preElement; 
	
	public static void main(String [] args) throws IOException, ParseException
	{
		SchemaGraph graph = new SchemaGraph("dblp"); 
		graph.printForCheck(); 
	}
	
	public SchemaGraph(String databaseName) throws IOException, ParseException
	{
		JSONParser parser = new JSONParser();
		JSONArray jsonRelations = (JSONArray)parser.parse(BasicFunctions.readFile("/Users/cjbaik/dev/NaLIR/src/zfiles/" + databaseName + "Relations.json"));
		
		for(int i = 0; i < jsonRelations.size(); i++)
		{
			JSONObject jsonRelation = (JSONObject) jsonRelations.get(i); 
			SchemaElement relation = new SchemaElement(schemaElements.size(), (String)jsonRelation.get("name"), (String)jsonRelation.get("type")); 
			schemaElements.add(relation); 
			relation.relation = relation; 
			
			JSONArray jsonArray = (JSONArray) jsonRelation.get("attributes"); 
			for(int j = 0; j < jsonArray.size(); j++)
			{
				JSONObject jsonAttribute = (JSONObject) jsonArray.get(j); 
				SchemaElement attribute = new SchemaElement(schemaElements.size(), (String) jsonAttribute.get("name"), (String) jsonAttribute.get("type")); 
				attribute.relation = relation; 
				relation.attributes.add(attribute); 
				schemaElements.add(attribute); 

				if(jsonAttribute.get("importance") != null)
				{
					relation.defaultAttribute = attribute; 
				}
				if(attribute.type.equals("pk"))
				{
					relation.pk = attribute; 
				}
			}
		}

		weights = new double [schemaElements.size()][schemaElements.size()]; 
		for(int i = 0; i < weights.length; i++)
		{
			for(int j = 0; j < weights.length; j++)
			{
				weights[i][j] = 0; 
			}
		}

		ArrayList<SchemaElement> relations = this.getElementsByType("relationship entity"); 
		for(int i = 0; i < relations.size(); i++)
		{
			SchemaElement relation = relations.get(i); 
			for(int j = 0; j < relation.attributes.size(); j++)
			{
				weights[relation.elementID][relation.attributes.get(j).elementID] = AttEdge; 
			}
		}
				
		JSONArray jsonEdges = (JSONArray)parser.parse(BasicFunctions.readFile("/Users/cjbaik/dev/NaLIR/src/zfiles/" + databaseName + "Edges.json"));

		for(int i = 0; i < jsonEdges.size(); i++)
		{
			JSONObject jsonEdge = (JSONObject) jsonEdges.get(i); 
			String leftRelName = (String) jsonEdge.get("foreignRelation"); 
			String leftAttName = (String) jsonEdge.get("foreignAttribute"); 
			String rightRelName = (String) jsonEdge.get("primaryRelation"); 
			
			int fk = this.searchAttribute(leftRelName, leftAttName); 
			int pk = this.searchRelation(rightRelName); 
			
			if(this.schemaElements.get(fk).relation.type.equals("relationship"))
			{
				weights[fk][pk] = relEdge; 
			}
			else
			{
				weights[fk][pk] = KeyEdge; 
			}
			schemaElements.get(pk).inElements.add(schemaElements.get(fk)); 
		}
		
		shortestDistanceCompute(); 
	}
	
	public void shortestDistanceCompute()
	{
		shortestDistance = new double[weights.length][weights.length]; 
		preElement = new int [weights.length][weights.length]; 

		for(int i = 0; i < weights.length; i++)
		{
			for(int j = 0; j < weights.length; j++)
			{
				if(weights[i][j] > weights[j][i])
				{
					weights[j][i] = weights[i][j]; 
				}
			}
			weights[i][i] = 1; 
		}

		for(int i = 0; i < weights.length; i++)
		{
			for(int j = 0; j < weights.length; j++)
			{
				shortestDistance[i][j] = weights[i][j]; 
			}
		}
		
		for(int i = 0; i < weights.length; i++)
		{
			dijkstra(i); 
		}
	}
	
	public void dijkstra(int source)
	{
		double [] localDistance = new double [schemaElements.size()]; 
		for(int i = 0; i < localDistance.length; i++)
		{
			localDistance[i] = weights[source][i]; 
		}
		
		for(int i = 0; i < preElement.length; i++)
		{
			preElement[source][i] = source; 
		}

		boolean [] dealt = new boolean[schemaElements.size()]; 
		for(int i = 0; i < dealt.length; i++)
		{
			dealt[i] = false; 
		}
		dealt[source] = true; 
		
		boolean finished = false;
		while(finished == false)
		{
			double maxDistance = 0; 
			int maxOrder = -1; 
			for(int i = 0; i < weights.length; i++)
			{
				if(dealt[i] == false && localDistance[i] > maxDistance)
				{
					maxDistance = localDistance[i]; 
					maxOrder = i; 
				}
			}
			
			dealt[maxOrder] = true; 
			for(int i = 0; i < weights.length; i++)
			{
				if(dealt[i] == false && localDistance[maxOrder]*weights[maxOrder][i] > localDistance[i])
				{
					localDistance[i] = localDistance[maxOrder]*weights[maxOrder][i]; 
					preElement[source][i] = maxOrder; 
				}				
			}			
			
			finished = true; 
			for(int i = 0; i < dealt.length; i++)
			{
				if(dealt[i] == false)
				{
					finished = false; 
				}
			}
		}
		
		for(int i = 0; i < localDistance.length; i++)
		{
			shortestDistance[source][i] = localDistance[i]; 
		}
	}
	
	public ArrayList<Edge> getJoinPath(SchemaElement left, SchemaElement right)
	{
		ArrayList<Edge> edges = new ArrayList<Edge>(); 
		int pre = right.elementID; 
		int cur = right.elementID; 
		
		while(schemaElements.get(cur).relation.elementID != left.relation.elementID)
		{
			pre = preElement[left.elementID][cur]; 
			if(schemaElements.get(cur).relation.elementID != schemaElements.get(pre).relation.elementID)
			{
				edges.add(new Edge(schemaElements.get(cur), schemaElements.get(pre))); 
			}
			cur = pre; 
		}
		
		return edges; 
	}
	
	public double distance(SchemaElement source, SchemaElement distination)
	{
		return shortestDistance[source.elementID][distination.elementID]; 
	}
	
	public ArrayList<SchemaElement> getNeighbors(SchemaElement element, String typeList)
	{
		String [] types = typeList.split(" "); 
		ArrayList<SchemaElement> neighbors = new ArrayList<SchemaElement>(); 
		for(int i = 0; i < schemaElements.size(); i++)
		{
			if(weights[element.elementID][i] > 0)
			{
				for(int j = 0; j < types.length; j++)
				{
					if(schemaElements.get(i).type.equals(types[j]))
					{
						neighbors.add(schemaElements.get(i)); 
					}
				}
			}
		}
		for(int i = 0; i < schemaElements.size(); i++)
		{
			if(weights[i][element.elementID] > 0)
			{
				for(int j = 0; j < types.length; j++)
				{
					if(schemaElements.get(i).type.equals(types[j]))
					{
						neighbors.add(schemaElements.get(i)); 
					}
				}
			}
		}
		
		return neighbors; 
	}
	
	public ArrayList<SchemaElement> getElementsByType(String typeList)
	{
		String [] types = typeList.split(" "); 
		ArrayList<SchemaElement> relations = new ArrayList<SchemaElement>(); 
		for(int i = 0; i < schemaElements.size(); i++)
		{
			for(int j = 0; j < types.length; j++)
			{
				if((schemaElements.get(i).type.equals(types[j])))
				{
					relations.add(schemaElements.get(i)); 
				}
			}
		}
		
		return relations; 
	}
	
	public int searchRelation(String relation_name)
	{
		for(int i = 0; i < schemaElements.size(); i++)
		{
			if((schemaElements.get(i).type.equals("entity") || schemaElements.get(i).type.equals("relationship")) 
				&& schemaElements.get(i).name.equals(relation_name))
			{
				return i; 
			}
		}
		
		return -1; 
	}

	public int searchAttribute(String relation_name, String attribute_name)
	{
		for(int i = 0; i < schemaElements.size(); i++)
		{
			if((schemaElements.get(i).type.equals("entity") || schemaElements.get(i).type.equals("relationship")) 
				&& schemaElements.get(i).name.equals(relation_name))
			{
				for(int j = i+1; j < schemaElements.size(); j++)
				{
					if(schemaElements.get(j).name.equals(attribute_name))
					{
						return j; 
					}
				}
			}
		}
		
		return -1; 
	}
	
	public void printForCheck()
	{
		ArrayList<SchemaElement> entities = this.schemaElements; 
		for(int i = 0; i < entities.size(); i++)
		{
			if(entities.get(i).type.equals("entity") || entities.get(i).type.equals("relationship"))
			{
				System.out.print(i + ": " + entities.get(i).relation.name + "." + entities.get(i).name + ": "); 
				for(int j = 0; j < entities.get(i).attributes.size(); j++)
				{
					System.out.print(entities.get(i).attributes.get(j).name + " "); 
				}
				System.out.println(); 
			}
		}
		
//		System.out.println(); 
//		
//		for(int i = 0; i < shortestDistance.length; i++)
//		{
//			for(int j = 0; j < shortestDistance.length; j++)
//			{
//				System.out.print((double)Math.round(shortestDistance[i][j]*1000)/1000 + "\t"); 
//			}
//			System.out.println(); 
//		}
	}
}
