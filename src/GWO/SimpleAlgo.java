
package GWO;

/*Input:
		Requirement of PCGWO algorithm
		Specification of Task Scheduling & Resource allocation.
		Tasks T1, T2,.…….TN and Resources R1, R2,…….RM and Maxitr
		Output: Set of tasks mapped to VMs
		1. Procedure: PCGWO
		2. {
//Parameters Initialization
		Initialize the number of task , number of resources , Set the
		3.
		initial values of the cluster size n, parameter a, coefficient
		vectors M, N and the maximum number of iterations Max itr
		4. Set t = 0 {counter initialization}.
// Population initialization
		5. i = 1
		6. while(i ≤ n) do
		7. {
		a. Bring about an initial population XWi(t)
		randomly.
		b. Appraise the fitness evaluation function of each
		hunt wolf (solution) FF(x)
		8. } end while
//Assign the best three solutions
		9. Accredit the values of the first, second and third near
		optimum solution Xα, Xβ and Xδ correspondingly.
		10. reiteration
		11. i = 1
		12. while(i ≤ n) do
		13. { Update each hunt agent in the location as shown in
		equation 14
		a. Cutback the parameter ‘a’ from 2 to 0.
		b. Update the coefficient M & N as shown in
		equation 15, 16 correspondingly
		c. Appraise the fitness function of each hunt agent
		FF(x)
		14. } end While
		15. update the vectors Xα, Xβ and Xδ
		16. Set t=t+1 (iteration counter increasing)
		17. //Termination criteria
		18. until (t<Maxitr). (Termination criteria satisfied)
		19. //Best solution
		20. Produce the optimum solution Xα
		21. } //end- procedure*/

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import utils.Constants;
import utils.GenerateLengthMatrix;

import java.util.List;

public class SimpleAlgo {
	static int N; //Population
	static int D; //Dimension
	static int nvm = Constants.NO_OF_VMS;
	static int ntasks = Constants.NO_OF_TASKS;
	static int maxiter; // İteration
	static double a; // between 0 - 2 by iteration number
	static double r1; //random number
	static double r2; //random number
	static double alfa[][]; // Best Position
	static double beta[][]; // Second Position
	static double delta[][]; // Third Position
	static double X1, X2, X3;
	static double A1, C1; //Alfa update value
	static double A2, C2; //Beta update value
	static double A3, C3; // delta uptade value
	static double Lower; // Min Value
	static double Upper; // Max Value
	static double positions[][][]; // Population Position
	static double BestVal[];
	static double fitness, alfaScore, betaScore, deltaScore  = Double.MAX_VALUE;

	static boolean flag = false;

	public SimpleAlgo(int iter, int UpLevel, int LowLevel, int searchAgent, int dimension) {
		
		maxiter = iter;
		Lower = LowLevel;
		Upper = UpLevel;
		N = searchAgent;
		D = dimension;
		fitness = Double.MAX_VALUE;
		alfaScore = Double.MAX_VALUE;
		betaScore = Double.MAX_VALUE;
		deltaScore = Double.MAX_VALUE;
		positions = new double[N][Constants.NO_OF_VMS][Constants.NO_OF_TASKS];
		alfa =  new double[Constants.NO_OF_VMS][Constants.NO_OF_TASKS];
		beta =  new double[Constants.NO_OF_VMS][Constants.NO_OF_TASKS];
		delta = new double[Constants.NO_OF_VMS][Constants.NO_OF_TASKS];
		BestVal = new double[maxiter];

		//initializing commuication and execution time matrix
		commMatrix = GenerateLengthMatrix.getlengthMatrix();
//		execMatrix = GenerateMatrices.getExecMatrix();
		
	}
	
	//from this d= no.of vms
	//wolves roam on vm by taking a prey
	//bestvalu = size(maxiter) not vm or tasks or populstion
	
	
	//Bechmark Function
	// TODO : get the values of makespan and cost 
	private static double[][] execMatrix, commMatrix;
	static double bechmark(double[][] position) {
		double result = 0;
		double makespan = 0;
		double[] dcWorkingTime = new double[nvm];
		for (int i = 0; i < ntasks; i++) {
				 for (int j =0;j<nvm;j++) {
//				int dcId = (int) position[i][j];
//				if (dcWorkingTime[j] != 0) --dcWorkingTime[j];
				//TODO : communication time matrix
				dcWorkingTime[j] += position[j][i];
//				dcWorkingTime[j] += commMatrix[i][j];
				makespan = Math.max(makespan, dcWorkingTime[j]);
			}
		}


		if(flag) {
//			Log.printLine("Positoin of particular wolf :");
			for (int j = 0; j < nvm; j++) {
				for (int i = 0; i < ntasks; i++) {
					Log.print(position[j][i] + " ");
				}
				Log.printLine();
			}
		}
		flag = false;



//		for(int i = 0; i < nvm; i++) {
//			for(int j=0;j<ntasks;j++) {
//			result += Math.pow(position[i][j], 2);
//			}
//		}
//		makespan = result;

		return makespan;
	}
	
	// update best solution
	static void sort_and_index(double[][][] position) {
		double score;
		for(int i = 0; i < N; i++) {
//			if(i==0) flag = true;
			score = bechmark(position[i]);
			if(score < alfaScore) {
				alfaScore = score;
				for(int j = 0; j < nvm; j++) {
					for(int k =0;k<ntasks;k++)
					alfa[j][k] = position[i][j][k];
				}
			}
			if(score > alfaScore && score < betaScore) {
				betaScore = score;
				for(int j = 0; j < nvm; j++) {
					for(int k =0;k<ntasks;k++)
						beta[j][k] = position[i][j][k];
				}
			}
			if(score > alfaScore && score > betaScore && score < deltaScore) {
				deltaScore = score;
				for(int j = 0; j < nvm; j++) {
					for(int k =0;k<ntasks;k++)
						delta[j][k] = position[i][j][k];
				}
			}
		}
	}		
	//first iterition and initialization
	// getting expected time to compute
	static void init(List<Cloudlet> cloudletList,List<Vm> vmList) {
		for(int i =0; i < N; i++) {
//				positions[i][j] = Lower + (Upper - Lower) * Math.random();
			for(int jj=0;jj<Constants.NO_OF_VMS;jj++){
				for (int ii=0;ii<Constants.NO_OF_TASKS;ii++) {
//						  if(cloudletList.get(ii).getVmId() == vmList.get(jj).getId()) {
							  positions[i][jj][ii] = cloudletList.get(ii).getCloudletLength()/
									  vmList.get(jj).getMips();
							  
//						  }
					  }
				}
		}
		// Position Sort
		sort_and_index(positions);
//		Log.printLine("Sorted positions");
//		for (int i =0 ;i<N ;i++){
//			for (int j =0 ; j< nvm;j++){
//				for(int k =0;k<ntasks;k++){
//					Log.print(positions[i][j][k]+ "  ");
//				}
//				Log.printLine();
//			}
//			Log.printLine();
//		}
		BestVal[0] = bechmark(alfa);

	}
	
	
	//update population positions
	 public double[][][] solution(List<Cloudlet> cloudletList,List<Vm> vmList){
		init(cloudletList,vmList);
		//int iter = 1;
		for(int iter = 1; iter < maxiter ; iter++) {
			a = 2.0 - ((double)iter * (2.0 / (double) maxiter));
			for( int i = 0; i < N; i++) {
				for(int j = 0; j < nvm; j++) {
					for(int k=0;k<ntasks;k++) {
						//Update Values for Alfa
						r1 = Math.random();
						r2 = Math.random();
						A1 = (2.0 * a * r1) - a;
						C1 = 2.0 * r2;
						
						//Update position by Alfa
						X1 = alfa[j][k] - A1 * (Math.abs(C1 * alfa[j][k] - positions[i][j][k]));

						//Update VAlues for beta
						r1 = Math.random();
						r2 = Math.random();
						A2 = (2.0 * a * r1) - a;
						C2 = 2.0 * r2;
						
						//Update position by Beta
						X2 = beta[j][k] - A2 * (Math.abs(C2 * beta[j][k] - positions[i][j][k]));
						
						//Update VAlues for Delta
						r1 = Math.random();
						r2 = Math.random();
						A3 = (2.0 * a * r1) - a;
						C3 = 2.0 * r2;
						
						//Update position by Delta
						X3 = delta[j][k] - A3 * (Math.abs(C3 * delta[j][k] - positions[i][j][k]));
						
						//update Population Positions
						positions[i][j][k] = (X1 + X2 + X3) / 3.0;
						positions[i][j][k] = simplebounds(positions[i][j][k]);
					}
				}
			}
			sort_and_index(positions);

			BestVal[iter] = bechmark(alfa);
		}
		double[][][] out = new double[2][nvm][ntasks];
		double test_sum =0;
		double test_makesapn = 0;
		for(int i = 0; i < nvm; i++) {
			for(int j=0;j<ntasks;j++) {
			out[1][i][j] = alfa[i][j];
			test_sum += alfa[i][j];
			test_makesapn += Math.abs(alfa[i][j]);
			}
		}
		out[0][0][0] = bechmark(alfa);
		Log.printLine("Test makespan : "+ test_sum);
		Log.printLine("Test makespan : "+ test_makesapn);
		return out;
	}
	
	static double simplebounds(double s){
		if(s < Lower) {
			s = Lower;
		}
		if(s > Upper) {
			s = Upper;
		}
		return s;
	}
}
