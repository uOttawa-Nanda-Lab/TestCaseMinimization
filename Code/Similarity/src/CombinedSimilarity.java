import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import simpack.api.ITreeNode;
import simpack.measure.tree.TopDownOrderedMaximumSubtree;
import simpack.measure.tree.BottomUpMaximumSubtree;

public class CombinedSimilarity {
	private String getNodeCharacteristics(ITreeNode node) {
		String nodeName = node.toString().split(":")[0].replace("[", "");
		int numChildren = node.getChildCount();
		String parent = node.getParent() != null ? node.getParent().toString().split(":")[0].replace("[", "") : "";
		String firstChild = node.getChildCount() > 0 && node.getFirstChild() != null ? node.getFirstChild().toString().split(": ")[0].replace("[", "") : "";
		firstChild = firstChild.equals("#text") ? node.getFirstChild().toString().split(": ")[1].replace("]", "") : firstChild;
		String lastChild = node.getChildCount() > 0 && node.getLastChild() != null ? node.getLastChild().toString().split(": ")[0].replace("[", "") : "";
		lastChild = lastChild.equals("#text") ? node.getLastChild().toString().split(": ")[1].replace("]", "") : lastChild;
		String prevSibling = node.getPreviousSibling() != null && node.getPreviousSibling() != null ? node.getPreviousSibling().toString().split(": ")[0].replace("[", "") : "";
		prevSibling = prevSibling.equals("#text") ? node.getPreviousSibling().toString().split(": ")[1].replace("]", "") : prevSibling;
		String numChildrenFirstChild = !firstChild.equals("") ? node.getFirstChild().getChildCount()+"" : "";
		String numChildrenLastChild = !lastChild.equals("") ? node.getLastChild().getChildCount()+"" : "";
		String nodeString = nodeName + "(" + numChildren + "," + numChildrenFirstChild + "," + numChildrenLastChild + ")" + "[" + parent + "," + firstChild + "," + lastChild  + "," + prevSibling + "]";
		return nodeString;
	}
	
	private void TreeToList(ITreeNode node, List treeList){
		treeList.add(getNodeCharacteristics(node));
		Enumeration childs = node.children();
		while (childs.hasMoreElements())
			TreeToList((ITreeNode)childs.nextElement(), treeList);
	}

	private List<List> getSublists(List list) {
    	List sublists = new ArrayList<List>();
		for (int i = 0; i < list.size(); i++)
			for (int j = i+1; j <= list.size(); j++)
				sublists.add(new ArrayList(list.subList(i, j)));
		
		Comparator cmp = new Comparator<ArrayList>()
	    {
	        public int compare(ArrayList s1, ArrayList s2) {
	            return Integer.compare(-s1.size(), -s2.size());
	        }
	    };
	    Collections.sort(sublists, cmp);
		return sublists;
	}
	

	private double getCombinedSubtree(List Subtree1, List Subtree2) {
		for (List l: getSublists(Subtree2))
			if (Collections.indexOfSubList(Subtree1, l) != -1)
				return Subtree1.size() + (Subtree2.size() - l.size());
		
		for (List l: getSublists(Subtree1))
			if (Collections.indexOfSubList(Subtree2, l) != -1)
				return Subtree2.size() + (Subtree1.size() - l.size());

		return Subtree1.size() + Subtree2.size();
	}

	public double getCombinedSimilarity(TopDownOrderedMaximumSubtree TopDown, BottomUpMaximumSubtree BottomUp) {
    	List Tree1List = new ArrayList();
    	TreeToList(TopDown.getTree1(), Tree1List);
    	List Tree2List = new ArrayList();
    	TreeToList(TopDown.getTree2(), Tree2List);

    	List TopDownSubTreeList = new ArrayList();
    	TreeToList(TopDown.getMatchedTree1(), TopDownSubTreeList);

    	if (BottomUp == null || BottomUp.getSimilarity() == null)
    		return TopDownSubTreeList.size()/(Tree1List.size()+Tree2List.size());
    	else {
    		List BottomUpSubTreeList = new ArrayList();
    		TreeToList(BottomUp.getSubtreeRootNodesTree1().get(0), BottomUpSubTreeList);

	    	return getCombinedSubtree(TopDownSubTreeList, BottomUpSubTreeList)*2/(Tree1List.size()+Tree2List.size());
    	}
	}
}
