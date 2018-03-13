package components;

import java.util.ArrayList;

import rdbms.RDBMS;

import dataStructure.Block;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;

public class SQLTranslator 
{	
	public static void translate(Query query, RDBMS db)
	{
		preStructureAdjust(query); 
		
		if(query.queryTree.allNodes.size() < 2) 
		{
			return; 
		}
		
		query.blocks = new ArrayList<Block>(); 
		blockSplit(query); 
		query.blocks.get(0).nodeEdgeGen(query.mainBlock, query.queryTree, query.graph); 
		query.blocks.get(0).translate(query.mainBlock, query.queryTree); 
		query.translatedSQL = query.blocks.get(0).SQL; 

		System.out.println(query.translatedSQL); 
		query.finalResult = db.conductSQL(query.translatedSQL); 
	}
	
	public static void preStructureAdjust(Query query)
	{
		if(query.queryTree.allNodes.get(0) != null && query.queryTree.allNodes.get(0).children.size() > 1)
		{
			for(int i = 1; i < query.queryTree.allNodes.get(0).children.size(); i++)
			{
				ParseTreeNode OT = query.queryTree.allNodes.get(0).children.get(i); 
				if(OT.children.size() == 2)
				{
					ParseTreeNode left = OT.children.get(0); 
					ParseTreeNode right = OT.children.get(1); 
					if(right.function.equals("max") || right.function.equals("min"))
					{
						if(right.children.size() == 0)
						{
							components.NodeInserter.addASubTree(query.queryTree, right, left); 
						}
					}
				}
			}
		}
	}
	
	public static void blockSplit(Query query)
	{
		ParseTree queryTree = query.queryTree; 
		
		ArrayList<ParseTreeNode> nodeList = new ArrayList<ParseTreeNode>(); 
		nodeList.add(queryTree.allNodes.get(0));
		
		while(!nodeList.isEmpty())
		{
			ParseTreeNode curNode = nodeList.remove(nodeList.size()-1); 
			Block newBlock = null; 
			if(curNode.parent != null && curNode.parent.tokenType.equals("CMT"))
			{
				newBlock = new Block(query.blocks.size(), curNode); 
				query.blocks.add(newBlock); 
			}			
			else if(curNode.tokenType.equals("FT") && !curNode.function.equals("max"))
			{
				newBlock = new Block(query.blocks.size(), curNode); 
				query.blocks.add(newBlock); 				
			}
			
			for(int i = curNode.children.size()-1; i >= 0; i--)
			{
				nodeList.add(curNode.children.get(i)); 
			}
		}
				
		ArrayList<Block> blocks = query.blocks; 
		if(blocks.size() == 0)
		{
			return; 
		}
		
		Block mainBlock = blocks.get(0); 
		for(int i = 0; i < blocks.size(); i++)
		{
			ParseTreeNode curRoot = blocks.get(i).blockRoot; 
			while(curRoot.parent != null)
			{
				if(curRoot.parent.tokenType.equals("CMT"))
				{
					mainBlock = blocks.get(i); 
					break; 
				}
				curRoot = curRoot.parent; 
			}
		}
		query.mainBlock = mainBlock; 
		
		for(int i = 0; i < blocks.size(); i++)
		{
			Block block = blocks.get(i); 
			if(block.blockRoot.parent.tokenType.equals("OT"))
			{
				block.outerBlock = mainBlock; 
				mainBlock.innerBlocks.add(block); 
			}
			else if(block.blockRoot.parent.tokenType.equals("FT"))
			{
				for(int j = 0; j < blocks.size(); j++)
				{
					if(blocks.get(j).blockRoot.equals(block.blockRoot.parent))
					{
						block.outerBlock = blocks.get(j); 
						blocks.get(j).innerBlocks.add(block); 
					}
				}
			}
		}
	}
}