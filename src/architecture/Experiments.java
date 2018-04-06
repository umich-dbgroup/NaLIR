package architecture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	public static void main(String [] args) throws Exception
	{
		CommandInterface ci = new CommandInterface();
		ci.executeCommand("#useDB yelp");
		File file = new File("yelp_all.nlqs");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    ci.executeCommand("#query " + line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
	}
}
