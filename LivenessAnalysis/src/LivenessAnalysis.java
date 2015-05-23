import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

/* Git push: fixed  dups and removing multiple dups, but not ones read*/

public class LivenessAnalysis {

	public static void main(String args[]) throws IOException {
        String file = args[0];
        //"./test";
		ArrayList<Node> nodes = new ArrayList<Node>();
		HashMap<Integer, Node> headNodes = new HashMap<Integer, Node>();

		nodes = buildNodes(file);
		headNodes = buildHeadNodes(nodes);
		buildPrevious(nodes, headNodes);
        ArrayList<Node> newNodes = new ArrayList<Node>();
        /*while(newNodes != nodes){
            newNodes = nodes.copy();
            propigateLive(nodes);
            
        }*/
		propigateLive(nodes);
	
		
		for(Node node: nodes)
		{
			node.liveAfter = clearDuplicates(node.liveAfter);
			node.liveBefore = clearDuplicates(node.liveBefore);
		}

		
		
		printLiveness(nodes);
		
		
		
		/*
		 * System.out.println("THIS LINE: " + nodes.get(nodes.size() -
		 * 1).value);
		 * 
		 * for (Node node : nodes) { System.out.println(node.value);
		 * System.out.println(node.prev);
		 * 
		 * }
		 */
	}

	private static ArrayList<String> clearDuplicates(ArrayList<String> elements)
	{
		ArrayList<String> al = new ArrayList<>();
		// add elements to al, including duplicates
		Set<String> hs = new HashSet<>();
		hs.addAll(elements);
		al.clear();
		al.addAll(hs);

		Collections.sort(al,
                new Comparator<String>()
                {
                    public int compare(String f1, String f2)
                    {
                        return f1.toString().compareTo(f2.toString());
                    }        
                });
		return al;
	}

	private static void printLiveness(ArrayList<Node> nodes) {
		for(Node node: nodes)
		{
			System.out.println(" # " + node.liveBefore);
			System.out.println("" + node.value + " | my previous are: " + node.prev);
			System.out.println(" # " + node.liveAfter);
		}

	}

	private static void propigateLive(ArrayList<Node> nodes) {
		Collections.reverse(nodes);
		for (Node node : nodes) {
			node.liveBefore.addAll(node.liveAfter);
            for (Node pNode : node.prev) {
				System.out.println("because of " + node.value + " adding: " + node.liveBefore + " to " + pNode.value);
				pNode.liveAfter.addAll(node.liveBefore);
                
                if(pNode.liveAfter.contains(node.variable) && !node.readVariables.contains(node.variable)){
                    pNode.liveAfter.remove(node.variable);
                }
			}
            
            if(node.liveBefore.contains(node.variable) && !node.readVariables.contains(node.variable)){
                node.liveBefore.removeAll(Collections.singleton(node.variable));
            }
		}
		Collections.reverse(nodes);

	}

	private static void buildPrevious(ArrayList<Node> nodes,
			HashMap<Integer, Node> headNodes) {
		for (Node node : nodes) {

			// ADD NODES NEXT IN LINE IF NOT A GOTO
			if (node.type != Node.Type.go2Type) {
				int nodeIndex = nodes.indexOf(node);
				if (nodeIndex + 1 < nodes.size()) {
					Node nextNode = nodes.get(nodeIndex + 1);
					nextNode.prev.add(node);
					//System.out.println("adding: " + node.value + " as previous to " + nextNode.value);
				}
			}

			// ADD NODE PREVIOUS TO HEADNODES / WHERE IF AND GOTOS GO TO
			if (node.type == Node.Type.go2Type || node.type == Node.Type.ifType) {
				Node headNode = headNodes.get(node.gotoNumber);
				headNode.prev.add(node);
				//System.out.println("adding: " + node.value + " as previous to " + headNode.value);
			}
		}
	}

	private static HashMap<Integer, Node> buildHeadNodes(ArrayList<Node> nodes) {
		HashMap<Integer, Node> headNodes = new HashMap<Integer, Node>();
		for (Node node : nodes) {
			if (node.type == Node.Type.labelType) {
				headNodes.put(node.labelNumber, node);
			}
		}

		return headNodes;
	}

	private static ArrayList<Node> buildNodes(String file) throws IOException {
		// Open the file
		FileInputStream fstream = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

		String strLine;
		ArrayList<Node> nodes = new ArrayList<Node>();

		// Read File Line By Line
		int lnNu = 0;

		// VARIABLE PATTER
		//String variablePatternString = "( [a-z] | [A-Z] )";

		//String variablePatternString = "( [a-z] | [A-Z] )|( [a-z]$| [A-Z]$)";
		String variablePatternString = "( [a-z] | [A-Z] )|( [a-z]$| [A-Z]$)|( [a-z]\\z| [A-Z]\\z)";
        
		// TOKEN PATTERNS
		String ifPatternString = "(^ if)";
		String gotoPatternString = "(^ goto)";
		String assignmentPatternString = "([a-z]|[A-Z]) :=.*";
		String labelPatternString = "^Label.*";
		String labelNumberPatternString = "(?<=Label)\\d+";
        String endLivePatternString = "(^ #)";

		Pattern ifPattern = Pattern.compile(ifPatternString);
		Pattern gotoPattern = Pattern.compile(gotoPatternString);
		Pattern assignmentPattern = Pattern.compile(assignmentPatternString);
		Pattern labelPattern = Pattern.compile(labelPatternString);

		Pattern labelNumberPattern = Pattern.compile(labelNumberPatternString);
        Pattern endLivePattern = Pattern.compile(endLivePatternString);
		Pattern variablePattern = Pattern.compile(variablePatternString);

		// ASSIGNMENT

		// LABEL
		// GOTO

		while ((strLine = br.readLine()) != null) {
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
			Node node = new Node();

			if (ifMatcher.find()) {
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.ifType;
				// Find the read variables
				while (variableMatcher.find()) {
					String variable = variableMatcher.group(1).replace(" ", "");
					node.liveBefore.add(variable);
				}
				// Find the goto Label
				if (labelNumberMatcher.find()) {
					String labelNumber = labelNumberMatcher.group(0).replace(
							" ", "");
					node.gotoNumber = Integer.parseInt(labelNumber);

				}

			}
			if (gotoMatcher.find()) {
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.go2Type;

				// Find the goto Label
				if (labelNumberMatcher.find()) {
					String labelNumber = labelNumberMatcher.group(0).replace(
							" ", "");

					node.gotoNumber = Integer.parseInt(labelNumber);
				}

			}
			if (assignmentMatcher.find()) {
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.assignType;

				int count = 0;
				while (variableMatcher.find()) {
					if (count == 0) {
						node.variable = (String) variableMatcher.group(1)
								.replace(" ", "");
					} else {
						String variable = variableMatcher.group(0).replace(" ",
								"");
						node.liveBefore.add(variable);
                        node.readVariables.add(variable);
					}
					count++;
				}
			}
			if (labelMatcher.find()) {
				node = new Node();
				node.value = strLine;
				node.type = Node.Type.labelType;
				if (labelNumberMatcher.find()) {
					String labelNumber = labelNumberMatcher.group(0).replace(
							" ", "");
					node.labelNumber = Integer.parseInt(labelNumber);
				}
			}
            if (endLiveMatcher.find()) {
                node = new Node();
                node.value = strLine;
                node.type = Node.Type.endLiveType;
                System.out.println("endlive node value:" + strLine);
                while(variableMatcher.find()) {
                    String variable = variableMatcher.group(0).replace(" ", "");
                    node.liveBefore.add(variable);
                    System.out.println(node.liveBefore + " BEFORE " );
                }
                System.out.println("endlive node value:" + strLine);
                System.out.println(node.liveBefore + " BEFORE " );
            }

			nodes.add(node);

		}

		// Close the input stream
		br.close();
		/*
		 * for (Node node : nodes) { System.out.println(node.liveBefore);
		 * System.out.println(node.value);
		 * 
		 * }
		 */
		return nodes;
	}

}
