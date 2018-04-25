package components;

import java.util.ArrayList;

import edu.umich.templar.db.MatchedDBElement;
import rdbms.SchemaElement;

import dataStructure.EntityPair;
import dataStructure.ParseTreeNode;
import dataStructure.Query;

public class EntityResolution 
{
	public static void entityResolute(Query query)
	{
		ArrayList<ParseTreeNode> nodes = query.parseTree.allNodes; 
				
		for(int i = 0; i < nodes.size(); i++)
		{
			ParseTreeNode left = nodes.get(i); 
			if(left.getChoiceMap() == null)
			{
				continue; 
			}
			SchemaElement leftMap = left.getChoiceMap().schemaElement; 

			for(int j = i+1; j < nodes.size(); j++)
			{
				ParseTreeNode right = nodes.get(j); 
				if(right.getChoiceMap() == null)
				{
					continue; 
				}
				SchemaElement rightMap = right.getChoiceMap().schemaElement; 
				
				if(leftMap.equals(rightMap))
				{
					if(left.tokenType.equals("VTTEXT") && right.tokenType.equals("VTTEXT"))
					{
						if(left.label.equals(right.label))
						{
							EntityPair entityPair = new EntityPair(left, right);
							query.entities.add(entityPair);

							// Remove mel
							MatchedDBElement mel = query.melMap.get(right);
							query.interp.getElements().remove(mel);
						}
						else
						{
							continue; 
						}
					}
					
					if((left.tokenType.equals("VTTEXT") && right.tokenType.equals("NT"))
						||(left.tokenType.equals("NT") && right.tokenType.equals("VTTEXT"))
						||(left.tokenType.equals("NT") && right.tokenType.equals("NT")))
					{
						if(Math.abs(left.wordOrder - right.wordOrder) > 2)
						{
							continue; 
						}
						else
						{
							EntityPair entityPair = new EntityPair(left, right);
							query.entities.add(entityPair);

							// Remove mel
							if (left.tokenType.equals("VTTEXT") && right.tokenType.equals("NT")) {
								MatchedDBElement mel = query.melMap.get(right);
								query.interp.getElements().remove(mel);
							} else if (left.tokenType.equals("NT") && right.tokenType.equals("VTTEXT")) {
								MatchedDBElement mel = query.melMap.get(left);
								query.interp.getElements().remove(mel);
							} else {
								MatchedDBElement mel = query.melMap.get(right);
								query.interp.getElements().remove(mel);
							}
						}
					}
				}
			}		
		}
	}
}
