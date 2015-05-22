import java.util.ArrayList;
import java.util.Set;


public class Node {
	public enum Type{
		ifType, assignType, labelType, go2Type
	}
	
	String variable;
	ArrayList<Node> prev = new ArrayList<Node>();
	String value;
	Type type;
	int labelNumber;
	int gotoNumber = -1;
	ArrayList<String> liveBefore = new ArrayList<String>();
	ArrayList<String> liveAfter = new ArrayList<String>();
	
	public Node()
	{
			
	}
	public String toString()
	{
		return value;
	}
}
