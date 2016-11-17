package model;

import java.awt.event.TextListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import main.Main;
import myUtils.Dataset;
import myUtils.Dictionary;
import myUtils.MyMath;
import myUtils.Text;

public class Model {
	public String folderName;
	private Dataset dataset;

	private float lr; // learning rate
	private float original_lr;
	private float rr; // regulize rate
	private int negSize;
	private int iter;
	private int batchSize;
	private int ws;
	private int n;
	private String info; // infomation used

	private TrainThread ttList[];

	private float WV[][][];
	private float TV[][];
	private float LV[][];
	private double bestAcDev = 0;
	private double ac = 0;

	private static Random random = new Random();

	public Model(String folderName, Dataset dataset, float lr, float rr, int negSize, int iter, int batchSize, int ws,
			int n, String info) {
		this.folderName = folderName;
		this.dataset = dataset;
		this.lr = lr;
		this.original_lr = lr;
		this.rr = rr;
		this.negSize = negSize;
		this.iter = iter;
		this.batchSize = batchSize;
		this.ws = ws;
		this.n = n;
		this.info = info;
		ttList = new TrainThread[30];

		// initialize vectors
		System.out.println("initializing model:vocabSize=" + dataset.dic.uniqueWordSize() + " textSize="
				+ dataset.textList.size());
		if (info.contains("w"))
			WV = new float[dataset.dic.uniqueWordSize()][2][];
		else
			WV = new float[dataset.dic.uniqueWordSize()][1][];
		for (int i = 0; i < WV.length; i++) {
			if (dataset.dic.wordIdCountList.get(i) <= 1)
				continue;

			if (i % 100000 == 0)
				System.out.println("init vectors :" + (i * 100 / dataset.dic.uniqueWordSize()) + "%");
			for (int bi = 0; bi < WV[i].length; bi++) {

				WV[i][bi] = new float[n];

				for (int j = 0; j < WV[i][bi].length; j++)
					WV[i][bi][j] = (random.nextFloat() - 0.5f) / n;
				// for (int j = 0; j < WV[i][bi].length; j++)
				// WV[i][bi][j] = 0;
				// WV[i][bi][random.nextInt(n)] = 1;
				// WV[i][bi][random.nextInt(n)] = 1;

			}
		}

		// one-hot representation
		// for (int i = 0; i < WV.length; i++) {
		// if (i % 100000 == 0)
		// System.out.println(i * 100 / dataset.dic.size());
		// if (dataset.dic.wordIdCountList.get(i) >= 2) {
		// for (int bi = 0; bi < WV[i].length; bi++) {
		// WV[i][bi] = new float[n];
		// for (int j = 0; j < WV[i][bi].length; j++)
		// WV[i][bi][j] = 0;
		// WV[i][bi][i % n] = 1;
		// }
		// }
		// }

		TV = new float[dataset.textList.size()][n];
		for (int i = 0; i < TV.length; i++) {
			for (int j = 0; j < TV[i].length; j++)
				TV[i][j] = (random.nextFloat() - 0.5f) / n;
		}
		LV = new float[20][n];
		for (int i = 0; i < LV.length; i++) {
			for (int j = 0; j < LV[i].length; j++)
				LV[i][j] = (random.nextFloat() - 0.5f) / n;
		}
		System.out.println("initializing finished");
	}

	public boolean isSub(int id) {
		double subsample = Main.subRate * dataset.dic.totalWordSize;
		if (dataset.dic.wordIdCountList.get(id) >= subsample
				&& random.nextDouble() < (1 - Math.sqrt(subsample / dataset.dic.wordIdCountList.get(id))))
			return true;
		return false;
	}

	public static void train(float a[], float b[], boolean mask[], int target, double lr, double rr) {
		if (a == null || b == null)
			return;

		float aa[] = null;
		float bb[] = null;
		if (Main.preLogistic) {
			aa = new float[a.length];
			bb = new float[b.length];
		}

		float y = 0;
		for (int i = 0; i < a.length; i++)
			if (mask == null || mask[i]) {
				if (Main.preLogistic) {
					aa[i] = MyMath.tanh(a[i]);
					bb[i] = MyMath.tanh(b[i]);
					y += aa[i] * bb[i];
				} else {
					y += a[i] * b[i];
				}
			}
		y = MyMath.logistic(y);
		for (int i = 0; i < a.length; i++) {
			if (mask == null || mask[i]) {
				if (Main.preLogistic) {
					a[i] += -(y - target) * bb[i] * (aa[i] * (1 - aa[i] * aa[i])) * lr - rr * a[i] * lr;
					b[i] += -(y - target) * aa[i] * (bb[i] * (1 - bb[i] * bb[i])) * lr - rr * b[i] * lr;
				} else {
					float wv = a[i];
					a[i] += -(y - target) * b[i] * lr - rr * a[i] * lr;
					b[i] += -(y - target) * wv * lr - rr * b[i] * lr;
				}
			}
		}
	}

	public class TrainThread extends Thread {

		public List<Task> taskSubList;
		public float lr;
		public float rr;

		public TrainThread(List<Task> taskSubList, float lr, float rr) {
			this.taskSubList = taskSubList;
			this.lr = lr;
			this.rr = rr;
		}

		private void runTask(Task task, boolean[] mask) {
			if (task.type == 0) {

				if (isSub(task.b))
					return;

				train(TV[task.a], WV[task.b][0], mask, 1, lr, rr);
				for (int neg = 0; neg < negSize; neg++) {
					if (Main.negType.contains("i")) {
						int c = dataset.dic.getRandomWord();
						train(TV[task.a], WV[c][0], mask, 0, lr, rr);
					}
					if (Main.negType.contains("o")) {
						while (true) {
							int c = random.nextInt(TV.length);
							if (c != task.a) {
								train(TV[c], WV[task.b][0], mask, 0, lr, rr);
								break;
							}
						}
					}
				}
			}
			if (task.type == 1) {

				if (isSub(task.b))
					return;

				train(WV[task.a][1], WV[task.b][0], mask, 1, lr, rr);
				for (int neg = 0; neg < negSize; neg++) {
					if (Main.negType.contains("i")) {
						train(WV[task.a][1], WV[dataset.dic.getRandomWord()][0], mask, 0, lr, rr);
					}
					if (Main.negType.contains("o")) {
						while (true) {
							int c = random.nextInt(WV.length);
							if (c != task.a) {
								train(WV[c][1], WV[task.b][0], mask, 0, lr, rr);
								break;
							}
						}
					}
				}
			}
			if (task.type == 2) {

				if (isSub(task.b))
					return;

				train(LV[task.a], WV[task.b][0], mask, 1, lr, rr);
				for (int neg = 0; neg < negSize; neg++) {
					if (Main.negType.contains("i")) {
						train(LV[task.a], WV[dataset.dic.getRandomWord()][0], mask, 0, lr, rr);
					}
				}
				if (Main.negType.contains("o")) {
					while (true) {
						int c = dataset.labelSet.toArray(new Integer[dataset.labelSet.size()])[random
								.nextInt(dataset.labelSet.size())];
						// System.out.println(c);
						if (c != task.a) {
							train(LV[c], WV[task.b][0], mask, 0, lr, rr);
							break;
						}
					}
				}
			}
		}

		private void runSmallTask(Task task, boolean[] mask) {
			Text text = dataset.textList.get(task.a);
			if (task.type == 0) {
				for (int b : text.getIds(Main.useUniqueWordList)) {
					task.b = b;
					train(TV[task.a], WV[task.b][0], mask, 1, lr, rr);
					for (int neg = 0; neg < negSize; neg++) {
						while (true) {
							int c = random.nextInt(TV.length);
							if (c != task.a) {
								train(TV[c], WV[task.b][0], mask, 0, lr, rr);
								break;
							}
						}
					}
				}
			}
			if (task.type == 1) {
				for (Text.Pair p : text.getIdPairList(ws, dataset.dic)) {
					task.a = p.a;
					task.b = p.b;
					train(WV[task.a][1], WV[task.b][0], mask, 1, lr, rr);
					for (int neg = 0; neg < negSize; neg++) {
						while (true) {
							int c = dataset.dic.getRandomWord();
							if (c != task.a) {
								train(WV[c][1], WV[task.b][0], mask, 0, lr, rr);
								break;
							}
						}
					}
				}
			}
			if (task.type == 2) {
				for (int b : text.getIds(Main.useUniqueWordList)) {
					task.a = text.label;
					task.b = b;

					train(LV[task.a], WV[task.b][0], mask, 1, lr, rr);
					for (int neg = 0; neg < negSize; neg++) {
						while (true) {
							int c = dataset.textList.get(random.nextInt(dataset.textList.size())).label;
							if (c != task.a) {
								train(LV[c], WV[task.b][0], mask, 0, lr, rr);
								break;
							}
						}
					}
				}
			}
		}

		public void run() {
			Random random = new Random();

			boolean mask[] = null;
			mask = new boolean[n];
			for (int i = 0; i < mask.length; i++) {
				if (i < mask.length * Main.dropRate)
					mask[i] = false;
				else
					mask[i] = true;
			}
			for (Task task : taskSubList) {

				if (task.b == -1) {
					runSmallTask(task.copy(), mask);
				} else {
					runTask(task, mask);
				}
			}
		}
	}

	public List<Task> getTaskList(int portion, int N) {
		System.out.print("assign task " + portion + "/" + N);
		List<Task> taskList = new ArrayList<Task>();
		double avgWordCount = 1.0 * dataset.dic.totalWordSize / dataset.dic.uniqueWordSize();
		for (int i = 0; i < dataset.textList.size(); i++) {
			if (i % N != portion)
				continue;
			if (i % 5000 == 0)
				System.out.print("|" + (i * 100 / dataset.textList.size()) + "%");
			Text text = dataset.textList.get(i);
			// d
			if (info.contains("d"))
				for (int j : text.getIds(Main.useUniqueWordList)) {
					if (WV[j][0] == null)
						continue;
					for (int t = 0; t < avgWordCount / Math.pow(avgWordCount, Main.pow); t++)
						if (random.nextDouble() < Math.pow(dataset.dic.wordIdCountList.get(j), Main.pow)
								/ dataset.dic.wordIdCountList.get(j))
							taskList.add(new Task(i, j, (short) 0));
				}
			// w
			if (info.contains("w"))
				for (Text.Pair p : text.getIdPairList(ws, dataset.dic)) {
					if (WV[p.a][0] == null)
						continue;
					if (WV[p.b][0] == null)
						continue;
					if (random.nextDouble() < 1.0)
						taskList.add(new Task(p.a, p.b, (short) 1));
				}
			// l
			if (info.contains("l"))
				if (!text.type.equals("test") && text.label != -1)
					for (int j : text.getIds(Main.useUniqueWordList)) {
						if (WV[j][0] == null)
							continue;
						for (int t = 0; t < avgWordCount / Math.pow(avgWordCount, Main.pow); t++)
							if (random.nextDouble() < Math.pow(dataset.dic.wordIdCountList.get(j), Main.pow)
									/ dataset.dic.wordIdCountList.get(j))
								taskList.add(new Task(text.label, j, (short) 2));
					}
		}
		System.out.print(" total " + taskList.size() + " tasks");
		System.out.print(" over");
		return taskList;
	}

	public List<Task> getSmallTaskList() {
		List<Task> taskList = new ArrayList<Task>();
		for (int i = 0; i < dataset.textList.size(); i++) {
			Text text = dataset.textList.get(i);
			// d
			if (info.contains("d"))
				taskList.add(new Task(i, -1, (short) 0));
			// w
			if (info.contains("w"))
				taskList.add(new Task(i, -1, (short) 1));
			// l
			if (info.contains("l"))
				if (text.type.equals("train") && text.label != -1)
					taskList.add(new Task(i, -1, (short) 2));
		}
		return taskList;
	}

	public void runTaskList(List<Task> taskList) {
		Collections.shuffle(taskList);
		int p = 0;
		while (true) {
			boolean over = false;
			for (int i = 0; i < ttList.length; i++) {
				if (ttList[i] == null || !ttList[i].isAlive()) {
					if (p < taskList.size()) {
						int s = p;
						int e = p + batchSize;
						if (taskList.size() < e)
							e = taskList.size();
						ttList[i] = new TrainThread(taskList.subList(s, e), lr, rr);
						ttList[i].start();
						p += batchSize;
					} else {
						over = true;
						break;
					}
				} else {
				}
			}
			if (over)
				break;
		}
	}

	public void waitTrainThreadOver() {
		while (true) {
			boolean over = true;
			for (int i = 0; i < ttList.length; i++)
				if (ttList[i] != null && ttList[i].isAlive())
					over = false;
			if (over)
				break;
		}
	}

	public void saveWordVectors(String vectorFolder) {
		new File(vectorFolder).mkdirs();
		int wordBi = 0;
		try {
			FileWriter fw = new FileWriter(vectorFolder + "WV" + n + ".txt");
			for (Entry<String, Integer> entry : dataset.dic.wordIdMap.entrySet()) {
				if (WV[entry.getValue()][wordBi] == null)
					continue;
				float v[] = WV[entry.getValue()][wordBi];
				fw.write(entry.getKey() + " ");
				for (int j = 0; j < v.length; j++)
					if (v[j] != 0)
						fw.write(v[j] + " ");
				fw.write("\n");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void train() {
		{

			for (int epoch = 0; epoch < iter; epoch++) {

				lr = original_lr * (1 - epoch / iter);
				System.out.print("traning " + epoch + "/" + iter);
				long startTime = System.currentTimeMillis();

				int N = 1;
				for (int portion = 0; portion < N; portion++) {
					List<Task> taskList = getTaskList(portion, N);
					runTaskList(taskList);
					waitTrainThreadOver();
				}

				System.out.print("train over||");
				System.out.println("||" + "|" + "time:" + (System.currentTimeMillis() - startTime));
			}
			waitTrainThreadOver();
		}
	}

	private float predictLabel(int i, int label) {
		Text text = dataset.textList.get(i);
		float p = 0;
		for (int id : text.getIds(Main.useUniqueWordList)) {
			p += MyMath.logistic(WV[id][0], LV[label]);
		}
		p /= text.getIds(Main.useUniqueWordList).size();
		return p;
	}

	private int predictLabel(int i) {
		Text text = dataset.textList.get(i);
		float max = 0;
		int label = -1;
		for (int l : dataset.labelSet) {
			float p = predictLabel(i, l);
			if (p > max) {
				max = p;
				label = l;
			}
		}
		return label;
	}

	public double labelClassify() {
		double correct = 0;
		double total = 0;
		for (int i = 0; i < dataset.textList.size(); i++) {
			Text text = dataset.textList.get(i);
			if (text.type.equals("test")) {
				int label = predictLabel(i);
				if (label == text.label)
					correct++;
				total++;
			}
		}
		return correct / total * 100;
	}

	public void resume(Model bak) { // word vector only
		for (Entry<String, Integer> entry : dataset.dic.wordIdMap.entrySet()) {
			WV[entry.getValue()] = bak.WV[bak.dataset.dic.wordIdMap.get(entry.getKey())];
		}
	}
}
