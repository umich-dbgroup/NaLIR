package dataStructure;

public class EntityPair 
{
	ParseTreeNode left; 
	ParseTreeNode right; 
	
	public EntityPair(ParseTreeNode left, ParseTreeNode right)
	{
		this.left = left; 
		this.right = right; 
	}
	
	public boolean isEntity(int node1, int node2)
	{
		if(node1 == left.nodeID && node2 == right.nodeID)
		{
			return true; 
		}
		else if(node1 == right.nodeID && node2 == left.nodeID)
		{
			return true; 
		}
		
		return false; 
	}

	public String printForCheck() 
	{
		String result = ""; 
		result += left.label + "(" + left.nodeID +") and "; 
		result += right.label + "(" + right.nodeID +") are the same entity"; 
		System.out.println(result); 
		return result;
	}
}
