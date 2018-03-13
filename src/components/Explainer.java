package components;

import java.util.ArrayList;

import tools.SimFunctions;

import dataStructure.NLSentence;
import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;

public class Explainer 
{
	public static void explain(Query query) throws Exception
	{
		for(int i = 0; i < query.adjustedTrees.size(); i++)
		{
			NLSentence NL = explainTree(query.adjustedTrees.get(i)); 
			query.NLSentences.add(NL); 
		}
	}
	
	public static NLSentence explainTree(ParseTree tree) throws Exception
	{
		NLSentence NL = new NLSentence(); 
		
		if(tree.allNodes.get(0) == null || !tree.allNodes.get(0).label.equals("ROOT"))
		{
			return null; 
		}
		
		ParseTreeNode root = tree.allNodes.get(0); 
		if(root.children.isEmpty() || !root.children.get(0).tokenType.equals("CMT"))
		{
			return null; 
		}
		
		ParseTreeNode CMT = root.children.get(0); 
		NL.addNode(CMT, CMT.label, false); 
		
		if(CMT.children.isEmpty())
		{
			return null; 
		}
		
		ParseTreeNode CMTChild = CMT.children.get(0); 
		ParseTreeNode coreNT; 
		
		boolean addThe = false; 
		while(!CMTChild.children.isEmpty() && CMTChild.tokenType.equals("FT"))
		{
			String label = ""; 
			if(addThe == false)
			{
				label += "the "; 
				addThe = true; 
			}
			label += CMTChild.label; 
			NL.addNode(CMTChild, label, false); 
			CMTChild = CMTChild.children.get(0); 
		}
		
		coreNT = CMTChild; 
		
		if(!coreNT.tokenType.equals("NT"))
		{
			return null; 
		}
		
		addCoreNT(coreNT, addThe, NL); 
		
		boolean isWhere = false; 
		for(int i = 1; i < root.children.size(); i++)
		{
			ParseTreeNode condition = root.children.get(i); 
			
			if(!condition.tokenType.equals("OT") || condition.children.size() != 2)
			{
				continue; 
			}
			
			if(isWhere == false)
			{
				NL.addNode(null, "where", false); 
			}
			
			ParseTreeNode left = condition.children.get(0); 
			while(left.tokenType.equals("FT"))
			{
				String label = ""; 
				if(addThe == false)
				{
					label += "the "; 
					addThe = true; 
				}
				label += left.label; 
				NL.addNode(left, label, false); 
				left = left.children.get(0); 
			}
			
			if(!left.parent.tokenType.equals("FT") && left.tokenType.contains("NT") && !left.mappedElements.get(left.choice).schemaElement.type.equals("number"))
			{
				ParseTreeNode numberOf = new ParseTreeNode(-1, "number of", "", "", null);
				numberOf.tokenType = "FT"; 
				numberOf.function = "count"; 
				NodeInserter.addNumberOf(tree, left, numberOf); 
				
				NL.addNode(null, "the", false); 
				NL.addNode(numberOf, "number of", true); 
			}
			if(left.tokenType.equals("NT"))
			{
				addCoreNT(left, true, NL); 
			}
			if(!left.subTreeContain(coreNT))
			{
				ParseTreeNode added = NodeInserter.addNode(tree, left, coreNT); 
				NL.addNode(added, "of the " + SimFunctions.lemmatize(coreNT.label), false); 
			}

			if(!condition.function.equals("="))
			{
				NL.addNode(condition, "is " + condition.toOT(), false); 
			}
			else
			{
				NL.addNode(condition, "is", false); 
			}
			
			ParseTreeNode right = condition.children.get(1); 
			if(right.tokenType.contains("VT") && !right.tokenType.equals("VTTEXT"))
			{
				addCoreNT(right, true, NL); 
			}
			else if(!right.parent.tokenType.equals("FT") && !right.tokenType.equals("FT"))
			{
				ParseTreeNode numberOf = new ParseTreeNode(-1, "number of", "", "", null);
				numberOf.tokenType = "FT"; 
				numberOf.function = "count"; 
				NodeInserter.addNumberOf(tree, right, numberOf); 
				
				NL.addNode(numberOf, "the number of", true); 
			}
			if(right.tokenType.equals("VTTEXT"))
			{
				right = NodeInserter.addSubTree(tree, right, left); 
			}
			if(right.tokenType.equals("NT"))
			{
				addCoreNT(right, true, NL); 
			}
			if(right.tokenType.equals("FT"))
			{
				if(right.function.equals("max"))
				{
					NL.addNode(right, "the most", false); 										
				}
				else if(right.function.equals("min"))
				{
					NL.addNode(right, "the least", false); 					
				}
			}
		}
		
		return NL; 
	}
	
	public static void addCoreNT(ParseTreeNode coreNT, boolean addThe, NLSentence NL)
	{
		ArrayList<ParseTreeNode> nodeList = new ArrayList<ParseTreeNode>(); 
		nodeList.add(coreNT); 
		
		while(!nodeList.isEmpty())
		{
			ParseTreeNode coreChild = nodeList.remove(nodeList.size()-1); 
			String label = ""; 
			
			if(coreChild.equals(coreNT))
			{
				if(addThe == false)
				{
					label += "the "; 
				}
			}
			else if(coreChild.tokenType.equals("NT") && coreChild.prep.equals(""))
			{
				coreChild.prep = "of"; 
			}
			else if(coreChild.tokenType.contains("VT") && coreChild.prep.equals(""))
			{
				if(!coreChild.mappedElements.isEmpty() && coreChild.parent != null && !coreChild.parent.mappedElements.isEmpty())
				{
					if(coreChild.mappedElements.get(coreChild.choice).schemaElement.relation.elementID 
						!= coreChild.parent.mappedElements.get(coreChild.choice).schemaElement.relation.elementID)
					{
						if(NL.isImplicit.get(NL.isImplicit.size()-1) == false)
						{
							coreChild.prep = "of"; 
						}
						else
						{
							NL.words.set(NL.words.size()-1, NL.words.get(NL.words.size()-1) + " of"); 
						}
					}	
				}
			}
			
			if(coreChild.tokenType.equals("OT") && coreChild.children.size() > 0)
			{
				label += coreChild.label + " "; 
				coreChild = coreChild.children.get(0); 
			}
			
			if(!coreChild.prep.isEmpty())
			{
				label += coreChild.prep + " "; 
			}
			
			if(coreChild.QT.equals("each"))
			{
				label += coreChild.QT + " "; 
			}
			
			if(coreChild.label.split(" ").length > 1)
			{
				label += "\"" + coreChild.label + "\""; 
			}
			else
			{
				label += coreChild.label; 
			}
			NL.addNode(coreChild, label, coreChild.isAdded); 
			
			for(int i = coreChild.children.size() - 1; i >= 0; i--)
			{
				nodeList.add(coreChild.children.get(i)); 
			}
		}
	}
}
