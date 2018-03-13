package tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class ReadHistory 
{
	public static void main(String [] args) throws ClassNotFoundException, SQLException, IOException
	{
		ReadHistory read = new ReadHistory(); 
		read.readHistory(); 
	}
	
	public void readHistory() throws SQLException, ClassNotFoundException, IOException
	{
		String driver = "com.mysql.jdbc.Driver"; 
		String db_url = "jdbc:mysql://127.0.0.1:3306/";
		String user = "root";
		String password = "caimi";
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(db_url, user, password);
		Statement statement = conn.createStatement(); 

		String file = "/Users/lifei/Dropbox/currentTasks/4. NaliRSystem/workspace/NaliRWeb/src/zfiles/history.txt"; 
		String fileContent = BasicFunctions.readFile(file); 
		
		Scanner scan = new Scanner(fileContent); 
		int count = 1; 
		while(scan.hasNextLine())
		{
			String query = scan.nextLine(); 
			if(query.endsWith(" "))
			{
				query = query.substring(0, query.length()-1); 
			}
			String insert = "INSERT mas_old.history VALUES (" + count + ", " + "'" + query + "')"; 
			count++; 
			statement.executeUpdate(insert); 
		}
		scan.close();
	}
}
