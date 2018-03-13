package dataStructure;

import java.util.ArrayList;

public class NLSentence 
{
	public ArrayList<ParseTreeNode> allNodes = new ArrayList<ParseTreeNode>(); 
	public ArrayList<String> words = new ArrayList<String>(); 
	public ArrayList<Boolean> isImplicit = new ArrayList<Boolean>(); 
	
	public void addNode(ParseTreeNode node, String word, boolean isImplicit)
	{
		this.allNodes.add(node); 
		this.words.add(word); 
		this.isImplicit.add(isImplicit); 
	}

	public String General() 
	{
		String result = ""; 
		for(int i = 0; i < words.size(); i++)
		{
			if(isImplicit.get(i))
			{
				continue; 
			}
			else
			{
				result += words.get(i); 
			}

			if(i != words.size()-1)
			{
				result += " "; 
			}
			else
			{
				result += ". "; 				
			}
		}
		
		result += "\n"; 
		return result; 
	}
	
	public ArrayList<String> Specific() 
	{
		ArrayList<String> results = new ArrayList<String>(); 
		
		String result = ""; 
		for(int i = 0; i < words.size(); i++)
		{
			if(isImplicit.get(i))
			{
				result += "#implicit " + words.get(i); 
			}
			else
			{
				result += "#explicit " + words.get(i); 
			}

			if(i != words.size()-1)
			{
				result += " "; 
			}
			else
			{
				result += ". "; 				
			}
			result += "\n"; 
			results.add(result); 
			result = ""; 
		}

		return results; 
	}

	public void printForCheck()
	{
		for(int i = 0; i < words.size(); i++)
		{
			if(isImplicit.get(i))
			{
				System.out.print("[" + words.get(i) + "]"); 
			}
			else
			{
				System.out.print(words.get(i)); 
			}

			if(i != words.size()-1)
			{
				System.out.print(" "); 
			}
			else
			{
				System.out.print(". "); 
			}
		}
		
		System.out.println(); 
	}
}
