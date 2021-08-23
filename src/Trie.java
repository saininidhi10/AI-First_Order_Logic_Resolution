import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

class Node {
	String value;
	HashMap<String,Node> children;
	HashSet<HashSet<String>> sentences;
	Node(String value){
		this.value = value;
		children = new HashMap<String, Node>();
		sentences = new HashSet<HashSet<String>>();
	}

	Node(Node n){
		this.value = n.value;
		children = new HashMap<String, Node>(n.children);
		sentences = new HashSet<HashSet<String>>(n.sentences);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	Node addChild(Node child, HashSet<String> sentence) {
		if(sentence!=null)
			child.sentences.add(sentence);
		this.children.put(child.value, child);
		return child;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return value + ":" + children.toString() + "," + sentences.toString();
	}
}

public class Trie {
	Node root;
	String[] argList;
	Trie(String value, String[] args){
		root = new Node(value);
		this.argList = args;
	}
	
	Trie(Trie t){
		root = new Node(t.root);
		argList = Arrays.copyOf(t.argList, t.argList.length);
	}
	
	Node addChild(Node p, String value, HashSet<String> sentence) {
		return p.addChild(new Node(value), sentence);
	}
	
	Trie insert(String predicate, HashSet<String> h) {
		String[] args = predicate.substring(predicate.indexOf('(')+1, predicate.indexOf(')')).split(",");
//		h.remove(predicate);
		Node p = root;
		for (int i=0; i<args.length; i++) {
			if(args[i].charAt(0)>='a' && args[i].charAt(0)<='z')
				args[i] = "var";
			if(i==args.length-1) {				
				if(h.size()>0) {
					if(p.children.containsKey(args[i])) {
						p = p.children.get(args[i]);
						p.sentences.add(h);
					}
					else
						p = addChild(p, args[i], h);
				}
				else {
					if(p.children.containsKey(args[i]))
						p = p.children.get(args[i]);
					else
						p = addChild(p, args[i], null);
				}
			}
			else {
				if(p.children.containsKey(args[i]))
					p = p.children.get(args[i]);
				else
					p = addChild(p, args[i], null);
			}
		}
		return this;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return root.toString();
	}
}
