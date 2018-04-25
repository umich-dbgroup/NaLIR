package dataStructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.umich.templar.db.MatchedDBElement;
import edu.umich.templar.task.Interpretation;
import rdbms.SchemaGraph;


public class Query 
{
	public SchemaGraph graph; 
	
	public static int queryID = 0; 
	public Sentence sentence;

	public ArrayList<String []> treeTable = new ArrayList<String []>(); // the dependency tree table: Position, Phrase, Tag, Parent, all strings; each phrase is an entry
    public ArrayList<String> conjTable = new ArrayList<String>(); // conjunction table: a^b

	public ParseTree originalParseTree; 
	public ParseTree parseTree;
	
	public ArrayList<EntityPair> entities = new ArrayList<EntityPair>(); 

	public ArrayList<Tree> adjustingTrees = new ArrayList<Tree>(); 
	public ArrayList<ParseTree> adjustedTrees = new ArrayList<ParseTree>(); 
	public ArrayList<NLSentence> NLSentences = new ArrayList<NLSentence>(); 
	
	public int queryTreeID = 0; 
	public ParseTree queryTree = new ParseTree(); 
	
	public Block mainBlock;
	public ArrayList<Block> blocks = new ArrayList<Block>(); 

	public String translatedSQL = "";  
	public ArrayList<ArrayList<String>> finalResult = new ArrayList<ArrayList<String>>(); 

	public Interpretation interp = null;
	public Map<ParseTreeNode, MatchedDBElement> melMap = new HashMap<>();

	public Query(String queryInput, SchemaGraph graph)
	{
		sentence = new Sentence(queryInput); 
		this.graph = graph; 
	}
}
