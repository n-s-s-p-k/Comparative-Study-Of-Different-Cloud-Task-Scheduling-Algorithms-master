package GWO;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;
import utils.Constants;

import java.util.List;

public class DCB_maker extends DatacenterBroker {

	 /**
	 * @param name
	 * @throws Exception
	 */
	public DCB_maker(String name) throws Exception {
	        super(name);
	    }


	
	//calculation of etc
	/**
	 * @param clist
	 * @param vlist
	 */
	public static void ETC(List<Cloudlet> clist, List<Vm> vlist) {
        double[][] etc = new double[Constants.NO_OF_TASKS][Constants.NO_OF_VMS];
        
        
        for(int i=0; i<Constants.NO_OF_TASKS;i++) {
        	for(int j=0;j<Constants.NO_OF_VMS;j++ ) {
        		etc[i][j]= clist.get(i).getCloudletLength()/vlist.get(j).getMips();
        	}
        }
        
        Log.printLine();
		Log.printLine("========== etc values ==========");
		String indent = "    ";
		for(int i=0; i<Constants.NO_OF_TASKS;i++) {
			Log.print(clist.get(i).getCloudletId() + indent);
			for(int j=0;j<Constants.NO_OF_VMS;j++ ) {
				Log.print( etc[i][j] + indent);
			}
			Log.printLine();
		}
		
//establishing the MS objective, the exe-
//cution time (ET) of all VMs in the pool should be calculated.
        double ExecutionTime[] = new double[Constants.NO_OF_VMS];
        double sum =0 ;
        for(int j=0;j<Constants.NO_OF_VMS;j++ ) {
        	for(int i=0; i<Constants.NO_OF_TASKS;i++) {
        		ExecutionTime[j] += etc[i][j];

        	}
        }
        Log.printLine();
		Log.printLine("========== Execution Time values ==========");
        for(int j=0;j<Constants.NO_OF_VMS;j++ )
        	Log.printLine( vlist.get(j).getId()+ indent 
				+ ExecutionTime[j]);
        
    	//if cloudlet allocated to vm then 1 else 0
    	double RealExecutionTime[] = new double[Constants.NO_OF_VMS];
        
        for(int j=0;j<Constants.NO_OF_VMS;j++ ) {
        	for(int i=0; i<Constants.NO_OF_TASKS;i++) {
        		int x=0;
        		if(clist.get(i).getVmId() == vlist.get(j).getId())
        			x=1;
        		RealExecutionTime[j] += etc[i][j]*x;
        		
        	}
        }
        
        Log.printLine();
		Log.printLine("========== Real Execution Time values ==========");
        for(int j=0;j<Constants.NO_OF_VMS;j++ )
        	Log.printLine( vlist.get(j).getId()+ indent 
				+ RealExecutionTime[j]);
        
        //MS objective is the maximum of ET for all VMs
        double makespan = 0,tsum=0;
        
        double tmax=0,tmin=RealExecutionTime[0];
        for(int j=0;j<Constants.NO_OF_VMS;j++) {
        	if (tmax < RealExecutionTime[j]) {
        		tmax = RealExecutionTime[j];
        	}
        	if(tmin > RealExecutionTime[j])
        		tmin = RealExecutionTime[j];
        	tsum += RealExecutionTime[j];
        }
        double tavg =tsum/Constants.NO_OF_VMS;
        double degreeofImbalance = (tmax - tmin)/tavg;
        makespan = tmax;
        Log.printLine(" Makespan estimated : "+ makespan);
        Log.printLine(" degreeofImbalance estimated : "+ degreeofImbalance);
	
	
	//Objective I = Minimize(Makespan)
        
//        RESOURCE UTILIZATION
//        Avg_RUR = (Avg Makespan / Cloud Makespan)
        double avg_makespan=tavg;
        double avg_resouceutliRatio = avg_makespan / makespan;
        Log.printLine("Resource Utilisation ratio : "+ avg_resouceutliRatio);
//	Objective II = Maximize (Avg_RUR)
        
//  Objective III = Minimize (Imbalance_Deg
        
        //Throughput = (Number of tasks) / (Makespan)
        double throughput = Constants.NO_OF_TASKS/makespan;
        Log.printLine("Throughput : "+ throughput);
        //Objective IV = Maximize (Throughput)
        
        //execution cost of execution task on a
//        specific VM, this cost relies on the length of the
//        task (TaskSize), the cost of transfer task to the
//        specific VM and the storage of that VM
        double cost[] = new double[Constants.NO_OF_TASKS]; 
        for(int j=0;j<Constants.NO_OF_VMS;j++ ) {
        	for(int i=0; i<Constants.NO_OF_TASKS;i++) {
        		if(clist.get(i).getVmId() == vlist.get(j).getId()) {
        			cost[i] = clist.get(i).getCloudletLength()/
        					vlist.get(j).getBw();
        		}
        		
        	}
        }
        
//        Cost minimum
        double minCost = cost[0];
        for(int i=0; i<Constants.NO_OF_TASKS;i++) {
        	if(minCost > cost[i]) {
        		minCost = cost[i];
        	}
        }
        Log.printLine("Minimum Cost for the task = " + minCost);




	}
	private int[] mapping;

	public void setMapping(int[] mapping) {
		this.mapping = mapping;
	}

	//Mapping the vm to particular task by gwo
	private List<Cloudlet> assignCloudletsToVms(List<Cloudlet> cloudlist) {
		int idx = 0;
		for (Cloudlet cl : cloudlist) {
			cl.setVmId(mapping[idx++]);
		}
		return cloudlist;
	}



	/**
	 * @param clist
	 * @param vlist
	 * @return mapping
	 */
	//TODO : use the gwo
	// should be double [] instead of void and return array
	public int[] algo(List<Cloudlet> clist, List<Vm> vlist) {
//TODO : map it to the vms and do it iteratively in driver for optimized value improvement
		//Grey Wolf Optimizer
		int dimension = Constants.NO_OF_TASKS;
		int upper = 100;
		int lower = -100;
		SimpleAlgo GO = new SimpleAlgo(Constants.MaxIter, upper, lower,
				Constants.Population, dimension);
		double[][][] best = GO.solution(clist,vlist);
		System.out.println("Optimized value = " + best[0][0][0]);
		for(int i = 0; i < Constants.NO_OF_VMS; i++) {
			for(int j=0;j<Constants.NO_OF_TASKS;j++)
			{
				if(best[1][i][j] != 0) {
					System.out.println("x["+i+"]["+j+"] = " + best[1][i][j]);
				}
			}
		}
//		System.out.println("=======================================");
//		System.out.println("Finding the sorted and best vm for task");
//		for(int j=0;j<Constants.NO_OF_TASKS;j++) {
//			for(int i = 0; i < Constants.NO_OF_VMS; i++)
//			{
//				System.out.println("x["+i+"]["+j+"] = " + best[1][i][j]);
//			}
//		}

		// for mapping getting minimum values out of it
		double min_mkspan[] = new double[Constants.NO_OF_TASKS];
		int[] vm_min_makespan = new int[Constants.NO_OF_TASKS];
//		System.out.println("=======================================");
//		System.out.println("Finding the sorted and best vm for task");
		for(int j=0;j<Constants.NO_OF_TASKS;j++) {
			min_mkspan[j] = best[1][0][j];
			for(int i = 0; i < Constants.NO_OF_VMS; i++)
			{
				if(min_mkspan[j]>best[1][i][j]) {
					min_mkspan[j] = best[1][i][j];
					vm_min_makespan[j] = i;
				}
			}
		}

//		for(int j=0;j<Constants.NO_OF_TASKS;j++) {
//			System.out.println("x["+vm_min_makespan[j]+"]["+j+"] = " + min_mkspan[j]);
//		}
	//TODO:Sorting the makespan and getting the order of tasks based on makespan
		int SortedMsTsakList[] = new int[Constants.NO_OF_TASKS];
		double minx = min_mkspan[0];
		int size = min_mkspan.length;

//		for (int i = size/2; i >0; i/=2) {
//			for(int j=i; j < size;j++ ){
//				double xx = min_mkspan[j];
//				int k;
//				for( k =i;k>=i&&min_mkspan[k-i]>xx;k-=i){
//					min_mkspan[k]=min_mkspan[k-i];
//					SortedMsTsakList[k]=k-i;
//				}
//				min_mkspan[k] =xx;
//			}
//		}
//printing in ascending order of makespan tasklist
//		for(int j=0;j<Constants.NO_OF_TASKS;j++) {
//			System.out.println("x["+vm_min_makespan[j]+"]["+SortedMsTsakList[j]+"] = " + min_mkspan[j]+" "+ j);
//		}

		//printing the mapping
//		System.out.println("=======================================");
//		System.out.println("Mapping");
//		for(int j=0;j<Constants.NO_OF_TASKS;j++) {
//				System.out.println("x["+vm_min_makespan[j]+"]["+j+"] = " + best[1][vm_min_makespan[j]][j]);
//		}
		return vm_min_makespan;
	}


	@Override
	protected void submitCloudlets(){
		List<Cloudlet> tasks = assignCloudletsToVms(getCloudletList());
		int vmIndex = 0;
		for (Cloudlet cloudlet : tasks) {
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}
	}
	
}
