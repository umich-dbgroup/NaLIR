package components;

import java.util.ArrayList;

import tools.BasicFunctions;
import dataStructure.NLSentence;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

public class NodeInserter 
{
	public static void addNumberOf(ParseTree parseTree, ParseTreeNode core, ParseTreeNode numberOf)
	{
		numberOf.parent = core.parent; 
		core.parent.children.set(core.parent.children.indexOf(core), numberOf); 
		numberOf.children.add(core); 
		core.parent = numberOf; 
		parseTree.allNodes.add(numberOf); 
	}
	
	public static ParseTreeNode addNode(ParseTree parseTree, ParseTreeNode newParent, ParseTreeNode child)
	{
		ParseTreeNode added = (ParseTreeNode) BasicFunctions.depthClone(child);
		added.children = new ArrayList<ParseTreeNode>(); 
		newParent.children.add(added); 
		added.parent = newParent; 
		parseTree.allNodes.add(added); 
		
		return added; 
	}
	
	public static ParseTreeNode addSubTree(ParseTree parseTree, ParseTreeNode right, ParseTreeNode left)
	{
		ParseTreeNode rightParent = right.parent; 
		
		ParseTreeNode added = (ParseTreeNode) BasicFunctions.depthClone(left);
		added.children.add(right); 
		right.parent = added; 
		
		rightParent.children.set(rightParent.children.indexOf(right), added); 
		added.parent = rightParent; 

		for(int i = 0; i < added.children.size(); i++)
		{
			if(!added.children.get(i).equals(right) && !added.children.get(i).mappedElements.isEmpty() && !right.mappedElements.isEmpty())
			{
				if(added.children.get(i).mappedElements.get(added.children.get(i).choice).schemaElement.elementID == right.mappedElements.get(right.choice).schemaElement.elementID)
				{
					added.children.remove(i); 
					parseTree.allNodes.remove(added.children.get(i)); 
					break; 
				}
			}
		}
		
		ArrayList<ParseTreeNode> addedNodes = new ArrayList<ParseTreeNode>(); 
		addedNodes.add(added); 
		while(!addedNodes.isEmpty())
		{
			ParseTreeNode cur = addedNodes.remove(0); 
			parseTree.allNodes.add(cur); 
			for(int i = 0; i < cur.children.size(); i++)
			{
				addedNodes.add(cur.children.get(i)); 
			}
			
			if(!cur.equals(right))
			{
				cur.isAdded = true; 
			}
		}
		
		return added; 
	}

	public static void deleteNode(ParseTree queryTree, NLSentence NL, int deleteID) 
	{
		if(deleteID < NL.allNodes.size())
		{
			ParseTreeNode deleteNode = NL.allNodes.get(deleteID); 
			NL.allNodes.remove(deleteID); 
			NL.isImplicit.remove(deleteID); 
			NL.words.remove(deleteID); 
			
			if(deleteNode != null)
			{
				queryTree.deleteNode(deleteNode); 
			}
		}
	}
	
	public static void addASubTree(ParseTree parseTree, ParseTreeNode newParent, ParseTreeNode child)
	{
		ParseTreeNode added = (ParseTreeNode) BasicFunctions.depthClone(child);
		newParent.children.add(added); 
		added.parent = newParent; 

		ArrayList<ParseTreeNode> nodeList = new ArrayList<ParseTreeNode>(); 
		nodeList.add(added); 
		while(!nodeList.isEmpty())
		{
			ParseTreeNode curNode = nodeList.remove(0); 
			parseTree.allNodes.add(curNode); 
			nodeList.addAll(curNode.children); 
		}
	}
}
