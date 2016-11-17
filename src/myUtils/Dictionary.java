package myUtils;

import java.util.*;

public class Dictionary {
	public static Random random = new Random();

	public Map<String, Integer> wordIdMap;
	public List<Integer> wordIdCountList;
	public Map<Integer, String> idWordMap;
	public double wordIdSumList[];
	public int totalWordSize = 0; // how many unique word
	private int uniqueWordSize = 0;

	public Dictionary() {
		wordIdMap = new HashMap<String, Integer>();
		idWordMap = new HashMap<Integer, String>();
		wordIdCountList = new ArrayList<Integer>();
	}

	public int uniqueWordSize() {
		return uniqueWordSize;
	}

	public int addWord(String w) {
		totalWordSize++;
		if (!wordIdMap.containsKey(w)) {
			wordIdMap.put(w, wordIdCountList.size());
			idWordMap.put(wordIdCountList.size(), w);
			wordIdCountList.add(0);
		}
		int index = wordIdMap.get(w);
		wordIdCountList.set(index, wordIdCountList.get(index) + 1);
		return index;
	}

	public void setRandomFactor(double factor) {
		wordIdSumList = new double[wordIdCountList.size()];
		wordIdSumList[0] = (Math.pow(wordIdCountList.get(0), factor));
		for (int t = 1; t < wordIdCountList.size(); t++) {
			wordIdSumList[t] = (Math.pow(wordIdCountList.get(t), factor) + wordIdSumList[t - 1]);
		}
		uniqueWordSize = wordIdMap.size();
//		wordIdCountList.clear();
//		wordIdCountList = null;
//		wordIdMap.clear();
//		wordIdMap = null;
	}

	public int getRandomWord() {

		double i = random.nextDouble() * (wordIdSumList[wordIdSumList.length - 1]);
		int l = 0, r = wordIdSumList.length - 1;
		while (l != r) {
			int m = (l + r) / 2;

			if ((m == 0 || wordIdSumList[m - 1] < i) && i <= wordIdSumList[m])
				return m;
			if (i <= wordIdSumList[m])
				r = m;
			else
				l = m + 1;
		}
		// System.out.println(wordIdSumList[l - 1] + "|" + i + "|" +
		// wordIdSumList[l]);
		return l;
		// return allWord.get(random.nextInt(allWord.size()));
	}
}
