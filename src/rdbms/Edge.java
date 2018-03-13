package rdbms;

public class Edge 
{
	public SchemaElement left; 
	public SchemaElement right; 
	
	public Edge(SchemaElement left, SchemaElement right)
	{
		this.left = left; 
		this.right = right; 
	}
	
	public String edgeToString()
	{
		String result = ""; 
		if(left.type.equals("fk"))
		{
			result += left.relation.name + "." + left.name; 
		}
		else
		{
			result += left.relation.name + "." + left.relation.pk.name; 
		}
		
		result += " = "; 
		
		if(right.type.equals("fk"))
		{
			result += right.relation.name + "." + right.name; 
		}
		else
		{
			result += right.relation.name + "." + right.relation.pk.name; 
		}
		return result; 
	}	
}
