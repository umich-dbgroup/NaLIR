package components;

import java.util.ArrayList;

import rdbms.MappedSchemaElement;
import dataStructure.NLSentence;
import dataStructure.ParseTreeNode;
import dataStructure.Query;

public class FeedbackGenerator 
{
	public static String feedbackGenerate(ArrayList<String> history, Query query)
	{
		String feedback = ""; 
		for(int i = 0; i < history.size(); i++)
		{
			feedback += "#history " + history.get(i) + "\n"; 
		}

		if(query != null)
		{
			for(int i = 0; i < query.sentence.outputWords.length; i++)
			{
				feedback += "#inputWord " + (i+1) + " " + query.sentence.outputWords[i] + "\n"; 
			}
			
			ArrayList<ParseTreeNode> deletedList = query.parseTree.deletedNodes; 
			for(int i = 0; i < deletedList.size(); i++)
			{
				ParseTreeNode minNode = deletedList.get(i); 
				int minId = i; 
				for(int j = i+1; j < deletedList.size(); j++)
				{
					if(deletedList.get(j).wordOrder < minNode.wordOrder)
					{
						minNode = deletedList.get(j); 
						minId = j; 
					}
				}
				ParseTreeNode temp = deletedList.get(i); 
				deletedList.set(i, minNode); 
				deletedList.set(minId, temp); 
			}
			for(int i = 0; i < deletedList.size(); i++)
			{
				feedback += "#deleted " + deletedList.get(i).wordOrder + " " + deletedList.get(i).label + "\n"; 
			}
			
			ArrayList<ParseTreeNode> allNodes = query.parseTree.allNodes; 
			for(int i = 0; i < allNodes.size(); i++)
			{
				String nodeMap = ""; 
				ParseTreeNode NTVT = allNodes.get(i); 
				if(NTVT.mappedElements.size() > 0)
				{
					if(NTVT.tokenType.equals("VTNUM"))
					{
						nodeMap += "#mapNum ; "; 
					}
					else
					{
						nodeMap += "#map ; "; 
					}
					nodeMap += NTVT.label + "; " + NTVT.wordOrder + "; " + NTVT.choice; 
					
					for(int j = 0; j < NTVT.mappedElements.size() && j < 5; j++)
					{
						nodeMap += "; "; 
						MappedSchemaElement mappedElement = NTVT.mappedElements.get(j); 
						if(mappedElement.schemaElement.type.equals("entity") || mappedElement.schemaElement.type.equals("relationship"))
						{
							nodeMap += mappedElement.schemaElement.name; 
						}
						else
						{
							nodeMap += mappedElement.schemaElement.relation.name + "." + mappedElement.schemaElement.name; 
						}
						
						if(mappedElement.mappedValues.size() > 0 && j == NTVT.choice && NTVT.tokenType.startsWith("VT")
							&& !mappedElement.schemaElement.type.equals("number") && !mappedElement.schemaElement.type.endsWith("k"))
						{
							nodeMap += "#" + mappedElement.choice; 
							for(int k = 0; k < mappedElement.mappedValues.size() && k < 5; k++)
							{
								nodeMap += "#" + mappedElement.mappedValues.get(k); 
							}
						}
					}

					feedback += nodeMap + "\n"; 
				}
			}
			
			if(!query.NLSentences.isEmpty())
			{
				feedback += "#general " + query.queryTreeID + "\n"; 
			}
			for(int i = 0; i < query.NLSentences.size(); i++)
			{
				NLSentence NL = query.NLSentences.get(i); 
				feedback += "#general " + NL.General(); 
			}
			
			ArrayList<String> specific = query.NLSentences.get(query.queryTreeID).Specific(); 
			
			for(int i = 0; i < specific.size(); i++)
			{
				feedback += specific.get(i); 
			}
		}
		
		if(query != null && !query.translatedSQL.isEmpty())
		{
			String title = query.translatedSQL.split("\n")[0]; 
			title = title.substring(7); 
			String [] titles = title.split(", "); 
			String curFeedback = "#result ";
			for(int i = 0; i < titles.length; i++)
			{
				curFeedback += "###" + titles[i].replaceAll("DISTINCT ", ""); 
			}
			feedback += curFeedback + "\n"; 
			
			for(int i = 0; i < query.finalResult.size() && i < 200; i++)
			{
				String cur = "#result "; 
				for(int j = 0; j < query.finalResult.get(i).size(); j++)
				{
					cur += "###" + query.finalResult.get(i).get(j); 
				}
				feedback += cur + "\n"; 
			}
		}
		
		return feedback; 
	}	
}
