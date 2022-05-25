package GWO;


import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import utils.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;

import static utils.Commons.lengthMatrix;


public class DriverClass {

	//lists for vms and cloudlets
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmlist;	
	
	//for mapping the cloudlets to vm based on optimal value
	private static int mapping[];
	private static double[][] commMatrix;
	private static double[][] execMatrix;
	private static Datacenter[] datacenter;
	/**
	 * @param userId
	 * @param vms
	 * @return vm list
	 */
	private static List<Vm> createVM(int userId, int vms) {

		//Creates a container to store VMs. This list is passed to the broker later
		LinkedList<Vm> list = new LinkedList<Vm>();

		//create VMs
		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			vm[i] = new Vm(i, userId, 
					Constants.VM_MIPS, 
					Constants.VM_PES, 
					Constants.VM_RAM,
                    Constants.VM_BANDWIDTH, 
                    Constants.VM_IMAGE_SIZE,
                    Constants.VMM_NAME, 
                    new CloudletSchedulerSpaceShared());
			//for creating a VM with a space shared scheduling policy for cloudlets:
			//vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority, vmm, new CloudletSchedulerSpaceShared());

			list.add(vm[i]);
		}

		return list;
	}

	
	/**
	 * @param tcount
	 * @return random seed for thet clooudlet length
	 */
	private static ArrayList<Integer> getSeedValue(int tcount){
		ArrayList<Integer> seed =new ArrayList<Integer> ();
		try {
//			File fobj = new File("RandomSeed");
			//randomseed file has randomly generated values bw 1000 to 3000
			//its a list for 400 values no.of tasks <=400
			File fobj = new File(System.getProperty("user.dir") + "/RandomSeed");
			Scanner sc = new Scanner(fobj);
			
			while(tcount > 0 && sc.hasNextLine()) {
				seed.add(sc.nextInt());
				tcount--;
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return seed;
	}

    /**
     * @param userId
     * @param cloudlets
     * @return cloudlet list
     */
	private static List<Cloudlet> createCloudlet(int userId, int cloudlets,int choice){
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		//cloudlet parameters
		// in utils.Constants
		
		UtilizationModel umf = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		//creating random length tasks and reading from a file
		//for the experientation
		//TODO :RANDOMlength file generator
		
//		long length = 1000;
//		ArrayList<Integer> seed = getSeedValue(cloudlets);
		
		for(int i=0;i<cloudlets;i++){
			int dcId;
			if(choice == 4)
				dcId = (int) (mapping[i]);
			else
				dcId = (int) (Math.random() * Constants.NO_OF_DATACENTERS);
			long length = (long) (1e3 * lengthMatrix[i][dcId]);
			cloudlet[i] = new Cloudlet(i,
					length,
					Constants.TASK_PES, 
					Constants.FILE_SIZE,
                    Constants.OUTPUT_SIZE, 
                    umf, umf, umf);
			
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}
	
	/////////////////// MAIN ///////////////////////
	/**
	 * Creates main() to run this example.
	 *
	 * @param args the args
	 * @return
	 */
	public static double main(String[] args) {
		Log.printLine("Starting Simulation");
		double finishtime = 0.0;

		try {
			// First step: Initialize the CloudSim package. It should be called before creating any entities.
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
 			boolean trace_flag = false; // trace events
 			
 			//initialising cluodsim
 			CloudSim.init(num_user, calendar, trace_flag);
 			
 			//creating datacenter
// 			Datacenter datacenter0 = DC_maker.createDatacenter("Datacenter_0");
 			datacenter = new Datacenter[Constants.NO_OF_DATACENTERS];
            for (int i = 0; i < Constants.NO_OF_DATACENTERS; i++) {
                datacenter[i] = DC_maker.createDatacenter("Datacenter_" + i);
            }
            
 		// Third step: Create Broker
 			DCB_maker broker = createBroker("Broker_0");
 			int brokerId = broker.getId();
 			
 			 //Fourth step: Create VMs and Cloudlets and send them to broker
            vmlist = createVM(brokerId, Constants.NO_OF_VMS);
            cloudletList = createCloudlet(brokerId, Constants.NO_OF_TASKS,0);
			mapping = broker.algo(cloudletList,vmlist);
			System.out.println("====================================");
			for (int i=0;i<mapping.length;i++) {
				System.out.println("mapping["+i+"] =" + mapping[i]);
			}
//			new GenerateLengthMatrix(cloudletList,vmlist);
//			commMatrix = GenerateMatrices.getCommMatrix();
//			execMatrix = GenerateMatrices.getExecMatrix();
            broker.submitVmList(vmlist);
			broker.setMapping(mapping);
            broker.submitCloudletList(cloudletList);

            
            
     		
            // Fifth step: Starts the simulation
            //total completion time
            double lastClock=CloudSim.startSimulation();

            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            //newList.addAll(globalBroker.getBroker().getCloudletReceivedList());

            CloudSim.stopSimulation();

			finishtime =printCloudletList(newList);
            
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
		return finishtime;
	}
	
	/**
	 * @param name
	 * @return broker
	 * @throws Exception
	 */
	private static DCB_maker createBroker(String name) throws Exception {
        return new DCB_maker(name);
    }
	
	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static double printCloudletList(List<Cloudlet> list) {
		Cloudlet cloudlet;
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID\tSTATUS\t" +
				"Datacenter ID\t" +
				"VM ID\t" +
				"Time\t" +
				"Start Time\t" +
				"Finish Time");

		double finishTime = 0;
		DecimalFormat dft = new DecimalFormat("###.##");
		dft.setMinimumIntegerDigits(2);
		for (Cloudlet value : list) {
			cloudlet = value;
			Log.print('\t' + dft.format(cloudlet.getCloudletId()) + '\t' + '\t');

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine("\t\t" + dft.format(cloudlet.getResourceId()) +
						"\t\t\t" + dft.format(cloudlet.getVmId()) +
						"\t\t" + dft.format(cloudlet.getActualCPUTime()) +
						"\t\t" + dft.format(cloudlet.getExecStartTime()) +
						"\t\t" + dft.format(cloudlet.getFinishTime()));
			}
			finishTime = Math.max(finishTime, cloudlet.getFinishTime());
		}
		return finishTime;
		
//		DCB_maker.ETC(cloudletList,vmlist);
	}
	
}
