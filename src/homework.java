import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class homework {
	String query;
	ArrayList<String> KB;
	HashMap<String, Trie> cnfIndex;

	homework(String query, ArrayList<String> KB){
		this.query  = query;
		this.KB = new ArrayList<String>(KB);
		cnfIndex = new HashMap<String, Trie>();
	}
	
	static void processInput(String input, ArrayList<String> queries, ArrayList<String> KB) {
//		queries = new ArrayList<String>();;
//		KB = new ArrayList<String>();
		try {
			File myObj = new File(input);
			Scanner myReader = new Scanner(myObj);
			int query_num = Integer.parseInt(myReader.nextLine().trim().replace(" ", "").replace("\t", ""));
//			System.out.println(query_num);
			for (int i = 0; i < query_num; i++)
				queries.add(myReader.nextLine().trim().replace(" ", "").replace("\t", ""));
//			System.out.println(queries);
			int kb_num = Integer.parseInt(myReader.nextLine().trim().replace(" ", "").replace("\t", ""));
//			System.out.println(kb_num);
			for (int i = 0; i < kb_num; i++)
				KB.add(myReader.nextLine().trim().replace(" ", "").replace("\t", ""));
//			System.out.println(KB + "\n");
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static HashMap<String, Trie> copy(HashMap<String, Trie> original) {
		HashMap<String, Trie> copy = new HashMap<String, Trie>();
		for (Map.Entry<String, Trie> entry : original.entrySet()) {
			copy.put(entry.getKey(),
					// Or whatever List implementation you'd like here.
					new Trie(entry.getValue()));
		}
		return copy;
	}

	String start() {
		converttoCNF(KB);
		String result = "FALSE";
//		for (int i = 0; i < queries.size(); i++) {
//			HashMap<String, Trie> cnfIndexcopy = copy(cnfIndex);
			String sentenceString = negateSentence(query);
			HashSet<String> sentence = new HashSet<String>();
			sentence.add(sentenceString);
			insertKey(sentenceString, new HashSet<String>(sentence));
//			Queue<HashSet<String>> q = new LinkedList<HashSet<String>>();
			Stack<HashSet<String>> stack = new Stack<HashSet<String>>();
//			q.add(sentence);
			stack.add(sentence);
			boolean solved = false;
			HashSet<HashSet<String>> visited = new HashSet<HashSet<String>>();
			visited.add(sentence);
			int counter = 0;
			while (!stack.empty() && counter < 1000) {
//				sentence = q.remove();
				sentence = stack.pop();
//				System.out.println(sentence);
				ArrayList<HashSet<String>> newSentences = resolution(sentence);
				for (HashSet<String> hashSet : newSentences) {
					if (!visited.contains(hashSet)) {
						if (hashSet.contains("FALSE")) {
							solved = true;
							break;
						}
//						q.add(hashSet);
						stack.push(hashSet);
						visited.add(hashSet);
					}
				}
				if (solved)
					break;
				counter += 1;
			}
			if (solved) {
//				System.out.println("TRUE\n");
				result="TRUE";
			} else {
//				System.out.println("FALSE\n");
				result="FALSE";
			}
//			cnfIndex = cnfIndexcopy;
//		}
		return result;
	}

	private static void writeToFile(ArrayList<String> result) {
		try {
			FileWriter myWriter = new FileWriter("output.txt");
			for (int i = 0; i < result.size(); i++) {
				if (i == result.size() - 1)
					myWriter.write(result.get(i));
				else
					myWriter.write(result.get(i) + "\n");
			}
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	void insertKey(String sentence, HashSet<String> sentences) {
		String key = sentence.substring(0, sentence.indexOf('('));
		if (cnfIndex.containsKey(key))
			cnfIndex.get(key).insert(sentence, sentences);
		else {
			Trie t = new Trie(key, sentence.substring(sentence.indexOf('(') + 1, sentence.indexOf(')')).split(","));
			t.insert(sentence, sentences);
			cnfIndex.put(key, t);
		}
	}

	void converttoCNF(ArrayList<String> KB) {
		for (int i = 0; i < KB.size(); i++) {
			if (KB.get(i).contains("=>")) {
				String[] temp = KB.get(i).split("=>");
				ArrayList<String> sentences;
				sentences = handleAnd(temp[0]);
				sentences.add(temp[1]);
				for (String sentence : sentences) {
					insertKey(sentence, new HashSet<String>(sentences));
				}

			} else {
				HashSet<String> sentences = new HashSet<String>();
				sentences.add(KB.get(i));
				insertKey(KB.get(i), new HashSet<String>(sentences));
			}

		}
//		System.out.println("CNF converted KB is as follows:\n" + cnfIndex + "\n");
	}

	ArrayList<String> handleAnd(String premise) {
		String[] temp = premise.split("&");
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < temp.length; i++)
			result.add(negateSentence(temp[i]));
		return result;
	}

	String negateSentence(String singleSentence) {
		if (singleSentence.contains("~"))
			return new String(singleSentence.substring(1));
		else
			return new String("~" + singleSentence);
	}

	ArrayList<HashSet<String>> resolution(HashSet<String> query) {
		ArrayList<HashSet<String>> result = new ArrayList<HashSet<String>>();
		for (String predicate : query) {
			String name = predicate.substring(0, predicate.indexOf('('));
			String[] args = predicate.substring(predicate.indexOf('(') + 1, predicate.indexOf(')')).split(",");
			String key = negateSentence(name);
			if (cnfIndex.containsKey(key)) {
				Trie t = cnfIndex.get(key);
				Node n = t.root;
				HashSet<HashSet<String>> sentences = new HashSet<HashSet<String>>();
				// Exact Match
				Node temp = n;
				int i = 0;
				for (i = 0; i < args.length; i++) {
					if (temp.children.containsKey(args[i]))
						temp = temp.children.get(args[i]);
					else
						break;
				}

				if (i == args.length) {
					HashSet<String> qremain = new HashSet<String>(query);
					qremain.remove(predicate);
					if (qremain.size() > 0)
						result.add(qremain);
					else {
						HashSet<String> res = new HashSet<String>();
						res.add("FALSE");
						result.clear();
						result.add(res);
						return result;
					}
				}

				// Unify with others
				Queue<Node> q = new LinkedList<Node>();
				q.add(n);
				for (i = 0; i <= args.length; i++) {
					HashSet<Node> newq = new HashSet<Node>();
					while (!q.isEmpty()) {
						n = q.remove();
						if (n.sentences.size() > 0)
							sentences.addAll(n.sentences);
						if (i < args.length && isVariable(args[i])) {
							newq.addAll(n.children.values());
						}
						if (n.children.containsKey("var"))
							newq.add(n.children.get("var"));
						if (i < args.length && n.children.containsKey(args[i])) {
							newq.add(n.children.get(args[i]));
						}
					}
					q = new LinkedList<>(newq);
				}

				if (sentences.size() > 0) {
					ArrayList<HashSet<String>> res = unifySentence(t.argList, predicate, key, args, sentences,
							new HashSet<String>(query));
					result.addAll(res);
				}

			}
		}
		Collections.sort(result, new SizeComarator());
		return result;
	}

	boolean isVariable(String s) {
		return s.charAt(0) >= 'a' && s.charAt(0) <= 'z';
	}

	String[] getArgs(HashSet<String> sentence, String predName, String[] oldArgs) {
		for (String string : sentence) {
			if (string.startsWith(predName)) {
				String[] args = string.substring(string.indexOf('(') + 1, string.indexOf(')')).split(",");
				for (int i = 0; i < args.length; i++) {
					if (isVariable(oldArgs[i]) && isVariable(args[i]))
						oldArgs[i] = args[i];
				}
				break;
			}
		}
		return oldArgs;
	}

	ArrayList<HashSet<String>> unifySentence(String[] qargs, String qpredicate, String name, String[] args,
			HashSet<HashSet<String>> sentences, HashSet<String> query) {
		ArrayList<HashSet<String>> unifiedSentences = new ArrayList<HashSet<String>>();
		HashSet<String> qremain = new HashSet<String>(query);
		qremain.remove(qpredicate);

		for (HashSet<String> sentence : sentences) {
			HashSet<String> newSentence = new HashSet<String>();
			qargs = getArgs(sentence, name, qargs);
			for (String predicate : sentence) {
				if (predicate.startsWith(name))
					continue;
				String temp = predicate;
				for (int j = 0; j < qargs.length; j++) {
					if (isVariable(qargs[j]) && !(isVariable(args[j]))) {
						if (predicate.contains('(' + qargs[j] + ','))
							temp = temp.replace("(" + qargs[j] + ",", "(" + args[j] + ",");
						else if (predicate.contains(',' + qargs[j] + ','))
							temp = temp.replace("," + qargs[j] + ",", "," + args[j] + ",");
						else if (predicate.contains(',' + qargs[j] + ')'))
							temp = temp.replace("," + qargs[j] + ")", "," + args[j] + ")");
						else if (predicate.contains('(' + qargs[j] + ')'))
							temp = temp.replace("(" + qargs[j] + ")", "(" + args[j] + ")");
					}
				}
				newSentence.add(new String(temp));
			}
			if (newSentence.size() > 0) {
				newSentence.addAll(qremain);
				for (String t : newSentence) {
					insertKey(t, new HashSet<String>(newSentence));
				}
				if (newSentence.size() > 0)
					unifiedSentences.add(newSentence);
			}
		}
		for (HashSet<String> sentence : sentences) {
			for (String predicate : sentence) {
				if (predicate.contains(name)) {
					HashSet<String> predremain = new HashSet<String>(sentence);
					predremain.remove(predicate);
					String[] predArgs = predicate.substring(predicate.indexOf('(') + 1, predicate.indexOf(')'))
							.split(",");
					HashSet<String> newSentence = new HashSet<String>(query);
					for (int j = 0; j < args.length; j++) {
						if (isVariable(args[j]) && !(isVariable(predArgs[j]))) {
							ArrayList<String> removeSet = new ArrayList<String>();
							ArrayList<String> addSet = new ArrayList<String>();
							for (String q : newSentence) {
								String temp = new String(q);
								if (q.contains('(' + args[j] + ','))
									temp = temp.replace("(" + args[j] + ",", "(" + predArgs[j] + ",");
								else if (q.contains(',' + args[j] + ','))
									temp = temp.replace("," + args[j] + ",", "," + predArgs[j] + ",");
								else if (q.contains(',' + args[j] + ')'))
									temp = temp.replace("," + args[j] + ")", "," + predArgs[j] + ")");
								else if (q.contains('(' + args[j] + ')'))
									temp = temp.replace("(" + args[j] + ")", "(" + predArgs[j] + ")");
								if (!temp.equals(q)) {
									removeSet.add(new String(q));
									addSet.add(new String(temp));
								}
							}
							newSentence.removeAll(removeSet);
							newSentence.addAll(addSet);
						}
					}
					if (!newSentence.equals(query)) {
						newSentence.addAll(predremain);
						for (String t : newSentence) {
							insertKey(t, new HashSet<String>(newSentence));
						}
						unifiedSentences.add(newSentence);
					}
				}

			}
		}
		return unifiedSentences;
	}

	public static void main(String[] args) throws IOException {
		ArrayList<String> queries = new ArrayList<String>();
		ArrayList<String> KB = new ArrayList<String>();
		ArrayList<String> result = new ArrayList<String>();
		processInput("C:\\Users\\saini\\eclipse-workspace\\FOL_Nidhi\\Test cases\\input_1.txt", queries, KB);
		for (String query : queries) {
			homework h = new homework(query, KB);
			result.add(h.start());
			h = null;
		}
		writeToFile(result);
	}
}

class SizeComarator implements Comparator<HashSet<?>> {

	@Override
	public int compare(HashSet<?> o1, HashSet<?> o2) {
		return -1 * Integer.valueOf(o1.size()).compareTo(o2.size());
	}
}