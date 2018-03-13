package tools;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;

public class PrintForCheck 
{
	public static void allParseTreeNodePrintForCheck(ParseTree parseTree)
	{
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			ParseTreeNode node = parseTree.allNodes.get(i); 
			String result = "";
			result += node.nodeID + ". "; 
			result += node.label + ": "; 
			result += node.tokenType + "; "; 
			result += node.function + "; "; 
			result += node.QT + "; ";
			result += "(" + node.choice + ") "; 

			if(node.mappedElements.size() > 0)
			{
				for(int j = 0; j < node.mappedElements.size() && j < 5; j++)
				{
					result += node.mappedElements.get(j).printForCheck() + "| "; 
				}
			}
			System.out.println(result); 
		}
	}
}
