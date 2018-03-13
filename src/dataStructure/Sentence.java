package dataStructure;

import java.util.ArrayList;

public class Sentence 
{
	public ArrayList<String> wordList = new ArrayList<String>(); 
	public String [] outputWords; 
	
	public Sentence(String queryInput)
	{
		wordSplit(queryInput);
		outputWords = new String [wordList.size()]; 
		for(int i = 0; i < outputWords.length; i++)
		{
			outputWords[i] = wordList.get(i); 
		}
	}
	
	// split a sentence into a list of words (words), in which words in " " are considered as one word;  
	private void wordSplit(String queryInput)
	{
		wordList = new ArrayList<String>(); 
		String curWord = ""; 
		while(queryInput.charAt(queryInput.length()-1) == '.' || queryInput.charAt(queryInput.length()-1) == ' '  || queryInput.charAt(queryInput.length()-1) == '?' 
			|| queryInput.charAt(queryInput.length()-1) == '\t' || queryInput.charAt(queryInput.length()-1) == '\n')
		{
			queryInput = queryInput.substring(0, queryInput.length() - 1); 
		}
		queryInput += " "; 
		
		boolean ifCited = false; // if the word is in a "", for example "Star Wars". 
		for(int i = 0; i < queryInput.length(); i++)
		{
			char c = queryInput.charAt(i); 
			if(c == '\t' || c == '\n' || c == ' ')
			{
				if(ifCited == false)
				{
					wordList.add(curWord); 
					curWord = ""; 
					while(i < queryInput.length()-1 && (queryInput.charAt(i+1) == '\t' || queryInput.charAt(i+1) == '\n' 
						|| queryInput.charAt(i+1) == ' ' || queryInput.charAt(i+1) == ','))
					{
						i++; 
					}
				}
				else
				{
					curWord += queryInput.charAt(i); 
				}
			}
			else if(c == '\'')
			{
				if(ifCited == false)
				{
					if(queryInput.charAt(i+1) == 't')
					{
						curWord += queryInput.charAt(i); 
					}
					else
					{
						wordList.add(curWord); 
						wordList.add("\'s"); 
						curWord = ""; 
						if(i < queryInput.length()-1 && queryInput.charAt(i+1) == 's')
						{
							i++; 
						}
						i++; 
					}
				}
				else
				{
					curWord += queryInput.charAt(i); 
				}				
			}
			else if(c == ',')
			{
				if(ifCited == false)
				{
					wordList.add(curWord); 
					wordList.add(","); 
					curWord = ""; 
					while(i < queryInput.length()-1 && (queryInput.charAt(i+1) == '\t' || queryInput.charAt(i+1) == '\n' || queryInput.charAt(i+1) == ' ' 
						|| queryInput.charAt(i+1) == ','))
					{
						i++; 
					}
				}
				else
				{
					curWord += queryInput.charAt(i); 
				}
			}
			else if(c == '\"')
			{
				if(ifCited == false)
				{
					ifCited = true; 
				}
				else
				{
					ifCited = false; 
				}
			}
			else
			{
				curWord += queryInput.charAt(i); 				
			}
		}	
	}
	
	public String printForCheck()
	{
		String result = ""; 
		for(int i = 0; i < outputWords.length; i++)
		{
			result += "\"" + outputWords[i] + "\" "; 
		}
		System.out.println(result); 
		return result + "\n"; 
	}
}
