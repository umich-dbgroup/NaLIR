package tools;

import java.util.ArrayList;
import java.util.List;

import rdbms.MappedSchemaElement;

import dataStructure.ParseTreeNode;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.northwestern.at.morphadorner.corpuslinguistics.lemmatizer.EnglishLemmatizer;

public class SimFunctions 
{
	private static int Q = 2; 

	private static EnglishLemmatizer lemmatizer; 

	private static ILexicalDatabase db = new NictWordNet();
    private static RelatednessCalculator wordnet = new WuPalmer(db); 
	private static List<POS[]> posPairs = wordnet.getPOSPairs();

	public static void main(String [] args) throws Exception
	{
		System.out.println(similarity("publication", "publication_num")); 
	}
	
	public static String lemmatize(String word) throws Exception
	{
		if(lemmatizer == null)
		{
			lemmatizer = new EnglishLemmatizer(); 
		}
		return lemmatizer.lemmatize(word); 
	}
	
	public static void similarity(ParseTreeNode treeNode, MappedSchemaElement element)
	{
		if(element.similarity > 0)
		{
			return; 
		}
		
		String nodeLabel = treeNode.label; 
		if(BasicFunctions.isNumeric(nodeLabel) && element.schemaElement.type.equals("number"))
		{
			int sum = 0; 
			for(int i = 0; i < element.mappedValues.size(); i++)
			{
				sum += Integer.parseInt(element.mappedValues.get(i));
			}
			
			int size = Integer.parseInt(nodeLabel)*element.mappedValues.size(); 
			element.similarity = 1-(double)Math.abs(sum-size)/(double)size; 
		}
		else
		{
			double [] sims = new double[element.mappedValues.size()]; 
			ArrayList<String> mappedValues = element.mappedValues; 
			for(int i = 0; i < mappedValues.size(); i++)
			{
				sims[i] = SimFunctions.pqSim(nodeLabel, mappedValues.get(i)); 
			}			
			
			for(int i = 0; i < mappedValues.size(); i++)
			{
				for(int j = i + 1; j < mappedValues.size(); j++)
				{
					if(sims[j] > sims[i])
					{
						double tempSim = sims[j]; 
						sims[j] = sims[i]; 
						sims[i] = tempSim; 
						String tempValue = mappedValues.get(j); 
						mappedValues.set(j, mappedValues.get(i)); 
						mappedValues.set(i, tempValue); 
					}
				}
			}
			
			element.choice = 0; 
			element.similarity = sims[0]; 
		}
	}
	
	public static boolean ifSchemaSimilar(String word1, String word2) throws Exception
	{
		double similarity = similarity(word1, word2); 
		if(similarity > 0.5)
		{
			return true; 
		}
		else
		{
			return false; 
		}
	}
	
	public static double similarity(String word1, String word2) throws Exception
	{
		double similarity = 0; 
		
		similarity = wordNetSim(word1, word2); 
		if(similarity < pqSim(word1, word2))
		{
			similarity = pqSim(word1, word2); 
		}
		similarity += pqSim(word1, word2)/10; 
		
		return similarity; 
	}
	
	public static double wordNetSim(String word1, String word2) throws Exception
	{
		double sim = wordNetSimCompute(word1, word2); 
		String [] words1 = word1.split("_"); 
		String [] words2 = word2.split("_"); 
		
		for(int i = 0; i < words1.length; i++)
		{
			for(int j = 0; j < words2.length; j++)
			{
				double sim_part = wordNetSimCompute(lemmatizer.lemmatize(words1[i]), lemmatizer.lemmatize(words2[j]));
				if(sim_part > sim)
				{
					sim = sim_part; 
				}
			}
		}
		
		return sim; 
	}

	public static double wordNetSimCompute(String word1, String word2)
	{
		double sim = -1D;
		for(POS[] posPair: posPairs) 
		{
		    List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
		    List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());

		    for(Concept synset1: synsets1)
		    {
		        for (Concept synset2: synsets2)
		        {
		            Relatedness relatedness = wordnet.calcRelatednessOfSynset(synset1, synset2);
		            double score = relatedness.getScore();
		            if (score > sim) 
		            { 
		                sim = score;
		            }
		        }
		    }
		}

		if (sim == -1D) 
		{
		    sim = 0.0;
		}
		
		return sim; 
	}
	
	public static double pqSim(String a, String b)
	{
		if(a.isEmpty() || b.isEmpty())
		{
			return 0; 
		}
		
		a = a.toLowerCase(); 
		b = b.toLowerCase(); 
		
		double similarity = 0; 
		String [] arrayA = new String [a.length() - Q + 1]; 
		for(int i = 0; i < arrayA.length; i++)
		{
			arrayA[i] = ""; 
			for(int j = 0; j < Q; j++)
			{
				arrayA[i] += a.charAt(i+j); 
			}
		}
		
		String [] arrayB = new String [b.length() - Q + 1]; 
		for(int i = 0; i < arrayB.length; i++)
		{
			arrayB[i] = ""; 
			for(int j = 0; j < Q; j++)
			{
				arrayB[i] += b.charAt(i+j); 
			}
		}
		
		int same = 0; 
		for(int i = 0; i < arrayA.length; i++)
		{
			for(int j = 0; j < arrayB.length; j++)
			{
				if(arrayA[i].equals(arrayB[j]))
				{
					same++; 
					arrayA[i] = "a"; 
					arrayB[j] = "b"; 
				}
			}
		}
		
		if(arrayA.length != 0 || arrayB.length != 0)
		{
			similarity = 2*(double)same/((double)arrayA.length + (double)arrayB.length); 
		}

		return Math.sqrt(similarity); 
	}
}
