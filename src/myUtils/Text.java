package myUtils;

import java.util.*;

import main.Main;

public class Text {
	private int wordIds[][];
	public String type; // train or test
	public int label;
	int ngram;
	private static Random random = new Random();
	public String s;

	public Text(String s, String type, int label, int ngram, Dictionary dic) {
		s = s.trim();
		s = s.replaceAll(" +", " ");
		this.s = s;
		this.type = type;
		this.label = label;
		this.ngram = ngram;
		String[] tokens = s.split(" ");

		wordIds = new int[ngram][];
		for (int gram = 0; gram < ngram; gram++) {
			int tl = tokens.length - gram;
			if (tl < 0)
				tl = 0;
			wordIds[gram] = new int[tl];
			for (int i = 0; i < tokens.length - gram; i++) {
				String w = "";
				for (int j = 0; j <= gram; j++)
					w += tokens[i + j] + "_";
				// System.out.println(w);

				int index = -1;
				index = dic.addWord(w);
				wordIds[gram][i] = index;
			}
		}
	}

	public List<Integer> getIds(boolean unique) {

		List<Integer> ids = new ArrayList<Integer>();
		for (int gram = 0; gram < ngram; gram++) {
			for (int i = 0; i < wordIds[gram].length; i++) {
				ids.add(wordIds[gram][i]);
			}
		}
		if (unique) {
			Set<Integer> idSet = new HashSet<Integer>();
			idSet.addAll(ids);
			ids.clear();
			ids.addAll(idSet);
		}
		return ids;
	}

	public List<Pair> getIdPairList(int ws, Dictionary dic) {
		List<Pair> pairList = new ArrayList<Pair>();
		for (int gram = 0; gram < ngram; gram++) {
			for (int i = 0; i < wordIds[gram].length; i++) {
				if (Main.subSampleRate != 0) {
					int cn = dic.wordIdCountList.get(wordIds[gram][i]);
					if (Math.sqrt(Main.subSampleRate * dic.totalWordSize / cn) < random.nextDouble())
						continue;
				}
				int l = i - ws;
				int r = i + ws;
				if (gram == 1) {// bigram
					l = i - ws;
					r = i + ws + 1;
				}
				if (gram == 2) { // trigram
					l = i - ws;
					r = i + ws + 2;
				}
				if (l < 0)
					l = 0;
				if (r >= wordIds[0].length)
					r = wordIds[0].length - 1;
				for (int g = 0; g < ngram; g++)
					for (int t = l; t <= r - g; t++)
						pairList.add(new Pair(wordIds[gram][i], wordIds[g][t]));
			}
		}
		return pairList;
	}

	public static class Pair {
		public int a;
		public int b;

		public Pair(int a, int b) {
			this.a = a;
			this.b = b;
		}
	}
}
