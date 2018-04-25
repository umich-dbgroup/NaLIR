package components;

import dataStructure.ParseTree;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import edu.umich.templar.db.Database;
import edu.umich.templar.db.MatchedDBElement;
import edu.umich.templar.db.el.*;
import edu.umich.templar.log.graph.LogGraph;
import edu.umich.templar.main.settings.Params;
import edu.umich.templar.scorer.LogGraphScorer;
import edu.umich.templar.task.Interpretation;
import edu.umich.templar.util.Similarity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import rdbms.MappedSchemaElement;
import rdbms.RDBMS;
import tools.BasicFunctions;
import tools.SimFunctions;

import java.util.*;

public class NodeMapper
{
	public static void phraseProcess(Query query, RDBMS db, Document tokens,
									 Database templarDB, LogGraph logGraph) throws Exception
	{
		tokenize(query, tokens); 
		deleteUseless(query); 
		map(query, db); 
		deleteNoMatch(query); 
		individualRanking(query);

		if (logGraph != null) {
			rankConfigs(query, templarDB, logGraph);
		} else {
			groupRanking(query, db);
		}
	}
	
	public static void tokenize(Query query, Document tokens) throws Exception
	{
		ParseTree parseTree = query.parseTree; 
		parseTree.root.tokenType = "ROOT"; // mark the root and the root's children; 
		
		for(int i = 0; i < parseTree.root.children.size(); i++) 
		{
			ParseTreeNode rootChild = parseTree.root.children.get(i);
			if(isOfType(tokens, parseTree, rootChild, "CMT_V", null)) // main verb is CMT (return)
			{
				rootChild.tokenType = "CMT"; 
			}
		}

		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = parseTree.allNodes.get(i); 

			if(curNode.tokenType.equals("NA") && isOfType(tokens, parseTree, curNode, "NEG", null)) // if it is NEG
            {
            	curNode.tokenType = "NEG";  
            }
		}
		
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			ParseTreeNode curNode = parseTree.allNodes.get(i); 
			if(curNode.tokenType.equals("NA") && curNode.relationship.equals("mwe")) // merge multi-word expression;
			{
				if(curNode.wordOrder > curNode.parent.wordOrder)
				{
					curNode.parent.label = curNode.parent.label + " " + curNode.label; 
				}
				else
				{
					curNode.parent.label = curNode.label + " " + curNode.parent.label; 					
				}
				parseTree.deleteNode(curNode); 
				i--; 
			}
		}
		
		int curSize = 0; // delete and merge some nodes; 
		while(curSize != parseTree.allNodes.size())
		{
			curSize = parseTree.allNodes.size(); 
			for(int i = 0; i < parseTree.allNodes.size(); i++)
			{
				ParseTreeNode curNode = parseTree.allNodes.get(i); 

				if(curNode.tokenType.equals("NA") && isOfType(tokens, parseTree, curNode, "FT", "function")) // if it is FT_A
                {
                	curNode.tokenType = "FT";  
                }
                else if(curNode.tokenType.equals("NA") && isOfType(tokens, parseTree, curNode, "OT", "operator"))// OT_A? 
                {
                	curNode.tokenType = "OT";
                }
                else if(curNode.tokenType.equals("NA") && isOfType(tokens, parseTree, curNode, "OBT", null))// OBT?
                {
                	curNode.tokenType = "OBT";
                }
                else if(BasicFunctions.isNumeric(curNode.label))
                {
                	curNode.tokenType = "VT"; 
                }
                else if(curNode.tokenType.equals("NA") && (curNode.pos.startsWith("NN") || curNode.pos.equals("CD"))) // if its POS is N
                {
                    curNode.tokenType = "NTVT";
                }
                else if(curNode.tokenType.equals("NA") && curNode.pos.startsWith("JJ"))
                {
                	curNode.tokenType = "JJ"; 
                }
                else if(curNode.tokenType.equals("NA") && isOfType(tokens, parseTree, curNode, "QT", "quantity")) 
                {
                    curNode.tokenType = "QT";
                } 
			}
		}
	}
	
	public static void deleteUseless(Query query)
	{
		ParseTree parseTree = query.parseTree; 
		query.originalParseTree = (ParseTree) BasicFunctions.depthClone(parseTree); 
		
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			if(parseTree.allNodes.get(i).tokenType.equals("NA") || parseTree.allNodes.get(i).tokenType.equals("QT"))
			{
				ParseTreeNode curNode = parseTree.allNodes.get(i); 
				if(curNode.label.equals("on") || curNode.label.equals("in") || curNode.label.equals("of") || curNode.label.equals("by"))
				{
					if(!curNode.children.isEmpty())
					{
						curNode.children.get(0).prep = curNode.label; 
					}
				}
				
				if(curNode.tokenType.equals("QT"))
				{
					curNode.parent.QT = curNode.function; 
				}
				
				parseTree.deleteNode(curNode); 
				i--; 
			}
		}
	}
	
	public static void map(Query query, RDBMS db) throws Exception
	{
		ParseTree parseTree = query.parseTree; 
		ArrayList<ParseTreeNode> allNodes = parseTree.allNodes; 
		
		for(int i = 0; i < allNodes.size(); i++)
		{
			ParseTreeNode treeNode = allNodes.get(i); 
			if(treeNode.tokenType.equals("NTVT") || treeNode.tokenType.equals("JJ")) // schema+text
			{
				db.isSchemaExist(treeNode); 
				db.isTextExist(treeNode);  
				if(treeNode.mappedElements.size() == 0)
				{
					treeNode.tokenType = "NA"; 
				}
			}
			else if(treeNode.tokenType.equals("VT")) // num
			{
				String OT = "="; 
				if(treeNode.parent.tokenType.equals("OT"))
				{
					OT = treeNode.parent.function; 
				}
				else if(treeNode.children.size() == 1 && treeNode.children.get(0).tokenType.equals("OT"))
				{
					OT = treeNode.children.get(0).function;
					// cjbaik 04/06/2018: Hack for Yelp dataset
					if (OT.equals("NA") && treeNode.children.get(0).label.equalsIgnoreCase("at least")) {
						OT = ">=";
					}
				}
				db.isNumExist(OT, treeNode); 
				{
					treeNode.tokenType = "VTNUM"; 
				} 
			}
		}	
	}
	
	public static void deleteNoMatch(Query query)
	{
		ParseTree parseTree = query.parseTree; 
		
		for(int i = 0; i < parseTree.allNodes.size(); i++)
		{
			if(parseTree.allNodes.get(i).tokenType.equals("NA"))
			{
				ParseTreeNode curNode = parseTree.allNodes.get(i); 
				parseTree.deleteNode(curNode); 
				if(curNode.label.equals("on") || curNode.label.equals("in"))
				{
					curNode.parent.prep = curNode.label; 
				}
				i--; 
			}
		}
	}

	public static void individualRanking(Query query)
	{
		ArrayList<ParseTreeNode> treeNodes = query.parseTree.allNodes; 
		for(int i = 0; i < treeNodes.size(); i++)
		{
			if(treeNodes.get(i).mappedElements.isEmpty())
			{
				continue; 
			}
			
			ParseTreeNode treeNode = treeNodes.get(i); 
			ArrayList<MappedSchemaElement> mappedList = treeNode.mappedElements; 
			for(int j = 0; j < mappedList.size(); j++)
			{
				MappedSchemaElement mappedElement = mappedList.get(j); 
				SimFunctions.similarity(treeNode, mappedElement); 
			}
			
			Collections.sort(mappedList); 
		}
		
		treeNodes = query.parseTree.allNodes; 
		for(int i = 0; i < treeNodes.size(); i++)
		{
			if(!treeNodes.get(i).tokenType.equals("NTVT"))
			{
				continue; 
			}

			ArrayList<MappedSchemaElement> deleteList = new ArrayList<MappedSchemaElement>(); 
			ParseTreeNode treeNode = treeNodes.get(i); 
			ArrayList<MappedSchemaElement> mappedList = treeNode.mappedElements; 
			for(int j = 0; j < mappedList.size(); j++)
			{
				MappedSchemaElement NT = mappedList.get(j); 
				for(int k = j+1; k < mappedList.size(); k++)
				{
					MappedSchemaElement VT = mappedList.get(k); 
					if(NT.mappedValues.isEmpty() && !VT.mappedValues.isEmpty() && NT.schemaElement.equals(VT.schemaElement))
					{
						if(NT.similarity >= VT.similarity)
						{
							VT.similarity = NT.similarity; 
							VT.choice = -1; 
							int VTposition = treeNode.mappedElements.indexOf(VT); 
							treeNode.mappedElements.set(treeNode.mappedElements.indexOf(NT), VT); 
							treeNode.mappedElements.set(VTposition, NT); 
						}
						deleteList.add(NT); 
					}
				}
			}
			
			treeNode.mappedElements.removeAll(deleteList); 
		}
	}

	public static Similarity similarity = new Similarity(10000);
	public static MatchedDBElement convertElToTemplar(Database templarDB, ParseTreeNode node, MappedSchemaElement mse) {
        boolean isFirstMappedChildOfRoot = false;
	    ParseTreeNode curNode = node;
	    while (true) {
	        if (!curNode.parent.mappedElements.isEmpty()) {
	            break;
            }
            if (curNode.parent.tokenType.equals("CMT") || curNode.parent.parent == null) {
	            // Make sure that this is the first child by word order
                int minWordOrder = 9999;
                ParseTreeNode minWordOrderMappedChild = null;
                for (int i = 0; i < curNode.parent.children.size(); i++) {
                    ParseTreeNode child = curNode.parent.children.get(i);
                    if (child.wordOrder < minWordOrder) {
                        minWordOrder = child.wordOrder;
                        minWordOrderMappedChild = child;
                    }
                }
                if (minWordOrderMappedChild != null && minWordOrderMappedChild.equals(curNode)) isFirstMappedChildOfRoot = true;
	            break;
            }
            curNode = curNode.parent;
        }


	    DBElement el;
	    // It's a relation
	    if (mse.schemaElement.relation.equals(mse.schemaElement)) {
	        Relation rel = templarDB.getRelationByName(mse.schemaElement.name);
	        mse.similarity = similarity.sim(node.label, rel.getCleanedName());
	        el = rel;
            mse.choice = -1;
        } else {
	        Relation rel = templarDB.getRelationByName(mse.schemaElement.relation.name);
	        Attribute attr = rel.getAttribute(mse.schemaElement.name);

	        // Figure out if there's any parent or child functions/operators
            String op = "=";
            boolean opSet = false;
            String func = null;
            if (node.parent.tokenType.equals("OT")) {
                op = node.parent.function;
                opSet = true;
            }
            if (node.parent.tokenType.equals("FT")) {
                func = node.parent.function;
            }

            for (ParseTreeNode child : node.children) {
                if (!opSet && child.tokenType.equals("OT")) {
                    op = child.function;
                    opSet = true;
                }
                if (func == null && child.tokenType.equals("FT")) {
                    func = child.function;
                }
            }

	        // It's only a value if the following conditions hold true
	        if (!mse.mappedValues.isEmpty() && mse.choice != -1 && !isFirstMappedChildOfRoot && func == null) {
                if (node.tokenType.equals("VTNUM")) {
                    el = new NumericPredicate(attr, op, Double.valueOf(node.label), null);

                    double prevSim = mse.similarity;
                    if (prevSim > 1.0) prevSim = 1.0;
                    if (prevSim < 0.0) prevSim = 0.0;
                    // We only would have found anything if the predicate exists
                    mse.similarity = ((1.0 - Params.SQLIZER_EPSILON) + prevSim) / 2;
                } else {
                    String value = mse.mappedValues.get(mse.choice);
                    el = new TextPredicate(attr, value);
                    mse.similarity = similarity.sim(node.label, value);
                }
            } else {
	            // Otherwise, it's an attr
                double attrSim = similarity.sim(node.label, attr.getCleanedName());

                if (mse.schemaElement.relation.defaultAttribute.equals(mse.schemaElement)) {
                    double relSim = similarity.sim(node.label, rel.getCleanedName());
                    mse.similarity = Math.max(relSim, attrSim);
                } else {
                    mse.similarity = attrSim;
                }
                mse.choice = -1;
                el = attr;
            }
        }

		return new MatchedDBElement(node.label, el, mse.similarity);
	}

	public static void rankConfigs(Query query, Database templarDB, LogGraph logGraph) {
        List<List<MatchedDBElement>> prunedCands = new ArrayList<>();

        Map<MatchedDBElement, Integer> melToChoiceIndex = new HashMap<>();

		List<ParseTreeNode> mappedNodes = new ArrayList<>();
		for (ParseTreeNode node : query.parseTree.allNodes) {
			if (!node.mappedElements.isEmpty()) {
				mappedNodes.add(node);

				List<MatchedDBElement> pruned = new ArrayList<>();
				prunedCands.add(pruned);

				boolean lastWasExactMatch = false;

				Set<MatchedDBElement> dupl = new HashSet<>();
				for (int i = 0; i < node.mappedElements.size(); i++) {
                    MatchedDBElement mel = convertElToTemplar(templarDB, node, node.mappedElements.get(i));
                    if (dupl.contains(mel)) continue;
                    dupl.add(mel);

                    melToChoiceIndex.put(mel, i);

                    boolean exactMatch = false;
                    if (mel.getScore() >= Params.EXACT_SCORE) {
                        exactMatch = true;
                    }

                    if (lastWasExactMatch && !exactMatch) break;

                    pruned.add(mel);

                    if (pruned.size() >= 5) break;

                    lastWasExactMatch = exactMatch;
                }

			}
		}

		int totalInterpsCount = 1;
		int[] counters = new int[mappedNodes.size()];
		int[] listSizes = new int[mappedNodes.size()];
		for (int i = 0; i < counters.length; i++) {
			counters[i] = 0;
			listSizes[i] = prunedCands.get(i).size();
			totalInterpsCount *= listSizes[i];
		}

		System.out.println("TOTAL INTERPS COUNT: " + totalInterpsCount);

		List<MatchedDBElement> candInterp = new ArrayList<>();
        List<Interpretation> maxInterps = new ArrayList<>();
        LogGraphScorer scorer = new LogGraphScorer(logGraph, true);

        double maxScore = 0.0;

        for (int i = 0; i < totalInterpsCount; i++) {
            for (int j = 0; j < prunedCands.size(); j++) {
                MatchedDBElement mel = prunedCands.get(j).get(counters[j]);
                candInterp.add(mel);
            }

            Interpretation interpObj = new Interpretation(candInterp);
            double score = scorer.score(interpObj);
            interpObj.setScore(score);

            if (score > maxScore) {
                maxInterps = new ArrayList<>();
                maxInterps.add(interpObj);
                maxScore = score;
            } else if (score == maxScore) {
                maxInterps.add(interpObj);
            }
            candInterp = new ArrayList<>();

            int counterIndex = 0;
            counters[counterIndex]++;
            while (counters[counterIndex] >= listSizes[counterIndex]) {
                counters[counterIndex] = 0;

                counterIndex++;
                if (counterIndex >= counters.length) break;

                counters[counterIndex] += 1;
            }
		}

		if (maxInterps.isEmpty()) throw new RuntimeException("No chosen interps!");

        if (maxInterps.size() > 1) {
            System.err.println("Warning: TIE!");
        }

        for (int i = 0; i < maxInterps.get(0).getElements().size(); i++) {
            MatchedDBElement mel = maxInterps.get(0).getElements().get(i);
            mappedNodes.get(i).choice = melToChoiceIndex.get(mel);

            query.melMap.put(mappedNodes.get(i), mel);

            if (mel.getEl() instanceof TextPredicate) {
                mappedNodes.get(i).tokenType = "VTTEXT";
            } else if (mel.getEl() instanceof Attribute) {
                mappedNodes.get(i).tokenType = "NT";
            }
        }

        query.interp = maxInterps.get(0);

        // System.out.println("JOIN PATH: " + maxInterps.get(0).getJoinPath());
	}

	public static void groupRanking(Query query, RDBMS db)
	{
		ParseTreeNode rooot = query.parseTree.allNodes.get(0); 
		double roootScore = 0; 
		for(int i = 0; i < query.parseTree.allNodes.size(); i++)
		{
			ParseTreeNode node = query.parseTree.allNodes.get(i); 
			double score = 0; 
			if(!node.mappedElements.isEmpty())
			{
				if(node.mappedElements.size() == 1)
				{
					score = 1; 
				}
				else
				{
					score = 1 - node.mappedElements.get(1).similarity/node.mappedElements.get(0).similarity; 
				}
				
				if(score >= roootScore)
				{
					rooot = node; 
					roootScore = score; 
				}
			}
		}
		
		if(rooot.label.equals("ROOT"))
		{
			return; 
		}
		rooot.choice = 0; 
		
		boolean [] done = new boolean [query.parseTree.allNodes.size()]; 
		for(int i = 0; i < done.length; i++)
		{
			done[i] = false; 
		}

		ArrayList<ParseTreeNode> queue = new ArrayList<ParseTreeNode>(); 
		queue.add(rooot); 
		queue.add(rooot); 
		
		while(!queue.isEmpty())
		{
			ParseTreeNode parent = queue.remove(0); 
			ParseTreeNode child = queue.remove(0); 
			
			if(done[query.parseTree.allNodes.indexOf(child)] == false)
			{
				if(!parent.equals(child))
				{
					int maxPosition = 0; 
					double maxScore = 0; 
					ArrayList<MappedSchemaElement> mappedElements = child.mappedElements; 
					for(int i = 0; i < mappedElements.size(); i++)
					{
						MappedSchemaElement parentElement = parent.mappedElements.get(parent.choice); 
						MappedSchemaElement childElement = child.mappedElements.get(i); 
						double distance = db.schemaGraph.distance(parentElement.schemaElement, childElement.schemaElement); 
						double curScore = parentElement.similarity * childElement.similarity * distance; 
						
						if(curScore > maxScore)
						{
							maxScore = curScore; 
							maxPosition = i; 
						}
					}
					child.choice = maxPosition; 
				}
				
				if(child.mappedElements.isEmpty())
				{
					for(int i = 0; i < child.children.size(); i++)
					{
						queue.add(parent); 
						queue.add(child.children.get(i)); 
					}
					if(child.parent != null)
					{
						queue.add(parent); 
						queue.add(child.parent); 
					}
				}
				else
				{
					for(int i = 0; i < child.children.size(); i++)
					{
						queue.add(child); 
						queue.add(child.children.get(i)); 						
					}
					if(child.parent != null)
					{
						queue.add(child); 
						queue.add(child.parent); 
					}
				}
				
				done[query.parseTree.allNodes.indexOf(child)] = true; 
			}
		}
		
		for(int i = 0; i < query.parseTree.allNodes.size(); i++)
		{
			ParseTreeNode node = query.parseTree.allNodes.get(i); 
			if(node.tokenType.equals("NTVT") || node.tokenType.equals("JJ"))
			{
				if(node.mappedElements.size() > 0)
				{
					if(node.mappedElements.get(node.choice).mappedValues.size() == 0 || node.mappedElements.get(node.choice).choice == -1)
					{
						node.tokenType = "NT"; 
					}
					else
					{
						node.tokenType = "VTTEXT"; 
					}
				}
			}
			
		}
	}

	public static boolean isOfType(Document tokens, ParseTree tree, ParseTreeNode node, String token, String tag) throws Exception
    {
    	if(isOfType(tokens, tree, node, token, 1, tag))
    	{
    		return true; 
    	}
    	else if(isOfType(tokens, tree, node, token, 2, tag))
    	{
    		return true; 
    	}
    	return false; 
    }
    	
    // test if the given phrase belongs to a given token type: type = 1: lower case; type = 2: original case;  OBT
    public static boolean isOfType(Document tokens, ParseTree tree, ParseTreeNode node, String token, int type, String tag) throws Exception 
    {
    	String label = ""; 
    	if(type == 1)
    	{
    		label = node.label.toLowerCase(); 
    	}
    	else if(type == 2)
    	{
    		label = SimFunctions.lemmatize(node.label).toLowerCase(); 
    	}
    	
    	Element tokenE = (Element)(tokens.getElementsByTagName(token)).item(0); // find the token first;        
        NodeList phrList = tokenE.getElementsByTagName("phrase"); // get its phrases

        for(int i = 0; i < phrList.getLength(); i++) // find the matching phrase
        {
            String phrText = phrList.item(i).getFirstChild().getNodeValue().trim();
            if(phrText.split(" ").length == 1 && !label.contains(" "))
            {
            	if(label.equals(phrText)) 
                {
                    node.tokenType = token; 
                    if(tag != null)
                    {
                        String attText = ((Element)phrList.item(i)).getElementsByTagName(tag).item(0).getFirstChild().getNodeValue().trim();
                        node.function = attText;
                    }
                    return true; 
                }
            }
            else if(phrText.split(" ").length == 1 && label.contains(" "))
            {
            	if(label.contains(phrText+" ")) 
                {
                    node.tokenType = token; 
                    if(tag != null)
                    {
                        String attText = ((Element)phrList.item(i)).getElementsByTagName(tag).item(0).getFirstChild().getNodeValue().trim();
                        node.function = attText;
                    }
                    return true; 
                }
            }
            else if(phrText.contains(label))
            {
            	if(phrText.equals(label))
            	{
            		return true; 
            	}

            	String [] phrWords = phrText.split(" "); 
            	int j = 0; 
            	while(j < phrWords.length)
            	{
            		if(phrWords[j].equals(label))
            		{
            			break; 
            		}
            		j++; 
            	}
            	
            	int index = node.wordOrder; 
            	if((index - j > 1))
            	{
                   	String wholePhrase = ""; 
            		for(int k = 0; (k<phrWords.length-1) && (tree.searchNodeByOrder(index-j+k)!=null); k++)
            		{
            			if(j == k)
            			{
                    		wholePhrase += label + " "; 
            			}
            			else
            			{
                    		wholePhrase += tree.searchNodeByOrder(index-j+k).label + " "; 
            			}
            		}
            		
            		if(tree.searchNodeByOrder(index-j+phrWords.length-1)!=null)
                	{
            			wholePhrase += tree.searchNodeByOrder(index-j+phrWords.length-1).label; 
                	}
                	
                	if(wholePhrase.contains(phrText))
            		{
                        node.tokenType = token; 
                        if(tag != null)
                        {
                            String attText = ((Element)phrList.item(i)).getElementsByTagName(tag).item(0).getFirstChild().getNodeValue().trim();
                            node.function = attText;
                        }
                        node.label = phrText; 
                		for(int k = 0; k < phrWords.length; k++)
                		{
                			if(j != k)
                			{
                				if(tree.searchNodeByOrder(index-j+k) != null)
                        		{
                					tree.deleteNode(tree.searchNodeByOrder(index-j+k)); 
                        		}
                			}
                		}
                        return true; 
            		}
            	}
            }
        }
        return false; 
    }    
}
