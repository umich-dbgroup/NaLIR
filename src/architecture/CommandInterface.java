package architecture;

import java.io.File;
import java.util.Scanner;

import rdbms.RDBMS;
import tools.BasicFunctions;
import dataStructure.ParseTreeNode;
import dataStructure.Query;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import components.*;

public class CommandInterface 
{
	LexicalizedParser lexiParser; 
	RDBMS db; 
	Document tokens; 
	
	Query query; 
	public String feedback = ""; 

	public static void main(String [] args) throws Exception
	{
		CommandInterface system = new CommandInterface();
		System.out.println("NaLIR Loaded. Type 'exit' to quit.");
		// system.executeCommand("#useDB mas");

		Scanner scan = new Scanner(System.in);
		while (true)
		{
			System.out.print("NaLIR> ");
			String command = scan.nextLine();

			if (command.equals("exit")) {
				break;
			}
			system.executeCommand(command);
		}
		scan.close();
	}
	
	public CommandInterface() throws Exception
	{
		lexiParser = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); 

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
    	DocumentBuilder builder = factory.newDocumentBuilder();
		tokens = builder.parse(new File("/Users/cjbaik/dev/NaLIR/src/zfiles/tokens.xml"));
	}

	public void executeCommand(String command) throws Exception
	{
		System.out.println("command: " + command); 
		
		if(command.startsWith("#useDB") && command.length() > 7)
		{
			db = new RDBMS(command.substring(7)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
			query = null; 
		}
		else if(command.startsWith("#query") && command.length() > 7)
		{
			inputQuery(command.substring(7)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
		}
		else if(command.startsWith("#mapSchema") && command.length() > 11)
		{
			mapChoice(command.substring(11)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
		}
		else if(command.startsWith("#mapValue") && command.length() > 10)
		{
			mapValueChoice(command.substring(10)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
		}
		else if(command.startsWith("#general") && command.length() > 9)
		{
			setGeneral(command.substring(9)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
		}
		else if(command.startsWith("#specific") && command.length() > 10)
		{
			deleteSpecific(command.substring(10)); 
			feedback = FeedbackGenerator.feedbackGenerate(db.history, query); 
		}
		
		System.out.println(FeedbackGenerator.feedbackGenerate(db.history, query)); 		
	}
	
	public void inputQuery(String queryInput) throws Exception
	{
		query = new Query(queryInput, db.schemaGraph); 
		components.StanfordNLParser.parse(query, lexiParser); 
		components.NodeMapper.phraseProcess(query, db, tokens); 
		
		components.EntityResolution.entityResolute(query); 
		components.TreeStructureAdjustor.treeStructureAdjust(query, db); 
		components.Explainer.explain(query); 
		components.SQLTranslator.translate(query, db); 
	}
	
	public void mapChoice(String choiceInput) throws Exception
	{
		String [] commands = choiceInput.split(" "); 
		if(commands.length == 2)
		{
			int wordOrder = Integer.parseInt(commands[0]); 
			int schemaChoice = Integer.parseInt(commands[1]); 
			ParseTreeNode node = query.parseTree.searchNodeByOrder(wordOrder); 
			node.choice = schemaChoice; 
		}
		
		components.EntityResolution.entityResolute(query); 
		components.TreeStructureAdjustor.treeStructureAdjust(query, db); 
		components.Explainer.explain(query); 
		components.SQLTranslator.translate(query, db); 
	}

	public void mapValueChoice(String choiceInput) throws Exception
	{
		String [] commands = choiceInput.split(" "); 
		if(commands.length >= 2)
		{
			int wordOrder = Integer.parseInt(commands[0]); 
			int valueChoice = Integer.parseInt(commands[1]); 
			ParseTreeNode node = query.parseTree.searchNodeByOrder(wordOrder); 
			
			node.mappedElements.get(node.choice).choice = valueChoice; 
		}

		components.EntityResolution.entityResolute(query); 
		components.TreeStructureAdjustor.treeStructureAdjust(query, db); 
		components.Explainer.explain(query); 
		components.SQLTranslator.translate(query, db); 
	}
	
	public void setGeneral(String choiceInput)
	{
		if(BasicFunctions.isNumeric(choiceInput))
		{
			query.queryTreeID = Integer.parseInt(choiceInput);
			components.SQLTranslator.translate(query, db); 
		}
	}
	
	private void deleteSpecific(String deleteID) 
	{
		if(BasicFunctions.isNumeric(deleteID))
		{
			components.NodeInserter.deleteNode(query.queryTree, query.NLSentences.get(query.queryTreeID), Integer.parseInt(deleteID)); 
			components.SQLTranslator.translate(query, db); 
		}		
	}
}