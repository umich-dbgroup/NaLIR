package architecture;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import dataStructure.Query;

import rdbms.RDBMS;
import tools.PrintForCheck;
import tools.BasicFunctions;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

public class Experiments 
{
	LexicalizedParser lexiParser; 
	RDBMS db; 
	Document tokens; 

	public static void main(String [] args) throws Exception
	{
        if (args.length < 1) {
            System.err.println("Error: please specify filename of queries to use.");
            System.exit(1);
        }
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = null;
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		
        /*
		Statement statement = conn.createStatement(); 
		ResultSet rs = statement.executeQuery("SELECT content FROM mas.history"); 
		ArrayList<String> history = new ArrayList<String>(); 
		while(rs.next())
		{
			history.add(rs.getString(1)); 
		}	*/	
		
		Experiments experiment = new Experiments(); 

        String fileName = args[0];
        String[] queryStrs = BasicFunctions.readFile(fileName).split("\n");
        System.out.println("Results for file: " + fileName);
        int i = 1;
        for (String queryStr : queryStrs) {
            System.out.println("------- START QUERY " + i + " ---------");
            try {
                experiment.runExperiments(queryStr, new ArrayList<String>()); 
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("------- END QUERY " + i + " ---------");
            i++;
        }
	}
	
	public Experiments() throws Exception
	{
		lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); 

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
    	DocumentBuilder builder = factory.newDocumentBuilder();
		tokens = builder.parse(new File("/Users/cjbaik/dev/feis_stuff/NaLIRWeb/src/zfiles/tokens.xml")); 
		db = new RDBMS("mas"); 
	}
	
	public void runExperiments(String queryStr, ArrayList<String> history) throws Exception
	{
        Query query = new Query(queryStr, db.schemaGraph);
//		Query query = new Query("return me the homepage of SIGMOD. ", db.schemaGraph);
//		Query query = new Query("return me all the papers in SIGMOD after 2005. ", db.schemaGraph);

//		Query query = new Query("return me the number of papers by \"H. V. Jagadish\" in SIGMOD. ", db.schemaGraph);
//		Query query = new Query("return me the number of papers by \"H. V. Jagadish\" in SIGMOD in each year. ", db.schemaGraph);
//		Query query = new Query("return me the average citations of the papers in SIGMOD. ", db.schemaGraph);
		// Query query = new Query("return me the publication in SIGMOD with the most citations. ", db.schemaGraph);
//		Query query = new Query("return me the total citations of all the papers by \"H. V. Jagadish\". ", db.schemaGraph);
//		Query query = new Query("return me the authors who have more publications than \"H. V. Jagadish\" in SIGMOD after 2000. ", db.schemaGraph);
//		Query query = new Query("return me the authors who have more than 20 papers on SIGMOD. ", db.schemaGraph);
//		Query query = new Query("return me the average number of papers accepted in SIGMOD in each year. ", db.schemaGraph);	
//		Query query = new Query("return me the conferences in database area, whose papers have more than 50000 total citations. ", db.schemaGraph);
		
		components.StanfordNLParser.parse(query, lexiParser); 
		components.NodeMapper.phraseProcess(query, db, tokens); 
//		query.parseTree.searchNodeByID(13).choice = -1; 
		
		components.EntityResolution.entityResolute(query); 
		components.TreeStructureAdjustor.treeStructureAdjust(query, db); 
		
		components.Explainer.explain(query); 
		components.SQLTranslator.translate(query, db); 

//		for(int i = 0; i < query.adjustedTrees.size(); i++)
//		{
//			query.NLSentences.get(i).printForCheck(); 
//			System.out.println(query.adjustedTrees.get(i).toString()); 
//		}
		
		if(query.queryTree.allNodes.size() < 2)
		{
			return; 
		}
		
		query.NLSentences.get(query.queryTreeID).printForCheck(); 
		System.out.println(query.queryTree.toString()); 
		PrintForCheck.allParseTreeNodePrintForCheck(query.queryTree); 

		System.out.println(); 
		for(int i = 0; i < query.blocks.size(); i++)
		{
			query.blocks.get(i).printForCheck(); 
		}			
	}
}
