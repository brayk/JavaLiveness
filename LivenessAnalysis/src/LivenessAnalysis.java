// Pair Programmed
// 3798 & 3953
// ==========
// LivenessAnalysis.java:
// - Checks the liveness for a program
// - Supports if-statements, labels, gotos, and assignments
//
// Usage:
// c: javac LivenessAnalysis.java
// r: java LivenessAnalysis input
// ///////////////////////////////////////

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;

public class LivenessAnalysis
{
	
	public static void main(String args[]) throws IOException
	{
		// File name from command line
		String file = args[0];
		
		// Data structure to hold the structure of the program and node information
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		// Lookup dictionary for the Labels
		HashMap<Integer, Node> headNodes = new HashMap<Integer, Node>();
		
		// Building the nodes
		nodes = buildNodes(file);
		
		// putting the labels in the lookup table
		headNodes = buildHeadNodes(nodes);
		
		// link the nodes based on goto
		buildPrevious(nodes, headNodes);
		
		// propagate the liveness rules
		propagateLive(nodes);
		
		// Clear duplicates and order the live strings
		for (Node node : nodes)
		{
			node.liveAfter = clearDuplicates(node.liveAfter);
			node.liveBefore = clearDuplicates(node.liveBefore);
		}
		
		// print nodes and their liveness
		printLiveness(nodes);
	}
	
	private static ArrayList<String> clearDuplicates(ArrayList<String> elements)
	{
		ArrayList<String> al = new ArrayList<>();
		
		// Add elements to hs to remove duplicates
		Set<String> hs = new HashSet<>();
		hs.addAll(elements);
		al.clear();
		al.addAll(hs);
		// Sort the elements after
		Collections.sort(al, new Comparator<String>()
		{
			public int compare(String f1, String f2)
			{
				return f1.toString().compareTo(f2.toString());
			}
		});
		return al;
	}
	
	private static void printLiveness(ArrayList<Node> nodes)
	{
		for (Node node : nodes)
		{
			if (node.type != Node.Type.endLiveType)
			{
				System.out.println(" # " + node.liveBefore);
				System.out.println("" + node.value + " | my previous are: " + node.prev);
				System.out.println(" # " + node.liveAfter);
			}
		}
		
	}
	
	private static void propagateLive(ArrayList<Node> nodes)
	{
		Collections.reverse(nodes);
		for (Node node : nodes)
		{
			node.liveBefore.addAll(node.liveAfter);
			for (Node pNode : node.prev)
			{
				// Adding the nodes that are live before a previous node but making sure it does not exist in the read variables
				pNode.liveAfter.addAll(node.liveBefore);
				
				if (pNode.liveAfter.contains(node.variable) && !node.readVariables.contains(node.variable))
				{
					pNode.liveAfter.removeAll(Collections.singleton(node.variable));
				}
			}
			
			// Make sure to remove variables that are in the non read for the before as well
			if (node.liveBefore.contains(node.variable) && !node.readVariables.contains(node.variable))
			{
				node.liveBefore.removeAll(Collections.singleton(node.variable));
			}
		}
		Collections.reverse(nodes);
		
	}
	
	private static void buildPrevious(ArrayList<Node> nodes, HashMap<Integer, Node> headNodes)
	{
		for (Node node : nodes)
		{
			
			// ADD NODES NEXT IN LINE IF NOT A GOTO
			if (node.type != Node.Type.go2Type)
			{
				int nodeIndex = nodes.indexOf(node);
				if (nodeIndex + 1 < nodes.size())
				{
					Node nextNode = nodes.get(nodeIndex + 1);
					nextNode.prev.add(node);
				}
			}
			
			// ADD NODE PREVIOUS TO HEADNODES / WHERE IF AND GOTOS GO TO
			if (node.type == Node.Type.go2Type || node.type == Node.Type.ifType)
			{
				Node headNode = headNodes.get(node.gotoNumber);
				headNode.prev.add(node);
			}
		}
	}
	
	private static HashMap<Integer, Node> buildHeadNodes(ArrayList<Node> nodes)
	{
		// Builds a set of nodes in a map so it can be collected via the label key/number
		HashMap<Integer, Node> headNodes = new HashMap<Integer, Node>();
		for (Node node : nodes)
		{
			if (node.type == Node.Type.labelType)
			{
				headNodes.put(node.labelNumber, node);
			}
		}
		return headNodes;
	}
	
	private static ArrayList<Node> buildNodes(String file) throws IOException
	{
		// Open the file
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		
		String strLine;
		ArrayList<Node> nodes = new ArrayList<Node>();
		
		// Read File Line By Line
		int lnNu = 0;
		
		// VARIABLE PATTER
		// String variablePatternString = "( [a-z] | [A-Z] )";
		
		String variablePatternString = "( [a-z] | [A-Z] )";
		String endVariablePatternString = "( [a-z]| [A-Z])";
		
		// TOKEN PATTERNS
		String ifPatternString = "(^ if)|(^if)";
		String gotoPatternString = "(^ goto)|(^goto)";
		String assignmentPatternString = "((^[a-z]|[A-Z]) :=.*)|((^ [a-z]|[A-Z]) :=.*)";
		String labelPatternString = "^Label.*";
		String labelNumberPatternString = "(?<=Label)\\d+";
		String endLivePatternString = "(^ #)|(^#)";
		
		Pattern ifPattern = Pattern.compile(ifPatternString);
		Pattern gotoPattern = Pattern.compile(gotoPatternString);
		Pattern assignmentPattern = Pattern.compile(assignmentPatternString);
		Pattern labelPattern = Pattern.compile(labelPatternString);
		
		Pattern labelNumberPattern = Pattern.compile(labelNumberPatternString);
		Pattern endLivePattern = Pattern.compile(endLivePatternString);
		Pattern variablePattern = Pattern.compile(variablePatternString);
		Pattern endVariablePattern = Pattern.compile(endVariablePatternString);
		
		// ASSIGNMENT
		
		// LABEL
		// GOTO
		
		while ((strLine = br.readLine()) != null)
		{
			// Print out the program
			DecimalFormat df = new DecimalFormat("00");
			System.out.println(df.format(lnNu) + " | " + strLine);
			lnNu++;
			
			// Create the nodes in order
			Matcher ifMatcher = ifPattern.matcher(strLine);
			Matcher gotoMatcher = gotoPattern.matcher(strLine);
			Matcher assignmentMatcher = assignmentPattern.matcher(strLine);
			Matcher labelMatcher = labelPattern.matcher(strLine);
			
			Matcher labelNumberMatcher = labelNumberPattern.matcher(strLine);
			Matcher endLiveMatcher = endLivePattern.matcher(strLine);
			Matcher variableMatcher = variablePattern.matcher(strLine);
			Matcher endVariableMatcher = endVariablePattern.matcher(strLine);
			Node node = new Node();
			
			// BUILD THE NODES
			
			// Check if if statement
			if (ifMatcher.find())
			{
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.ifType;
				// Find the read variables
				while (variableMatcher.find())
				{
					String variable = variableMatcher.group(1).replace(" ", "");
					node.liveBefore.add(variable);
				}
				// Find the goto Label
				if (labelNumberMatcher.find())
				{
					String labelNumber = labelNumberMatcher.group(0).replace(" ", "");
					node.gotoNumber = Integer.parseInt(labelNumber);
					
				}
				
			}
			// Check if goto statement
			if (gotoMatcher.find())
			{
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.go2Type;
				
				// Find the goto Label
				if (labelNumberMatcher.find())
				{
					String labelNumber = labelNumberMatcher.group(0).replace(" ", "");
					
					node.gotoNumber = Integer.parseInt(labelNumber);
				}
				
			}
			// Check if assignemnt statement
			if (assignmentMatcher.find())
			{
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.assignType;
				// find variables
				while (variableMatcher.find())
				{
					String variable = variableMatcher.group(0).replace(" ", "");
					node.liveBefore.add(variable);
					node.readVariables.add(variable);
				}
			}
			// check if label
			if (labelMatcher.find())
			{
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.labelType;
				// find label number
				if (labelNumberMatcher.find())
				{
					String labelNumber = labelNumberMatcher.group(0).replace(" ", "");
					node.labelNumber = Integer.parseInt(labelNumber);
				}
			}
			// check if endLive token
			if (endLiveMatcher.find())
			{
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.endLiveType;
				
				// Find variables
				while (endVariableMatcher.find())
				{
					String variable = endVariableMatcher.group(1).replace(" ", "");
					node.liveBefore.add(variable);
				}
				
			}
			
			nodes.add(node);
			
		}
		br.close();
		return nodes;
	}
	
}
