package myUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import main.Main;
import myUtils.*;

public class Dataset {
	public List<Text> textList;
	public Dictionary dic = new Dictionary();
	public Set<Integer> labelSet = new HashSet<Integer>();

	// dataset for training
	private List<Text> getTrainDataset(String folder, int ngram, int portion) {
		List<Text> d = new ArrayList<Text>();
		try {
			File file = new File(folder);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			{
				int index = 0;
				while ((line = reader.readLine()) != null) {
					if (index++ % 10000 == 0) {
						System.out.print(".");
					}
					if (index % portion != 0)
						continue;
					String content = line;
					Text text = new Text(content, "dev", -1, ngram, dic);
					d.add(text);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}
	
	private List<Text> getImdbDataset(String filePath, int ngram, boolean addData) {
		List<Text> d = new ArrayList<Text>();
		try {
			File file = new File(filePath);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int readDataCount = 0;
			while ((line = reader.readLine()) != null) {
				String key = "train";
				int index = Integer.parseInt(line.split(" ")[0].substring(2));
				if (25000 <= index && index < 50000)
					key = "test";
				int label = -1;
				if (index < 12500)
					label = 1;
				if (12500 <= index && index < 25000)
					label = 0;
				if (25000 <= index && index < 25000 + 12500)
					label = 1;
				if (25000 + 12500 <= index && index < 25000 + 25000)
					label = 0;
				if (addData) {
					if (label != -1)
						continue;
				} else {
					if (label == -1)
						continue;
				}
				String content = line.substring(line.split(" ")[0].length()).trim();
				// System.out.println(content);
				Text text = new Text(content, key, label, ngram, dic);
				d.add(text);
				if (readDataCount++ % 1000 == 0) {
					System.out.print(".");
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public Dataset(String fileName, String type, int ngram, String addDataType) {
		System.out.print("reading dataset:" + fileName);
		textList = new ArrayList<Text>();
		if(type.equals("unlabel"))
			textList = getTrainDataset("./data/" + fileName, ngram, 1);
		if (type.equals("imdb"))
			textList = getImdbDataset("./data/" + fileName, ngram, false);
		
		//some additional unlabeled data
		if (addDataType.contains("news")) {
			String folder = "./data/unlabeled/news.txt";
			List<Text> adtl = getTrainDataset(folder, ngram, Integer.parseInt(addDataType.split("news")[1]));
			for (Text t : adtl) {
				t.type = "dev";
			}
			textList.addAll(adtl);
		}
		if (addDataType.contains("wb")) {
			String folder = "./data/unlabeled/wb.txt";
			List<Text> adtl = getTrainDataset(folder, ngram, Integer.parseInt(addDataType.split("wb")[1]));
			for (Text t : adtl) {
				t.type = "dev";
			}
			textList.addAll(adtl);
		}
		if (addDataType.contains("books")) {
			{
				String folder = "./data/unlabeled/books_large_p1.txt";
				List<Text> adtl = getTrainDataset(folder, ngram, Integer.parseInt(addDataType.split("books")[1]));
				for (Text t : adtl) {
					t.type = "dev";
				}
				textList.addAll(adtl);
			}
		}
		if (addDataType.contains("sick")) {
			String folder = "./data/unlabeled/sick.txt";
			List<Text> adtl = getTrainDataset(folder, ngram, Integer.parseInt(addDataType.split("sick")[1]));
			for (Text t : adtl) {
				t.type = "dev";
			}
			textList.addAll(adtl);
		}
		if (addDataType.contains("imdbd")) {
			String path = "./data/" + "imdb.txt";
			List<Text> adtl = getImdbDataset(path, ngram, true);
			for (Text t : adtl) {
				t.type = "dev";
			}
			textList.addAll(adtl);
		}

		// set dictionary random word rate
		dic.setRandomFactor(Main.pow);

		System.out.println();
		showDetail();
		System.out.println("reading finished");
	}

	public List<Text> getTextList() {
		return textList;
	}

	public void showDetail() {
		System.out.println("text size = " + textList.size());
		System.out.println("vocab size = " + dic.uniqueWordSize());
		System.out.println("total vocab size = " + dic.totalWordSize);
		double length = 0;
		for (Text text : textList) {
			length += text.getIds(false).size();
		}
		length /= textList.size();
		System.out.println("avg length = " + length);
		double l1 = 0;
		double l2 = 0;
		for (Text text : textList) {
			double t = text.getIds(false).size() - length;
			l1 += Math.abs(t);
			l2 += t * t;
		}
		l1 /= textList.size();
		l2 /= textList.size();
		l2 = Math.sqrt(l2);
		System.out.println("l1 = " + l1);
		System.out.println("l2 = " + l2);
	}

}

