// 3798 & 3953
// ==========
// Node.java: 
// class to organize the lines of code
// ///////////////////////////////////////

import java.util.ArrayList;

public class Node
{
	public enum Type
	{
		ifType, assignType, labelType, go2Type, endLiveType
	}
	
	String				variable;
	ArrayList<Node>		prev			= new ArrayList<Node>();
	String				value;
	Type				type;
	int					labelNumber;
	int					gotoNumber		= -1;
	ArrayList<String>	liveBefore		= new ArrayList<String>();
	ArrayList<String>	liveAfter		= new ArrayList<String>();
	ArrayList<String>	readVariables	= new ArrayList<String>();
	
	public Node()
	{
		
	}
	
	public String toString()
	{
		return value;
	}
}
