package tools;

import java.util.ArrayList;

public class Practice 
{
	public static void main(String[] args) 
	{
		ArrayList<Integer> queue1 = new ArrayList<Integer>(); 
		queue1.add(5); 
		ArrayList<Integer> queue2 = new ArrayList<Integer>(); 
		queue2.add(5); 
		System.out.println(queue1.hashCode()); 
		System.out.println(queue2.hashCode()); 
	}
}
