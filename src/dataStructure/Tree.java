package dataStructure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.Serializable;

import rdbms.SchemaGraph;

@SuppressWarnings("serial")
public class Tree implements Serializable, Comparable<Tree>
{
	public TreeNode root; 
	public ArrayList<TreeNode> allNodes = new ArrayList<TreeNode>(); 
	public int HashNum = 1; 
	
	public int cost = 0; 
	public double weight = 1; 
	public int invalid = 0; 
	
	public Tree(ParseTree parseTree)
	{
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			allNodes.add(new TreeNode(parseTree.allNodes.get(i))); 
		}
		root = allNodes.get(0); 
		
		for(int i = 0; i < allNodes.size(); i++)
		{
			TreeNode node = allNodes.get(i); 
			ParseTreeNode parseTreeNode = parseTree.allNodes.get(i); 
			ParseTreeNode parent = parseTreeNode.parent; 
			int parentPos = parseTree.allNodes.indexOf(parent); 
			if(parentPos >= 0)
			{
				node.parent = allNodes.get(parentPos);  
			}
			else
			{
				node.parent = null; 
			}
			
			for(int j = 0; j < parseTreeNode.children.size(); j++)
			{
				ParseTreeNode child = parseTreeNode.children.get(j); 
				int childPos = parseTree.allNodes.indexOf(child); 
				node.children.add(allNodes.get(childPos)); 
			}
		}
		
		hashTreeToNumber(); 
	}
	
	public void treeEvaluation(SchemaGraph schemaGraph, Query query)
	{
		for(int i = 0; i < allNodes.size(); i++)
		{
			allNodes.get(i).haveChildren = new ArrayList<Boolean>(); 
			allNodes.get(i).upValid = true;
			allNodes.get(i).weight = 1; 
		}
		weight = 1; 
		invalid = 0; 
		
		for(int i = 0; i < allNodes.size(); i++)
		{
			allNodes.get(i).NodeEvaluate(schemaGraph, query); 
		}
		
		for(int i = 0; i < allNodes.size(); i++)
		{
			TreeNode node = allNodes.get(i); 
			if(node.upValid == false)
			{
				invalid++; 
			}
			for(int j = 0; j < node.haveChildren.size(); j++)
			{
				if(node.haveChildren.get(j) == false)
				{
					invalid++; 
				}
			}
			
			weight *= node.weight; 
		}
	}
	
	public boolean moveSubTree(TreeNode newParent, TreeNode node)
	{
		if(newParent.equals(node))
		{
			return false; 
		}
		else if(newParent.equals(node.parent))
		{
			return false; 
		}
		
		boolean isParent = false; 
		TreeNode temp = newParent; 
		while(temp != null)
		{
			if(temp.parent != null && temp.equals(node))
			{
				isParent = true; 
				break; 
			}
			temp = temp.parent; 
		}
		
		if(isParent == false)
		{
			TreeNode oldParent = node.parent; 
			oldParent.children.remove(node); 
			newParent.children.add(node); 
			node.parent = newParent; 
			return true; 
		}
		else if(newParent.parent == node && newParent.children.isEmpty() && (newParent.tokenType.equals("OT") || newParent.tokenType.equals("FT")))
		{
			TreeNode nodeParent = node.parent; 
			nodeParent.children.add(newParent); 
			newParent.parent.children.remove(newParent); 
			newParent.parent = nodeParent; 
			nodeParent.children.remove(node); 
			node.parent = newParent; 
			node.children.remove(newParent); 
			newParent.children.add(node); 
			return true; 
		}
		else
		{
			return false; 
		}
	}
	
	public void addEqual()
	{
		TreeNode node = new TreeNode(); 
		node.label = "equals"; 
		node.nodeID = 9999; 
		node.tokenType = "OT"; 
		node.function = "="; 
		node.parent = allNodes.get(0); 
		allNodes.get(0).children.add(node); 
		allNodes.add(node); 
	}
	
	public TreeNode searchNodeByID(int ID)
	{
		for(int i = 0; i < allNodes.size(); i++)
		{
			if(allNodes.get(i).nodeID == ID)
			{
				return allNodes.get(i); 
			}
		}
		
		return null; 
	}
	
	public void hashTreeToNumber()
	{
		LinkedList<TreeNode> stack = new LinkedList<TreeNode>(); 
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		
		stack.add(root); 
		stack.add(root); 
		
		while(!stack.isEmpty())
		{
			TreeNode r = stack.removeLast(); 
			if(!list.contains(r.nodeID))
			{
				for(int i = 0; i < r.children.size(); i++)
				{
					stack.add(r.children.get(i)); 
					stack.add(r.children.get(i)); 
				}
			}
			list.add(r.nodeID); 
		}
		
		HashNum = list.hashCode(); 
	}
	
	public int compareTo(Tree tree) 
	{
		if(this.weight*100-this.cost > tree.weight*100-tree.cost)
		{
			return -1; 
		}
		else if(tree.weight*100-tree.cost > this.weight*100-this.cost)
		{
			return 1; 
		}
		return 0;
	}

	public String toString() 
	{
		String result = "";
		result += "HashNum: " + this.HashNum + "; invalid: " + invalid + "; weight: " + (double)Math.round(weight*100)/100 + "; cost: " + cost + "\n"; 
		LinkedList<TreeNode> nodeList = new LinkedList<TreeNode>(); 
		nodeList.add(root); 
		LinkedList<Integer> levelList = new LinkedList<Integer>(); 
		levelList.add(0); 
		
		while(!nodeList.isEmpty())
		{
			TreeNode curNode = nodeList.removeLast(); 
			int curLevel = levelList.removeLast(); 
			for(int i = 0; i < curLevel; i++)
			{
				result += "    "; 
			}
			result += "(" + curNode.nodeID + ")"; 
			result += curNode.label + "\n"; 
			
			for(int i = 0; i < curNode.children.size(); i++)
			{
				nodeList.add(curNode.children.get(curNode.children.size()-i-1)); 
				levelList.add(curLevel+1); 
			}
		}
		
		return result; 
	}

	public String printForCheck() 
	{
		String result = ""; 
		System.out.println(this.toString()); 
		result += this.toString() + "\n"; 

		System.out.println("All Nodes: "); 
		result += "All Nodes: \n"; 

		for(int i = 0; i < allNodes.size(); i++)
		{
			result += allNodes.get(i).printForCheck();
		}
		

		return result; 
	}
}
