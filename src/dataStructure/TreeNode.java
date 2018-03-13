package dataStructure;

import java.io.Serializable;
import java.util.ArrayList;

import rdbms.SchemaElement;
import rdbms.SchemaGraph;

@SuppressWarnings("serial")
public class TreeNode implements Serializable, Comparable<TreeNode>
{
	public int nodeID; 
	public String label; // word itself; 
	public String tokenType = "NA"; // CMT, NT, VT and so forth; 
	public String function = "NA"; // only exist in OT and FT; 
	public TreeNode parent; 
	public ArrayList<TreeNode> children = new ArrayList<TreeNode>(); 
	
	public SchemaElement mappedElement; 
	
	public boolean hasQT = false; 
	public boolean upValid = true; 
	public ArrayList<Boolean> haveChildren = new ArrayList<Boolean>(); 
	public double weight = 0.98; 

	public TreeNode()
	{
		
	}
	
	public TreeNode(ParseTreeNode parseTreeNode)
	{
		this.nodeID = parseTreeNode.nodeID; 
		this.label = parseTreeNode.label; 
		this.tokenType = parseTreeNode.tokenType; 
		this.function = parseTreeNode.function; 
		
		if(!parseTreeNode.QT.isEmpty())
		{
			hasQT = true; 
		}
		
		if(parseTreeNode.choice >= 0 && parseTreeNode.mappedElements.size() > parseTreeNode.choice-1 && !parseTreeNode.mappedElements.isEmpty())
		{
			this.mappedElement = parseTreeNode.mappedElements.get(parseTreeNode.choice).schemaElement; 
		}
		else 
		{
			this.mappedElement = null; 
		}
	}
	
	public void NodeEvaluate(SchemaGraph schemaGraph, Query query)
	{
		NodeValidTest(); 
		NodeWeightCompute(schemaGraph, query); 
	}
	
	public void NodeValidTest()
	{
		if(tokenType.equals("ROOT"))
		{
			if(parent != null)
			{
				upValid = false; 
			}
			for(int i = 0; i < children.size(); i++)
			{
				if(!children.get(i).tokenType.equals("CMT") && !children.get(i).tokenType.equals("OT"))
				{
					children.get(i).upValid = false; 
				}
			}
		}
		if(tokenType.equals("CMT"))
		{
			int NTVTFTNum = 0; 
			for(int i = 0; i < children.size(); i++)
			{
				TreeNode child = children.get(i); 
				if((child.tokenType.equals("FT") && !child.function.equals("min") && !child.function.equals("max")) || child.mappedElement != null)
				{
					NTVTFTNum++; 
				}
				else
				{
					child.upValid = false; 
				}
			}
			
			if(NTVTFTNum == 0)
			{
				haveChildren.add(false); 
			}
			else if(NTVTFTNum > 1)
			{
				for(int i = 0; i < children.size(); i++)
				{
					children.get(i).upValid = false; 
				}
			}
		}
		else if(tokenType.equals("FT"))
		{
			if(function.equals("max") || function.equals("min"))
			{
				if(!parent.tokenType.equals("OT") || !parent.function.equals("="))
				{
					upValid = false; 
				}
				for(int i = 0; i < children.size(); i++)
				{
					children.get(i).upValid = false; 
				}
			}
			else
			{
				if(function.equals("sum") || function.equals("avg"))
				{
					if(!parent.tokenType.equals("OT") && !parent.tokenType.equals("CMT"))
					{
						upValid = false; 
					}
				}
				else
				{
					if(parent.tokenType.equals("OT") || parent.tokenType.equals("CMT") || parent.function.equals("sum") || parent.function.equals("avg"))
					{
						upValid = true; 
					}
					else
					{
						upValid = false; 
					}
				}
				
				if(children.size() == 0)
				{
					haveChildren.add(false); 
				}
				else if(children.size() == 1)
				{
					TreeNode child = children.get(0); 
					if(child.mappedElement != null || child.function.equals("count"))
					{
					}
					else
					{
						child.upValid = false; 
					}
				}
				else
				{
					for(int i = 0; i < children.size(); i++)
					{
						children.get(i).upValid = false; 
					}
				}
			}
		}
		else if(tokenType.equals("NT"))
		{
//			if(children.size() == 0)
//			{
//				if(!parent.tokenType.equals("CMT") && !parent.tokenType.equals("OT") && ! this.hasQT == true)
//				{
//					haveChildren.add(false); 
//				}
//			}
//			else
//			{
				for(int i = 0; i < children.size(); i++)
				{
					TreeNode child = children.get(i); 
					if(!child.tokenType.equals("OT") && child.mappedElement == null)
					{
						child.upValid = false; 
					}
				}
//			}
		}
		else if(tokenType.equals("VTTEXT") || tokenType.equals("VTNUM"))
		{
			for(int i = 0; i < children.size(); i++)
			{
				TreeNode child = children.get(i); 
				child.upValid = false; 
			}
			if(!parent.tokenType.equals("NT") && !parent.tokenType.equals("OT"))
			{
				upValid = false; 
				haveChildren.add(false); 
				haveChildren.add(false); 
			}
		}
		else if(tokenType.equals("OT"))
		{
			if(children.size() > 2)
			{
				for(int i = 0; i < children.size(); i++)
				{
					children.get(i).upValid = false; 
				}
			}
			else if(children.size() == 0)
			{
				haveChildren.add(false); 
				haveChildren.add(false); 
				haveChildren.add(false); 
			}
			else if(children.size() == 1)
			{
				TreeNode child = children.get(0); 
				if(child.tokenType.equals("VTNUM") && child.mappedElement != null)
				{
					if(!parent.tokenType.equals("NT"))
					{
						upValid = false; 
					}
				}
				else if(child.tokenType.equals("VTNUM") || child.tokenType.equals("NT") || child.tokenType.equals("VTTEXT") || child.tokenType.equals("FT"))
				{
					if(!parent.tokenType.equals("ROOT"))
					{
						upValid = false; 
					}
					haveChildren.add(false); 
				}
				else
				{
					if(!parent.tokenType.equals("ROOT") && !parent.tokenType.equals("NT"))
					{
						upValid = false; 
					}
					haveChildren.add(false); 
					haveChildren.add(false); 
					haveChildren.add(false); 
				}
			}
			else if(children.size() == 2)
			{
				int leftRight = 0; 
				int right = 0; 
				
				for(int i = 0; i < children.size(); i++)
				{
					TreeNode child = children.get(i); 
					if(child.tokenType.equals("VTNUM") || child.tokenType.equals("VTTEXT") || child.function.equals("max") || child.function.equals("min"))
					{
						right++; 
					}
					else if(child.tokenType.equals("NT") || child.tokenType.equals("FT"))
					{
						leftRight++; 
					}
					else
					{
						child.upValid = false; 
					}
				}
				
				if((leftRight+right) == 0)
				{
					if(!parent.tokenType.equals("ROOT") && !parent.tokenType.equals("NT"))
					{
						upValid = false; 
					}
					haveChildren.add(false); 
					haveChildren.add(false); 
					haveChildren.add(false); 
				}
				else if((leftRight+right) == 1)
				{
					if(!parent.tokenType.equals("ROOT"))
					{
						upValid = false; 
					}
					haveChildren.add(false); 
				}
				else
				{
					if(right == 2)
					{
						haveChildren.add(false); 
					}
					if(!parent.tokenType.equals("ROOT"))
					{
						upValid = false; 
					}
				}
			}
		}
	}
	
	public void NodeWeightCompute(SchemaGraph schemaGraph, Query query)
	{
		weight = 0.98; 
		if(mappedElement == null)
		{
			return; 
		}
		else if(parent != null && parent.tokenType.equals("OT"))
		{
			if(parent.children.size() == 1 && this.tokenType.equals("VTNUM"))
			{
				if(parent.parent != null && parent.parent.mappedElement != null)
				{
					weight = schemaGraph.distance(mappedElement, parent.parent.mappedElement); 
					return; 
				}
			}
			return; 
		}
		else if(parent == null || parent.mappedElement == null)
		{
			return; 
		}
		
		if(!mappedElement.equals(parent.mappedElement))
		{
			weight = schemaGraph.distance(mappedElement, parent.mappedElement); 
		}
		else
		{
			for(int i = 0; i < query.entities.size(); i++)
			{
				if(query.entities.get(i).isEntity(parent.nodeID, nodeID))
				{
					return; 
				}
			}
			weight = 0.95; 
		}
	}
	
	public int compareTo(TreeNode node) 
	{
		if(this.nodeID > node.nodeID)
		{
			return 1; 
		}
		else if(node.nodeID > this.nodeID)
		{
			return -1; 
		}
		return 0;
	}

	public String printForCheck()
	{
		String result = "";
		result += nodeID + ". "; 
		result += label + ": "; 
		result += tokenType + "; "; 
		result += "valid: "; 
		result += upValid + "! "; 
		for(int i = 0; i < haveChildren.size(); i++)
		{
			result += haveChildren.get(i) + " "; 
		}
		result += "| "; 
		if(mappedElement != null)
		{
			result += mappedElement.printForCheck() + "; "; 
		}
		result += "; weight: " + (double)Math.round(weight*100)/100; 
		System.out.println(result); 
		return result + "\n"; 
	}

}
