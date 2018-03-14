package dataStructure;

import rdbms.SchemaElement;

public class SQLElement 
{
	public Block block; 
	public ParseTreeNode node; 

	public SQLElement(Block block, ParseTreeNode node)
	{
		this.block = block; 
		this.node = node; 
	}
	
	public String toString(Block block, String attribute)
	{
		String result = "";

		if (node.mappedElements.isEmpty()) return result;

		if(block.equals(this.block))
		{
			SchemaElement element = node.mappedElements.get(node.choice).schemaElement; 
			result += element.relation.name + "." + element.name; 
		}
		else if(this.block.outerBlock.equals(block))
		{
			if(attribute.isEmpty())
			{
				result += "block_" + this.block.blockID + "." + node.parent.function; 
			}
			else
			{
				result += "block_" + this.block.blockID + "." + attribute; 
			}
		}
		else
		{
			if(attribute.isEmpty())
			{
				result += "block_" + this.block.outerBlock.blockID + "." + node.parent.function; 
			}
			else
			{
				result += "block_" + this.block.blockID + "." + attribute; 
			}
		}
		
		return result; 
	}
}