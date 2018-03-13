package components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;

import rdbms.RDBMS;
import rdbms.SchemaGraph;
import tools.BasicFunctions;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import dataStructure.Tree;
import dataStructure.TreeNode;
import dataStructure.NLSentence;

public class TreeStructureAdjustor 
{
	public static void treeStructureAdjust(Query query, RDBMS db)
	{
		query.adjustingTrees = new ArrayList<Tree>(); 
		Hashtable<Integer, Integer> preTrees = new Hashtable<Integer, Integer>(); 
		
		adjust(query, db, false, preTrees); 
		if(query.adjustingTrees.size() == 0 || (query.adjustingTrees.size() > 0 && query.adjustingTrees.get(0).cost > 3))
		{
			boolean maxMin = false; 
			for(int i = 0; i < query.parseTree.allNodes.size(); i++)
			{
				if(query.parseTree.allNodes.get(i).function.equals("max") || query.parseTree.allNodes.get(i).function.equals("min"))
				{
					maxMin = true; 
				}
			}
			if(maxMin == true)
			{
				adjust(query, db, true, preTrees); 
			}
		}
		
		ArrayList<Tree> adjustedTrees = query.adjustingTrees; 
		
		Collections.sort(adjustedTrees); 
		for(int i = 0; i < adjustedTrees.size(); i++)
		{ 
			for(int j = 0; j < adjustedTrees.get(i).allNodes.size(); j++)
			{
				TreeNode node = adjustedTrees.get(i).allNodes.get(j); 
				Collections.sort(node.children); 
			}
			adjustedTrees.get(i).hashTreeToNumber(); 
		}

		LinkedList<Integer> list = new LinkedList<Integer>(); 
		for(int i = 0; i < adjustedTrees.size(); i++)
		{
			if(list.contains(adjustedTrees.get(i).HashNum))
			{
				adjustedTrees.remove(i); 
				i--; 
			}
			else
			{
				list.add(adjustedTrees.get(i).HashNum); 
			}
		}				
		
		buildAdjustedTrees(query); 
	}
	
	public static void preAdjust(Tree tree)
	{
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			if(tree.allNodes.get(i).function.equals("avg") || tree.allNodes.get(i).function.equals("sum"))
			{
				if(tree.allNodes.get(i).children.isEmpty())
				{
					tree.moveSubTree(tree.allNodes.get(i), tree.allNodes.get(i).parent); 
				}
			}
			else if(tree.allNodes.get(i).tokenType.equals("OT") && tree.allNodes.get(i).children.isEmpty())
			{
				tree.moveSubTree(tree.allNodes.get(i), tree.allNodes.get(i).parent); 
			}
		}
	}
	
	public static void adjust(Query query, RDBMS db, boolean addEqual, Hashtable<Integer, Integer> preTrees)
	{
		ParseTree parseTree = query.parseTree; 
		ArrayList<Tree> adjustedTrees = query.adjustingTrees; 
		query.NLSentences = new ArrayList<NLSentence>(); 

		Tree tree = new Tree(parseTree); 
		preAdjust(tree); 
		
		if(addEqual)
		{
			tree.addEqual(); 
		}
		tree.treeEvaluation(db.schemaGraph, query); 
		
		if(tree.invalid == 0)
		{
			adjustedTrees.add(tree); 
		}
		
		ArrayList<Tree> queue = new ArrayList<Tree>(); 
		queue.add(tree); 

		preTrees.put(tree.HashNum, tree.cost); 		
		
		while(!queue.isEmpty() && queue.size() < 100)
		{
			Tree curTree = queue.remove(0); 
			
			ArrayList<Tree> extendedTrees = extend(curTree, db.schemaGraph, query); 
			for(int i = 0; i < extendedTrees.size(); i++)
			{
				Tree addTree = extendedTrees.get(i); 
				if(preTrees.containsKey(addTree.HashNum))
				{
					if(preTrees.get(addTree.HashNum) > addTree.cost)
					{
						preTrees.remove(addTree.HashNum); 
						preTrees.put(addTree.HashNum, addTree.cost); 
					}
				}
				else
				{
					queue.add(addTree); 
					preTrees.put(addTree.HashNum, tree.cost); 
					if(addTree.invalid == 0)
					{
						adjustedTrees.add(addTree); 
					}
				}
			}			
		}
	}
	
	public static ArrayList<Tree> extend(Tree tree, SchemaGraph schemaGraph, Query query)
	{		
		ArrayList<Tree> extendedTrees = new ArrayList<Tree>(); 

		if(tree.cost > 4)
		{
			return extendedTrees; 
		}
		
		for(int i = 1; i < tree.allNodes.size(); i++)
		{
			extendedTrees.addAll(extendNode(tree, tree.allNodes.get(i), schemaGraph, query)); 
		}

		for(int i = 0; i < extendedTrees.size(); i++)
		{
			Tree newTree = extendedTrees.get(i); 
			newTree.hashTreeToNumber(); 
		}

		return extendedTrees; 
	}
	
	public static ArrayList<Tree> extendNode(Tree tree, TreeNode node, SchemaGraph schemaGraph, Query query)
	{
		ArrayList<Tree> extendedTrees = new ArrayList<Tree>(); 
		for(int i = 0; i < tree.allNodes.size(); i++)
		{
			Tree newTree = (Tree)BasicFunctions.depthClone(tree); 
			newTree.cost++; 

			if(newTree.allNodes.get(i).nodeID != node.nodeID)
			{
				boolean ifAdded = newTree.moveSubTree(newTree.allNodes.get(i), newTree.searchNodeByID(node.nodeID)); 
				
				if(ifAdded == true)
				{
					newTree.treeEvaluation(schemaGraph, query); 
					if(newTree.invalid < tree.invalid || (newTree.invalid == tree.invalid && newTree.weight*10000 - newTree.cost > tree.weight*10000 - tree.cost))
					{
						extendedTrees.add(newTree); 
					}
				}
			}
		}
		
		return extendedTrees; 
	}
	
	public static void buildAdjustedTrees(Query query)
	{
		ArrayList<Tree> adjustingTrees = query.adjustingTrees; 

		query.adjustedTrees = new ArrayList<ParseTree>(); 
		ArrayList<ParseTree> adjustedTrees = query.adjustedTrees; 
		
		for(int i = 0; i < adjustingTrees.size() && i < 5; i++)
		{
			Tree adjustingTree = adjustingTrees.get(i); 
			ParseTree adjustedTree = (ParseTree) BasicFunctions.depthClone(query.parseTree); 
			
			for(int j = 0; j < adjustingTree.allNodes.size(); j++)
			{
				TreeNode lackNode = adjustingTree.allNodes.get(j); 
				
				if(adjustedTree.searchNodeByID(lackNode.nodeID) == null)
				{
					ParseTreeNode newNode = new ParseTreeNode(lackNode.nodeID, lackNode.label, "", "", null); 
					newNode.tokenType = lackNode.tokenType; 
					newNode.nodeID = lackNode.nodeID; 
					newNode.function = lackNode.function; 
					adjustedTree.allNodes.add(newNode); 
				}	
			}
			
			for(int j = 0; j < adjustedTree.allNodes.size(); j++)
			{
				adjustedTree.allNodes.get(j).children = new ArrayList<ParseTreeNode>(); 
			}
			
			for(int j = 0; j < adjustingTree.allNodes.size(); j++)
			{
				TreeNode curNode = adjustingTree.allNodes.get(j); 
				ParseTreeNode curParseNode = adjustedTree.searchNodeByID(curNode.nodeID); 
				
				if(!curNode.label.contains("ROOT"))
				{
					curParseNode.parent = adjustedTree.searchNodeByID(curNode.parent.nodeID); 					
				}
				
				for(int k = 0; k < curNode.children.size(); k++)
				{
					curParseNode.children.add(adjustedTree.searchNodeByID(curNode.children.get(k).nodeID)); 
				}
			}
			
			adjustedTrees.add(adjustedTree); 
		}
		
		if(query.adjustedTrees.size() > 0)
		{
			query.queryTreeID = 0; 
			query.queryTree = adjustedTrees.get(query.queryTreeID); 
		}
		
		for(int i = 0; i < query.queryTree.allNodes.size(); i++)
		{
			ParseTreeNode OT = query.queryTree.allNodes.get(i); 
			if(OT.tokenType.equals("OT") && OT.children.size() == 2)
			{
				ParseTreeNode left = OT.children.get(0); 
				ParseTreeNode right = OT.children.get(1); 
				
				if(left.tokenType.equals("VTNUM"))
				{
					OT.children.set(1, left); 
					OT.children.set(0, right); 
				}
			}
		}
	}
}
