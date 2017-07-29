package ComparisonSimilarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;

public class M1MTMJ {
	/**
	 * Euclidean similarity.
	 */
	public static final int EUCLIDEAN = 0;

	/**
	 * Triangle multiplying Jaccard similarity.
	 */
	public static final int TMJ = 2;

	/**
	 * Jaccard similarity.
	 */
	public static final int Jaccard = 3;

	/**
	 * Cosine distance.
	 */
	public static final int Cosine = 4;

	/**
	 * Triangle similarity.
	 */
	public static final int Triangle = 5;

	/**
	 * PIP similarity.
	 */
	public static final int PIP = 6;

	/**
	 * NHSM similarity.
	 */
	public static final int NHSM = 7;

	/**
	 * BC similarity.
	 */
	public static final int BC = 8;

	/**
	 * Pearson similarity.
	 */
	public static final int Pearson = 9;

	/**
	 * CPCC similarity.
	 */
	public static final int CPCC = 10;
	/**
	 * 
	 * /** Running steps
	 */
	private static double runSteps;

	/**
	 * The total number of users
	 */
	private int userNumber;

	/**
	 * The total number of items
	 */
	private int itemNumber;

	/**
	 * How many user-item pairs are counted for each user/item.
	 * 
	 * Popularity of each user/item, for example, a movie has watched by 5
	 * users, Its popularity is 5.
	 */
	private int[] itemDegree;
	private double[] itemTotalRating;
	private double[] itemAveRating;
	private int[] userDegree;
	private double[] userTotalRating;
	private double[] userAveRating;
	private double[] standarddeviation;
	private double[][] everyItemClassfication;
	/**
	 * The item rating information of each user, including scores and rated
	 * indices.
	 */
	private double[][] itemRatingInformation;
	private int[][] itemRatingIndices;
	private double[][] userRatingInformation;
	private int[][] userRatingIndices;

	/**
	 * The total score of all neighbors on this user/item. It is used for
	 * prediction.
	 */
	private double neighborScoreTotal;

	/**
	 * The position of the item which is leaved out.
	 */
	private int leaveOutItemPosition;

	/**
	 * The index of the item which is leaved out in compress matrix.
	 */
	private int leaveOutItemIndex;

	/**
	 * The value of the item which is leaved out.
	 */
	private double leaveOutValue;


	/**
	 ************************* 
	 * Construct the compress rating matrix. 
	 * @param paraUsers
	 * 			the number of users.
	 * @param paraItems
	 * 			the number of items.
	 * @throws Exception
	 ************************* 
	 */
	public M1MTMJ(int paraUsers, int paraItems) throws Exception {
		// Initialize these arrays
		itemNumber = paraItems;
		userNumber = paraUsers;
		itemRatingInformation = new double[itemNumber][];
		itemRatingIndices = new int[itemNumber][];
		userRatingInformation = new double[userNumber][];
		userRatingIndices = new int[userNumber][];

		itemTotalRating = new double[itemNumber];
		itemAveRating = new double[itemNumber];
		standarddeviation = new double[itemNumber];
		userTotalRating = new double[userNumber];
		userAveRating = new double[userNumber];

		String tempReaderfile = "data/movielens/1M/ratings.dat";
		File tempFile = null;
		BufferedReader tempBufReader = null;

		String tempString = null;
		int tempRating = 0;
		int tempUserIndex = 0;
		int tempItemIndex = 0;
		String[] tempStrArray = null;

		// Compute values of arrays
		tempFile = new File(tempReaderfile);
		if (!tempFile.exists()) {
			return;
		}// Of if
		tempBufReader = new BufferedReader(new FileReader(tempFile));

		int[][] tempItemIndices = new int[itemNumber][userNumber];
		int[][] tempItemRatings = new int[itemNumber][userNumber];
		itemDegree = new int[itemNumber];
		int[][] tempUserIndices = new int[userNumber][itemNumber];
		int[][] tempUserRatings = new int[userNumber][itemNumber];
		// int[] tempUserCount = new int[userNumber];
		userDegree = new int[userNumber];

		while ((tempString = tempBufReader.readLine()) != null) {
			tempStrArray = tempString.split("::");
			tempUserIndex = Integer.parseInt(tempStrArray[0]);
			tempItemIndex = Integer.parseInt(tempStrArray[1]);
			tempRating = Integer.parseInt(tempStrArray[2]);
			if (tempUserIndex > (userNumber - 1)) {
				break;
			}
			tempItemIndices[tempItemIndex][itemDegree[tempItemIndex]] = tempUserIndex;
			tempItemRatings[tempItemIndex][itemDegree[tempItemIndex]] = tempRating;
			itemTotalRating[tempItemIndex] += tempRating;
			itemDegree[tempItemIndex]++;
		}// Of while

		tempBufReader.close();

		for (int i = 0; i < itemDegree.length; i++) {
			itemRatingInformation[i] = new double[itemDegree[i]];
			itemRatingIndices[i] = new int[itemDegree[i]];

			// Compress
			for (int j = 0; j < itemDegree[i]; j++) {
				itemRatingInformation[i][j] = tempItemRatings[i][j];
				itemRatingIndices[i][j] = tempItemIndices[i][j];
			}// Of for j

			itemAveRating[i] = itemTotalRating[i] / itemDegree[i];
		}// Of for i
		
		for (int i = 0; i < userDegree.length; i++) {
			userRatingInformation[i] = new double[userDegree[i]];
			userRatingIndices[i] = new int[userDegree[i]];

			// Compress
			for (int j = 0; j < userDegree[i]; j++) {
				userRatingInformation[i][j] = tempUserRatings[i][j];
				userRatingIndices[i][j] = tempUserIndices[i][j];
			}// Of for j
			userAveRating[i] = userTotalRating[i] / userDegree[i];
		}// Of for i
	}// Of the first constructor

	/**
	 ************************* 
	 * Leave one out.
	 * 
	 * @author Fan Min
	 ************************* 
	 */
	public void leaveOneOut(int paraItemIndex, int paraItemPosition,
			int paraUserIndex, double paraValue) {
		leaveOutItemIndex = paraItemIndex;
		runSteps++;
		leaveOutItemPosition = paraItemPosition;
		runSteps++;
		runSteps++;
		leaveOutValue = paraValue;
		runSteps++;

		itemRatingInformation[paraItemIndex][paraItemPosition] = 0;
		runSteps++;

		itemTotalRating[paraItemIndex] -= leaveOutValue;
		runSteps++;
		itemDegree[paraItemIndex]--;
		runSteps++;
		if (itemDegree[paraItemIndex] > 1e-6) {
			itemAveRating[paraItemIndex] = itemTotalRating[paraItemIndex]
					/ itemDegree[paraItemIndex];
		} else {
			itemAveRating[paraItemIndex] = 0;
		}// Of if
		runSteps++;
	}// Of leaveOneOut

	/**
	 ************************* 
	 * Leave one out restore.
	 * 
	 * @author Fan Min
	 ************************* 
	 */
	public void leaveOneOutRestore() {
		// userRatingInformation[leaveOutItemIndex][leaveOutItemPosition] =
		// leaveOutValue;
		// runSteps++;
		itemRatingInformation[leaveOutItemIndex][leaveOutItemPosition] = leaveOutValue;
		runSteps++;

		itemDegree[leaveOutItemIndex]++;
		runSteps++;
		itemTotalRating[leaveOutItemIndex] += leaveOutValue;
		runSteps++;
		itemAveRating[leaveOutItemIndex] = itemTotalRating[leaveOutItemIndex]
				/ itemDegree[leaveOutItemIndex];
		runSteps++;
	}// Of leaveOneOutRestore
	
	/**
	 ************************* 
	 * Initialize space, suit Euclidean similarity .
	 * @param paraNeighborsIndice
	 * @param paraNeighborsDistance
	 ************************* 
	 */
	public void InitializeSpaceBigToSmall(int[] paraNeighborsIndice,
			double[] paraNeighborsDistance, int[] paraCheckIndice) {
		paraNeighborsIndice[0] = -1;
		paraNeighborsDistance[0] = Double.MAX_VALUE;
		paraCheckIndice[0] = -1;
		for (int i = 1; i < paraNeighborsDistance.length; i++) {
			paraNeighborsIndice[i] = -1;
			paraNeighborsDistance[i] = -10;
			paraCheckIndice[i] = -1;
		}// of for i
	}// of InitializeSpaceBigToSmall

	/**
	 ************************* 
	 * Initialize space, suit Euclidean similarity .
	 * @param paraNeighborsIndice
	 * @param paraNeighborsDistance
	 ************************* 
	 */
	public void InitializeSpaceSmallToBig(int[] paraNeighborsIndice,
			double[] paraNeighborsDistance, int[] paraCheckIndice) {
		paraNeighborsIndice[0] = -1;
		paraNeighborsDistance[0] = -10;
		paraCheckIndice[0] = -1;
		for (int i = 1; i < paraNeighborsDistance.length; i++) {
			paraNeighborsIndice[i] = -1;
			paraNeighborsDistance[i] = 10;
			paraCheckIndice[i] = -1;
		}// of for i
	}// of InitializeSpaceSmallToBig


	/**
	 ************************* 
	 * Prediction, and return the deviation. The distance measure is the average
	 * rating.
	 * 
	 * @author Fan Min
	 ************************* 
	 */
	public double[] computeOneDeviationKNNMAE(int paraItemIndex,
			int[] paraUserIndexArray, double[] paraRatingArray, int paraK, int paraMeasure)
			throws Exception {
 
		double[] tempResult = new double[2];
		for (int i = 0; i < paraUserIndexArray.length; i++) {
			// Step 1. Leave the one out.
			leaveOneOut(paraItemIndex, i, paraUserIndexArray[i],
					paraRatingArray[i]);
			// Step 2. Predict.
			double tempPrediction = 0;

			if (paraMeasure == EUCLIDEAN) {
				tempPrediction = predictKNNDistance(paraItemIndex, i, paraUserIndexArray,
						paraRatingArray, paraK,paraMeasure);
			}else{
				tempPrediction = predictKNN(paraItemIndex, i, paraUserIndexArray,
						paraRatingArray, paraK,paraMeasure);
			}
			
			// Of if
			runSteps++;
			tempResult[0] += Math.abs(tempPrediction - leaveOutValue);
			runSteps++;
			tempResult[1] += (tempPrediction - leaveOutValue)
					* (tempPrediction - leaveOutValue);
			runSteps++;
			// Step 4. Restore.
			leaveOneOutRestore();
			runSteps++;
		}// Of for i

		// Step 5. Return
		return tempResult;
	}// of computeOneDeviationKNNMAE

	/**
	 ************************* 
	 * Prediction Euclidean similarity.
	 * 
	 ************************* 
	 */
	public double predictKNNDistance(int paraPredictItemArrayIndex,
			int paraUserPosition, int[] paraUserIndexArray,
			double[] paraRatingArray, int paraK, int paraMeasure)
			throws Exception {

		// Step 1. Find the neighbors.
		neighborScoreTotal = 0;
		int[] tempNeighborIndices = new int[paraK + 2];
		double[] tempNeighborDistances = new double[paraK + 2];
		int[] tempcheckIndices = new int[paraK + 2];

		InitializeSpaceSmallToBig(tempNeighborIndices, tempNeighborDistances,
				tempcheckIndices); 

		double tempDistance;
		// System.out.println("The item  " + paraPredictUserArrayIndex);
		for (int i = 0; i < itemNumber; i++) {
			// Step 1.1 exclude myself

			if (i == paraPredictItemArrayIndex) {
				continue;
			}// Of if
			int tempCheck = Arrays.binarySearch(itemRatingIndices[i],
					paraUserIndexArray[paraUserPosition]);

			if (tempCheck < 0) {
				continue;
			}// Of if

			tempDistance = Euclidean(paraPredictItemArrayIndex, i);

			// System.out.println("Distance: " + tempDistance);

			// // Less than the smallest
			if ((tempDistance == 9999)
					|| (tempDistance > tempNeighborDistances[paraK + 1])) {
				continue;
			}// Of if

			// Insert
			for (int j = paraK; j >= 0; j--) {
				if (tempDistance < tempNeighborDistances[j]) {
					tempNeighborDistances[j] = tempNeighborDistances[j - 1];
					tempNeighborIndices[j] = tempNeighborIndices[j - 1];
					tempcheckIndices[j] = tempcheckIndices[j - 1];
					// System.out.println("Move to position " + j);
				} else {
					tempNeighborDistances[j + 1] = tempDistance;
					tempNeighborIndices[j + 1] = i; 
					tempcheckIndices[j + 1] = tempCheck;
					// System.out.println("Insert to position " + (j + 1));
					break;
				}// Of if
			}// Of for j
		}// Of for i
		// Step 3. Predict.
		double tempPrediction = 0;
		double tempSumSimilarity = 0;
		for (int i = 1; i <= paraK; i++) {
			if (tempNeighborIndices[i] != -1) {
				neighborScoreTotal += (itemRatingInformation[tempNeighborIndices[i]][tempcheckIndices[i]] - itemAveRating[tempNeighborIndices[i]])
						* tempNeighborDistances[i];
				tempSumSimilarity += tempNeighborDistances[i];
				// System.out.println("tempSumSimilarity: " +tempSumSimilarity);
			}// of if
		}// of for i

		if (tempSumSimilarity != 0) {
			tempPrediction = itemAveRating[paraPredictItemArrayIndex]
					+ neighborScoreTotal / tempSumSimilarity;
			if (tempPrediction < 1) {
				tempPrediction = 1;
			}
			if (tempPrediction > 5) {
				tempPrediction = 5;
			}
		} else {
			tempPrediction = 2.5;
		}// Of if

		// System.out.println("The predicted value is " + tempPrediction);
		// System.out.println();
		return tempPrediction;

	}// Of predictKNNDistance

	/**
	 ************************* 
	 * Prediction triangle, cosine, jaccard, PIP etc.
	 * 
	 ************************* 
	 */
	private double predictKNN(int paraPredictItemArrayIndex,
			int paraUserPosition, int[] paraUserIndexArray,
			double[] paraRatingArray, int paraK, int paraMeasure) throws Exception {
		// Step 1. Find the neighbors.
		neighborScoreTotal = 0;
		int[] tempNeighborIndices = new int[paraK + 2];
		double[] tempNeighborDistances = new double[paraK + 2];
		int[] tempcheckIndices = new int[paraK + 2];

		InitializeSpaceBigToSmall(tempNeighborIndices, tempNeighborDistances,
				tempcheckIndices);

		double tempDistance = 0;
		// System.out.println("The item  " + paraPredictUserArrayIndex);
		for (int i = 0; i < itemNumber; i++) {
			// Step 1.1 exclude myself

			if (i == paraPredictItemArrayIndex) {
				continue;
			}// Of if
			int tempCheck = Arrays.binarySearch(itemRatingIndices[i],
					paraUserIndexArray[paraUserPosition]);

			if (tempCheck < 0) {
				continue;
			}// Of if
			
			try {
				if (paraMeasure == TMJ) {
					tempDistance = TMJ(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == Jaccard) {
					tempDistance = Jaccard(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == Cosine) {
					tempDistance = Cosine(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == Triangle) {
					tempDistance = Triangle(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == PIP) {
					tempDistance = PIP(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == NHSM) {
					tempDistance = NHSM(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == BC) {
					tempDistance = BC(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == Pearson) {
					tempDistance = pearson(paraPredictItemArrayIndex, i);
				} else if (paraMeasure == CPCC) {
					tempDistance = CPCC(paraPredictItemArrayIndex, i);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
								
//			 System.out.println("Distance: " + tempDistance);

			// // Less than the smallest
			if ((tempDistance == -9999)
					|| (tempDistance < tempNeighborDistances[paraK + 1])) {
				continue;
			}// Of if
				// Insert(�Ӵ�С����)
			for (int j = paraK; j >= 0; j--) {
				if (tempDistance > tempNeighborDistances[j]) {
					tempNeighborDistances[j] = tempNeighborDistances[j - 1];
					tempNeighborIndices[j] = tempNeighborIndices[j - 1];
					tempcheckIndices[j] = tempcheckIndices[j - 1];
					// System.out.println("Move to position " + j);
				} else {
					tempNeighborDistances[j + 1] = tempDistance;
					tempNeighborIndices[j + 1] = i; // ����Ӧ�ļ����кŶ����Ǹ��ж�Ӧ����Ŀ���?
					tempcheckIndices[j + 1] = tempCheck;
					// System.out.println("Insert to position " + (j + 1));
					break;
				}// Of if
			}// Of for j
		}// Of for i
		// Step 3. Predict.
		double tempPrediction = 0;
		double tempSumSimilarity = 0;
		for (int i = 1; i <= paraK; i++) {

			if (tempNeighborIndices[i] != -1) {
				neighborScoreTotal += (itemRatingInformation[tempNeighborIndices[i]][tempcheckIndices[i]] - itemAveRating[tempNeighborIndices[i]])
						* tempNeighborDistances[i];
				tempSumSimilarity += tempNeighborDistances[i];
				// System.out.println("tempSumSimilarity: " +tempSumSimilarity);
			}// of if
		}// of for i

		if (tempSumSimilarity != 0) {

			tempPrediction = itemAveRating[paraPredictItemArrayIndex]
					+ neighborScoreTotal / tempSumSimilarity;
			if (tempPrediction < 1) {
				tempPrediction = 1;
			}
			if (tempPrediction > 5) {
				tempPrediction = 5;
			}
		} else {
			tempPrediction = 2.5;
		}// Of if

		// System.out.println("The predicted value is " + tempPrediction);
		// System.out.println();
		return tempPrediction;
	}// of predictKNN

	/**
	 ************************* 
	 * Compute the Manhattan distance between two rows. 0s are not considered.
	 * 
	 * @author Fan Min
	 ************************* 
	 */
	public double manhattanDistance(int paraRow1, int paraRow2) {
		double tempDistance = 0;
		double tempCount = 0;
		for (int i = 0; i < itemRatingInformation[paraRow1].length; i++) {
			for (int j = 0; j < itemRatingInformation[paraRow2].length; j++) {
				if (itemRatingIndices[paraRow1][i] == itemRatingIndices[paraRow2][j]) {
					tempCount++;
					tempDistance += Math.abs(itemRatingInformation[paraRow1][i]
							- itemRatingInformation[paraRow2][j]);
				}// Of if
			}// Of for j
		}// Of for i

		if (tempCount == 0) {
			return 0;
		}// Of if
		return tempDistance / tempCount;
	}// Of manhattanDistance

	/**
	 ************************* 
	 * Compute the Euclidean distance.
	 * 
	 ************************* 
	 */
	public double Euclidean(int paraRow1, int paraRow2) throws Exception {
		double tempE = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {

				tempE += (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j])
						* (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j]);
				i++;
				j++;
			}// Of if
		}// Of while
			// System.out.println("test1.3");

		tempE = Math.sqrt(tempE);
		// System.out.println("tempE: "+tempE);

		return tempE;
	}// Of Euclidean

	/**
	 ************************* 
	 * Compute the TMJ similarity.
	 * 
	 ************************* 
	 */
	public double TMJ(int paraRow1, int paraRow2) throws Exception {
		double tempDistance = 0;
		double tempJ = 0;
		double tempT = 0;
		double tempParaRow1 = 0;
		double tempParaRow2 = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {
				tempParaRow1 += itemRatingInformation[paraRow1][i]
						* itemRatingInformation[paraRow1][i];
				tempParaRow2 += itemRatingInformation[paraRow2][j]
						* itemRatingInformation[paraRow2][j];
				tempT += (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j])
						* (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j]);
				tempJ++;

				// System.out.print(itemRatingIndices[paraRow1][i] +",");
				i++;
				j++;
			}// Of if
		}// Of while
		tempJ = tempJ
				/ (itemRatingIndices[paraRow1].length
						+ itemRatingIndices[paraRow2].length - tempJ - 1);
		if (tempParaRow1 == 0 || tempParaRow2 == 0) {
			tempT = 0;
		} else {
			tempT = 1 - Math.sqrt(tempT)
					/ (Math.sqrt(tempParaRow1) + Math.sqrt(tempParaRow2));
		}
		tempDistance = tempJ * tempT;
		return tempDistance;
	}// Of TMJ

	/**
	 ************************* 
	 * Compute the Jaccard similarity.
	 * 
	 ************************* 
	 */
	public double Jaccard(int paraRow1, int paraRow2) throws Exception {
		double tempJ = 0;

		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {

				tempJ++;

				// System.out.print(itemRatingIndices[paraRow1][i] +",");
				i++;
				j++;
			}// Of if
		}// Of while
			// System.out.println("test1.3");
		tempJ = tempJ
				/ (itemRatingIndices[paraRow1].length
						+ itemRatingIndices[paraRow2].length - tempJ - 1);
		return tempJ;
	}// Of Jaccard

	/**
	 ************************* 
	 * Compute the cosine similarity.
	 * 
	 ************************* 
	 */
	public double Cosine(int paraRow1, int paraRow2) throws Exception {
		double tempC = 0;
		double tempParaRow1 = 0;
		double tempParaRow2 = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {
				tempParaRow1 += itemRatingInformation[paraRow1][i]
						* itemRatingInformation[paraRow1][i];
				tempParaRow2 += itemRatingInformation[paraRow2][j]
						* itemRatingInformation[paraRow2][j];
				tempC += itemRatingInformation[paraRow1][i]
						* itemRatingInformation[paraRow2][j];

				// System.out.print(itemRatingIndices[paraRow1][i] +",");
				i++;
				j++;
			}// Of if
		}// Of while
			// System.out.println("test1.3");
		if (tempParaRow1 == 0 || tempParaRow2 == 0) {
			tempC = 0;
		} else {
			tempC = tempC / (Math.sqrt(tempParaRow1) * Math.sqrt(tempParaRow2));
		}
		return tempC;
	}// Of cosine

	/**
	 ************************* 
	 * Compute the Triangle similarity.
	 * 
	 ************************* 
	 */
	public double Triangle(int paraRow1, int paraRow2) throws Exception {
		double tempT = 0;
		double tempParaRow1 = 0;
		double tempParaRow2 = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {
				tempParaRow1 += itemRatingInformation[paraRow1][i]
						* itemRatingInformation[paraRow1][i];
				tempParaRow2 += itemRatingInformation[paraRow2][j]
						* itemRatingInformation[paraRow2][j];

				tempT += (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j])
						* (itemRatingInformation[paraRow1][i] - itemRatingInformation[paraRow2][j]);

				i++;
				j++;

			}// Of if
		}// Of while
			// System.out.println("test1.3");

		if (tempParaRow1 == 0 || tempParaRow2 == 0) {
			tempT = 0;
		} else {
			tempT = 1 - Math.sqrt(tempT)
					/ (Math.sqrt(tempParaRow1) + Math.sqrt(tempParaRow2));
		}
		return tempT;
	}// Of Triangle

	/**
	 ************************* 
	 * Compute the PIP similarity.
	 * 
	 ************************* 
	 */
	public double PIP(int paraRow1, int paraRow2) throws Exception {
		double tempEachPiP = 0;
		double tempPiP = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {
				boolean agreeMent = true;
				if ((itemRatingInformation[paraRow1][i] > 3 && itemRatingInformation[paraRow2][j] < 3)
						|| (itemRatingInformation[paraRow1][i] < 3 && itemRatingInformation[paraRow2][j] > 3)) {
					agreeMent = false;
				}
				double tempProximity = 0;
				double absoluteDiatance = 0;
				if (agreeMent) {
					absoluteDiatance = Math
							.abs(itemRatingInformation[paraRow1][i]
									- itemRatingInformation[paraRow2][j]);
				} else {
					absoluteDiatance = 2 * Math
							.abs(itemRatingInformation[paraRow1][i]
									- itemRatingInformation[paraRow2][j]);
				}
				tempProximity = (9 - absoluteDiatance) * (9 - absoluteDiatance);
				// System.out.println("tempProximity: "+tempProximity);
				double tempImpact = 0;
				if (agreeMent) {
					tempImpact = (Math
							.abs(itemRatingInformation[paraRow1][i] - 3) + 1)
							* (Math.abs(itemRatingInformation[paraRow2][j] - 3) + 1);
				} else {
					tempImpact = 1 / ((Math
							.abs(itemRatingInformation[paraRow1][i] - 3) + 1) * (Math
							.abs(itemRatingInformation[paraRow2][j] - 3) + 1));
				}
				// System.out.println("tempImpact: "+tempImpact);
				double tempPopularity = 0;
				if ((itemRatingInformation[paraRow1][i] > userAveRating[itemRatingIndices[paraRow1][i]] && itemRatingInformation[paraRow2][j] > userAveRating[itemRatingIndices[paraRow1][i]])
						|| (itemRatingInformation[paraRow1][i] < userAveRating[itemRatingIndices[paraRow1][i]] && itemRatingInformation[paraRow2][j] < userAveRating[itemRatingIndices[paraRow1][i]])) {
					double tempPP = (itemRatingInformation[paraRow1][i] + itemRatingInformation[paraRow2][j])
							/ 2 - userAveRating[itemRatingIndices[paraRow1][i]];
					tempPopularity = 1 + tempPP * tempPP;
				} else {
					tempPopularity = 1;
				}
				// System.out.println("tempPopularity: "+tempPopularity);
				tempEachPiP = tempProximity * tempImpact * tempPopularity;
				tempPiP += tempEachPiP;

				i++;

				j++;

			}// Of if
		}// Of while
			// System.out.println("tempPiP: "+tempPiP);

		return tempPiP;
	}// Of PIP

	/**
	 ************************* 
	 * Compute the NHSM similarity
	 * 
	 ************************* 
	 */
	public double NHSM(int paraRow1, int paraRow2) throws Exception {

		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");
		double tempJ = 0;
		double tempPSS = 0;
		double tempURP = 0;
		double tempChangedeviation = 0;

		for (int i = 0; i < itemRatingInformation[paraRow1].length; i++) {
			if (itemRatingInformation[paraRow1][i] > 0) {
				tempChangedeviation += (itemRatingInformation[paraRow1][i] - itemAveRating[paraRow1])
						* (itemRatingInformation[paraRow1][i] - itemAveRating[paraRow1]);
				// System.out.println("ratingMatrix[paraRow][i]"+itemRatingInformation[paraRow1][i]);
				// System.out.println("averageScoreArray[paraRow]"+itemAveRating[paraRow1]);
			}
		}// of if i
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {
				tempJ++;
				double tempEachPSS = 0;
				double Proximity = 0;
				Proximity = 1 - 1 / (1 + Math.exp(-Math
						.abs(itemRatingInformation[paraRow1][i]
								- itemRatingInformation[paraRow2][j])));
				// System.out.println("test tempProximity");
				// Impact
				double Significance = 0;
				Significance = 1 / (1 + Math.exp(-Math
						.abs(itemRatingInformation[paraRow1][i] - 3)
						* Math.abs(itemRatingInformation[paraRow2][j] - 3)));
				// System.out.println("test tempImpact");
				// Popularity
				double Singularity = 0;
				Singularity = 1 - 1 / (1 + Math
						.exp(-Math
								.abs((itemRatingInformation[paraRow1][i] + itemRatingInformation[paraRow2][j])
										/ 2
										- userAveRating[itemRatingIndices[paraRow1][i]])));
				// System.out.println("test tempPopularity");
				tempEachPSS = Proximity * Significance * Singularity;
				tempPSS += tempEachPSS;

				i++;

				j++;

			}// Of if
		}// Of while
		tempJ = tempJ
				/ (itemRatingIndices[paraRow1].length
						+ itemRatingIndices[paraRow2].length - tempJ - 1);
		
		double tempStandardDeviationPredition = Math.sqrt(tempChangedeviation
				/ (itemRatingInformation[paraRow1].length - 1));
		double tempStandardDeviationParaRow2 = Math
				.sqrt(standarddeviation[paraRow2]
						/ itemRatingInformation[paraRow2].length);
		tempURP = 1 - 1 / (1 + Math.exp(-Math.abs(itemAveRating[paraRow1]
				- itemAveRating[paraRow2])
				* Math.abs(tempStandardDeviationPredition
						- tempStandardDeviationParaRow2)));

		double tempDistance = tempPSS * tempJ * tempURP;

		return tempDistance;
	}// Of NHSM

	/**
	 ************************* 
	 * Compute the BC similarity.
	 * 
	 ************************* 
	 */
	public double BC(int paraRow1, int paraRow2) throws Exception {

		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if
			// System.out.println("test1.1");

		everyItemClassfication = new double[2][6];
		// System.out.println("R1: "+paraRow1 +", R2: "+paraRow2);
		for (int j = 0; j < itemRatingInformation[paraRow1].length; j++) {
			if (itemRatingInformation[paraRow1][j] == 0) {
				continue;
			}

			// System.out.println(itemRatingInformation[paraRow1][j]);
			everyItemClassfication[0][(int) (itemRatingInformation[paraRow1][j] - 1)]++;
			everyItemClassfication[0][5]++;
		}// of for if

		for (int j = 0; j < itemRatingInformation[paraRow2].length; j++) {
			if (itemRatingInformation[paraRow2][j] == 0) {
				continue;
			}

			// System.out.println(itemRatingInformation[paraRow2][j]);
			everyItemClassfication[1][(int) (itemRatingInformation[paraRow2][j] - 1)]++;
			everyItemClassfication[1][5]++;
		}// of for j

		double BSum = 0;

		for (int x = 0; x < 5; x++) {
			BSum += Math
					.sqrt((everyItemClassfication[0][x] / everyItemClassfication[0][5])
							* (everyItemClassfication[1][x] / everyItemClassfication[1][5]));
		}// of if X

		return BSum;
	}// Of BC

	/**
	 *************************  
	 * Compute the PCC similarity.
	 * 
	 *************************
	 */
	public double pearson(int paraRow1, int paraRow2) throws Exception {
		double tempDistance = 0;
		double tempFirstUserRating = 0;
		double tempSecondUserRating = 0;
		double tempXYSum = 0;
		double tempXSum = 0;
		double tempYSum = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if

		// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {

				tempFirstUserRating = itemRatingInformation[paraRow1][i]
						- itemAveRating[paraRow1];
				tempSecondUserRating = itemRatingInformation[paraRow2][j]
						- itemAveRating[paraRow2];
				tempXYSum += tempFirstUserRating * tempSecondUserRating;
				tempXSum += tempFirstUserRating * tempFirstUserRating;
				tempYSum += tempSecondUserRating * tempSecondUserRating;
				i++;
				j++;

			}// Of if
		}// Of while
		if (tempXSum==0||tempYSum==0) {
			return -1;
		}
			// System.out.println("test1.3");
		tempDistance = tempXYSum / (Math.sqrt(tempXSum) * Math.sqrt(tempYSum));

		tempDistance = Math.abs(tempDistance);
		// System.out.println(tempDistance);
		return tempDistance;
	}// Of CPCC

	/**
	 *************************  
	 * Compute the CPCC similarity.
	 * 
	 ************************* 
	 */
	public double CPCC(int paraRow1, int paraRow2) throws Exception {
		double tempDistance = 0;
		double tempFirstUserRating = 0;
		double tempSecondUserRating = 0;
		double tempXYSum = 0;
		double tempXSum = 0;
		double tempYSum = 0;
		if (itemRatingIndices[paraRow1].length == 0
				|| itemRatingIndices[paraRow2].length == 0) {

			return 0.0;
		}// Of if

		if ((itemRatingIndices[paraRow1][0] > itemRatingIndices[paraRow2][itemRatingIndices[paraRow2].length - 1])
				|| (itemRatingIndices[paraRow2][0] > itemRatingIndices[paraRow1][itemRatingIndices[paraRow1].length - 1])) {

			return 0.0;
		}// Of if

		// System.out.println("test1.1");
		int i = 0, j = 0;
		while (i < itemRatingIndices[paraRow1].length
				&& j < itemRatingIndices[paraRow2].length) {
			if (itemRatingIndices[paraRow1][i] < itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow1][i] == 0) {

				i++;
			} else if (itemRatingIndices[paraRow1][i] > itemRatingIndices[paraRow2][j]
					|| itemRatingInformation[paraRow2][j] == 0) {

				j++;
			} else {

				tempFirstUserRating = itemRatingInformation[paraRow1][i] - 3;
				tempSecondUserRating = itemRatingInformation[paraRow2][j] - 3;
				tempXYSum += tempFirstUserRating * tempSecondUserRating;
				tempXSum += tempFirstUserRating * tempFirstUserRating;
				tempYSum += tempSecondUserRating * tempSecondUserRating;
				i++;
				j++;

			}// Of if
		}// Of while
			// System.out.println("test1.3");
		if (tempXSum==0||tempYSum==0) {
			return -1;
		}
		tempDistance = tempXYSum / (Math.sqrt(tempXSum) * Math.sqrt(tempYSum));

		tempDistance = Math.abs(tempDistance);
		return tempDistance;
	}// Of cosineDistance


	/**
	 ************************* 
	 * Compute the MAE based on the deviation of each leave-one-out.
	 * 
	 * @author Fan Min
	 ************************* 
	 */
	public double[] computeMAE(int paraK, int paraMeasure) throws Exception {
//		 System.out.println("computeMAE test 1");

		double[] tempResultArray = new double[2];
		double tempTotalDeviationMAE = 0;
		double tempTotalDeviationRMSE = 0;
		int tempTestingObjectCount = 0;
		runSteps = 0;
		for (int i = 0; i < itemRatingInformation.length; i++) {
//			 System.out.println("item i: " + i);
			double[] tempResult = computeOneDeviationKNNMAE(i,
					itemRatingIndices[i], itemRatingInformation[i], paraK,paraMeasure);
			runSteps++;
			tempTotalDeviationMAE += tempResult[0];
			runSteps++;
			tempTotalDeviationRMSE += tempResult[1];
			runSteps++;
			tempTestingObjectCount += itemRatingInformation[i].length;
			runSteps++;
		}// Of for i
			// System.out.println("tempTotalDeviation="+tempTotalDeviationRMSE+"tempTestingObjectCount: "
			// + tempTestingObjectCount);
		tempResultArray[0] = tempTotalDeviationMAE / tempTestingObjectCount;
		runSteps++;
		tempResultArray[1] = Math.sqrt(tempTotalDeviationRMSE
				/ tempTestingObjectCount);
		runSteps++;
		return tempResultArray;
	}// Of computeMAE

	public static void main(String[] args) {
		try {
			M1MTMJ tempRecommender = new M1MTMJ(6040, 4000); //
			for (int tempK = 5; tempK < 105; tempK += 5) {
				System.out.print("tempK: " + tempK);
				experimentKNNMAE(tempRecommender, tempK, Pearson );
			}// of for tempK

		} catch (Exception ee) {
			System.out.println(ee);
		}// of try
	}// of main

	/**
	 ************************* 
	 * The MAE experiment.
	 * 
	 * @author Heng-Ru Zhang
	 ************************* 
	 */
	public static void experimentKNNMAE(M1MTMJ paraRecommender, int parak, int paraMeasure) {
		double[] tempResultArray = new double[2];
		try {

			// for (int tempK =10; tempK <11; tempK +=1) {
			long tempStart = System.currentTimeMillis();

			tempResultArray = paraRecommender.computeMAE(parak,paraMeasure); // tempK

			long tempEnd = System.currentTimeMillis();
//			System.out.println("The time of the program is: "
//					+ (tempEnd - tempStart) + "; The runSteps=" + runSteps);
			System.out.println(" The MAE: " + tempResultArray[0] + " The RMSE: "
					+ tempResultArray[1]);
			// }// Of for tempK
		} catch (Exception ee) {
			System.out.println(ee);
		}// Of try
	}// of experimentKNNMAE

}// of ML1mJCT

