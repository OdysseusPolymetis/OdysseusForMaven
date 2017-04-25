package fr.odysseus.utils;

import java.util.LinkedList;

import fr.odysseus.dataModels.NWRecord;

public class ReorderingLists {
	public static LinkedList<NWRecord>reorder(LinkedList<NWRecord> prevList,LinkedList<NWRecord> newList){
		for (int j = 0; j < prevList.size(); j++) {
			if (newList.get(j).getSrc() == "^" && newList.get(j).getTrg() == "^") {
				prevList.remove(newList.get(j));
				newList.remove(j);
				j--;
//				System.out.println("je réorganise en 1");
			} else if (newList.get(j).getSrc().equals("^") && newList.get(j).getTrg().equals("")) {
				prevList.remove(newList.get(j));
				newList.remove(j);
				j--;
//				System.out.println("je réorganise en 2");
			} else if (newList.get(0).getSrc().equals("^") && newList.get(0).getTrg().equals("") == false) {
				newList.get(1).setTrg(prevList.get(0).getTrg() + " " + prevList.get(1).getTrg());
				newList.get(1).setLemma(prevList.get(0).getLemma() + " " + prevList.get(1).getLemma());
				newList.get(1).setTag(prevList.get(0).getTag() + " " + prevList.get(1).getTag());
				newList.remove(0);
				prevList.remove(0);
				j--;
//				System.out.println("je réorganise en 3");
			} else if (j < newList.size() && j > 0 && newList.get(j).getSrc().equals("^") && newList.get(j).getTrg().equals("") == false) {
				newList.get(j - 1).setTrg(prevList.get(j - 1).getTrg() + " " + prevList.get(j).getTrg());
				newList.get(j - 1).setLemma(prevList.get(j - 1).getLemma() + " " + prevList.get(j).getLemma());
				newList.get(j - 1).setTag(prevList.get(j - 1).getTag() + " " + prevList.get(j).getTag());
				newList.remove(j);
				prevList.remove(j);
				j--;
//				System.out.println("je réorganise en 4");
			}
		}
		return newList;
	}
}