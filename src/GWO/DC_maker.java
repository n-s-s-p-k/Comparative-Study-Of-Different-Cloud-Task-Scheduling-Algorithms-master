package GWO;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import utils.Constants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DC_maker {
	 public static Datacenter createDatacenter(String name) {

	        // Here are the steps needed to create a PowerDatacenter:
	        // 1. We need to create a list to store one or more Machines
	        List<Host> hostList = new ArrayList<Host>();

	        // 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
	        //    create a list to store these PEs before creating a Machine.
//	        ArrayList<Pe>[] peList = new ArrayList[Constants.NO_OF_PES];
	        
	        List<Pe> peList1 = new ArrayList<Pe>();
	        //Another list, for a dual-core machine
			List<Pe> peList2 = new ArrayList<Pe>();
			
	        int mips = Constants.HOST_MIPS;

	        // 3. Create PEs and add these into the list.
	      //for a quad-core machine, a list of 4 PEs is required:
			peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
			peList1.add(new Pe(3, new PeProvisionerSimple(mips)));
			
			peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
			peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

//			for (int i =0  ;i< Constants.NO_OF_PES ; i++) {
//				peList[i].add(new Pe(0, new PeProvisionerSimple(mips)));
//			}
	        //4. Create Hosts with its id and list of PEs and add them to the list of machines
	        int hostId = 0; 
	        int ram = Constants.HOST_RAM;
	        long storage = Constants.STORAGE;
	        int bw = Constants.HOST_BANDWIDTH;
	        
	        hostList.add(
	                new Host(
	                        hostId,
	                        new RamProvisionerSimple(ram),
	                        new BwProvisionerSimple(bw),
	                        storage,
	                        peList1,
	                        new VmSchedulerTimeShared(peList1)
	                )
	        ); // This is our first machine
	        
	        hostId++;

			hostList.add(
	    			new Host(
	    				hostId,
	    				new RamProvisionerSimple(ram),
	    				new BwProvisionerSimple(bw),
	    				storage,
	    				peList2,
	    				new VmSchedulerTimeShared(peList2)
	    			)
	    		); // Second machine

	        // 5. Create a DatacenterCharacteristics object that stores the
	        //    properties of a data center: architecture, OS, list of
	        //    Machines, allocation policy: time- or space-shared, time zone
	        //    and its price (G$/Pe time unit).
	        String arch = Constants.ARCHITECTURE; 
	        String os = Constants.OS;
	        String vmm = Constants.VMM_NAME; 
	        double time_zone = Constants.TIME_ZONE;
	        double cost = Constants.COST_PROCESSING; 
	        double costPerMem = Constants.COST_MEMORY;
	        double costPerStorage = Constants.COST_STORAGE; 
	        double costPerBw = Constants.COST_BANDWIDTH;
	        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

	        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
	                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


	        // 6. Finally, we need to create a PowerDatacenter object.
	        Datacenter datacenter = null;
	        try {
	            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return datacenter;
	    }

}
