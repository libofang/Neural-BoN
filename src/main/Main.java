package main;

import java.util.*;

import model.Model;
import myUtils.*;
import myUtils.Dictionary;


public class Main {
	// all the parameters are here
	public static int ngram = 1;
	public static float lr = 0.025f; // learning rate //0.1 89.93420.0
	public static float rr = 0.000f; // regulize rate //not used
	public static int negSize = 5; //negative sampling size
	public static int iter = 10; //iteration
	public static int batchSize = 100; 
	public static int ws = 5; //CGNR window size
	public static int n = 500; //word embeddings dimension
	public static String info = "l"; // w, d or l // w stands for CGNR. d stands for TGNR. l stands for LGNR.
	public static boolean useUniqueWordList = false;
	public static String negType = "i"; // i or o. negative sampling type
	public static boolean preLogistic = false;
	public static boolean saveWordVectors = true;
	public static double subSampleRate = 0; 
	public static double dropRate = 0; 
	public static String addDataType = ""; // news4 
	public static double subRate = 1; 
	public static double pow = 1; 
	public static int testId = new Random().nextInt(10000);

	public static class DatasetTask {
		public String folderName;
		public String type;

		public DatasetTask(String folderName, String type) {
			this.folderName = folderName;
			this.type = type;
		}
	}

	public static void train(List<DatasetTask> taskList) {

		for (int index_task = 0; index_task < taskList.size(); index_task++) {
			DatasetTask task = taskList.get(index_task);

			// load dataset
			Dataset dataset = null;
			dataset = new Dataset(task.folderName, task.type, ngram, addDataType);

			// initialize model
			Model model = new Model(task.folderName, dataset, lr, rr, negSize, iter, batchSize, ws, n, info);

			System.out.println(index_task + "||" + index_task + "||" + index_task + "||" + index_task + "||");
			// train model
			model.train();


			{
				long startTime = System.currentTimeMillis();

				// save model
				String baseTmpSaveFolder = "./results/" + task.folderName + ngram + "/" + info + "_" + addDataType + "_" + n
						+ "_r" + testId + "/";
				String tmpSaveFolder = baseTmpSaveFolder + "fold" + index_task + "/";
				if(saveWordVectors)
					model.saveWordVectors(tmpSaveFolder);

				System.out.println("||" + "|" + "time:" + (System.currentTimeMillis() - startTime));
			}
		}
		System.out.println();
		System.out.println(info);
		System.out.println();
	}

	public static void main(String args[]) {
		
		 {
			 List<DatasetTask> taskList = new ArrayList<DatasetTask>();
			 taskList.add(new DatasetTask("books.txt", "unlabel"));
			 //taskList.add(new DatasetTask("imdb.txt", "imdb"));
			 train(taskList);
		 }	
	}
}
