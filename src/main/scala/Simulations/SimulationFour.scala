package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.allocationpolicies.{VmAllocationPolicyAbstract, VmAllocationPolicyBestFit, VmAllocationPolicyFirstFit, VmAllocationPolicyRoundRobin, VmAllocationPolicySimple}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.network.topologies.BriteNetworkTopology
import org.cloudbus.cloudsim.network.topologies.NetworkTopology
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerAbstract, CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerAbstract, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*
import scala.collection.mutable.ListBuffer
import java.util

class SimulationFour {
  /*
  Fetch configs from resources and setup logger
  */
  val customer_config = ConfigFactory.load("cloud-customer")
  val provider_config = ConfigFactory.load("cloud-provider")
  val logger = CreateLogger(classOf[SimulationOne])

  //Service Model
  val SERVICE_MODEL: String = provider_config.getString("SaaS.type")

  // Hosts Config
  val HOSTS: Int = provider_config.getInt(("SaaS.host.count"))
  val HOSTS_RAM: Int = provider_config.getInt("SaaS.host.RAMInMBs")
  val HOSTS_BANDWIDTH: Long = provider_config.getLong("SaaS.host.BandwidthInMBps")
  val HOSTS_STORAGE: Long = provider_config.getLong("SaaS.host.StorageInMBs")
  val HOSTS_PES: Int = provider_config.getInt("SaaS.host.PEs")
  val HOSTS_MIPS_CAPACITY: Double = provider_config.getDouble("SaaS.host.mipsCapacity")

  // VMs Config
  val VMS: Int = provider_config.getInt("SaaS.vm.count")
  val VMS_MIPS_CAPACITY: Double = provider_config.getDouble("SaaS.vm.mipsCapacity")
  val VMS_PES: Int = provider_config.getInt("SaaS.vm.PEs")
  val VMS_RAM: Long = provider_config.getLong("SaaS.vm.RAMInMBs")
  val VMS_BANDWIDTH: Long = provider_config.getLong("SaaS.vm.BandwidthInMBps")
  val VMS_STORAGE: Long = provider_config.getLong("SaaS.vm.StorageInMBs")
  val VM_ALLOCATION_POLICY: String = provider_config.getString("SaaS.VmAllocationPolicy")
  val VM_SCHEDULER_POLICY: String = provider_config.getString("SaaS.vm.scheduler")

  // Cloudlets Configs
  val CLOUDLETS: Int = customer_config.getInt("SaaS.cloudlet.count")
  val CLOUDLETS_LENGTH: Long = provider_config.getLong("SaaS.cloudlet.length")
  val CLOUDLETS_PES: Int = provider_config.getInt("SaaS.cloudlet.PEs")
  val CLOUDLETS_SIZE: Long = provider_config.getLong("SaaS.cloudlet.size")
  val CLOUDLETS_SCHEDULER_POLICY: String = provider_config.getString("SaaS.cloudlet.scheduler")

  val UTILIZATION_RATIO: Double = provider_config.getDouble("SaaS.utilizationRatio")

  // COSTING
  val COST_PER_SECOND: Double = provider_config.getDouble("SaaS.CostPerSecond")
  val COST_PER_MEM: Double = provider_config.getDouble("SaaS.CostPerMem")
  val COST_PER_STORAGE: Double = provider_config.getDouble("SaaS.CostPerStorage")
  val COST_PER_BW: Double = provider_config.getDouble("SaaS.CostPerBW")

  // Network Latency
  val NETWORK_BW: Double = provider_config.getDouble("SaaS.NetworkBW")
  val NETWORK_LATENCY: Double = provider_config.getDouble("SaaS.NetworkLatency")

  // Create a simulation and hostlist and display the hostlist on logs
  val simulation = new CloudSim
  val hostList = createHostList(HOSTS)
  logger.info(s"Created hosts: $hostList")

  // Create a datacenter and assign costing
  val datacenter0 = new DatacenterSimple(simulation, hostList, fetchVmAllocationPolicy);
  datacenter0.getCharacteristics
    .setCostPerSecond(COST_PER_SECOND)
    .setCostPerMem(COST_PER_MEM)
    .setCostPerStorage(COST_PER_STORAGE)
    .setCostPerBw(COST_PER_BW)
  val broker0 = new DatacenterBrokerSimple(simulation);

  // configure the network
  configureNetwork()

  // Create the list of VMs and print them on logs
  val vmList = createVmList(VMS)
  logger.info(s"Create virtual machine: $vmList")

  // Create the list of cloudlets and print them on logs
  val cloudletList = createCloudlet(CLOUDLETS)
  logger.info(s"Create a list of cloudlets: $cloudletList")

  // Submit the list of VMs and Cloudlets to the broker
  broker0.submitVmList(vmList);
  broker0.submitCloudletList(cloudletList);

  // Start the simulation
  logger.info("Starting cloud simulation...")
  simulation.start();

  // Print the output of the simulation
  new CloudletsTableBuilder(broker0.getCloudletFinishedList).setTitle("SaaS").build()

  // Print the costs of each VM and Cloudlets and also store the total cost for test case check
  val TotalCost: Double = CalculateAndPrintCost

  def configureNetwork() = {
    val networkTopology = new BriteNetworkTopology()
    simulation.setNetworkTopology(networkTopology)
    networkTopology.addLink(datacenter0,broker0,NETWORK_BW,NETWORK_LATENCY)
  }

  // Create the list of hosts and assigns the configs as well as a VM Scheduler Policy.
  // Return the list of hosts
  def createHostList(HOSTS: Int) = {
    val hostlist = new util.ArrayList[Host]
    (1 to HOSTS).map(hostPesList => hostlist.add(new HostSimple(HOSTS_RAM,
      HOSTS_BANDWIDTH,
      HOSTS_STORAGE, createHostPesList(HOSTS_PES), false)
      .setVmScheduler(fetchVmSchedulerPolicy)
    ))
    hostlist
  }

  // Creates the list of VMs and assigns the VM configs as well as sets the Cloudlets Scheduler Policy.
  // Returns the list of VMs
  def createVmList(VmCount: Int) = {
    val vmlist = new util.ArrayList[Vm]
    (1 to VmCount).map(_ => vmlist.add(new VmSimple(VMS_MIPS_CAPACITY, VMS_PES)
      .setRam(VMS_RAM)
      .setBw(VMS_BANDWIDTH)
      .setSize(VMS_STORAGE)
      .setCloudletScheduler(fetchCloudletSchedulerPolicy)
    ))
    vmlist
  }

  // Based on the configs, sets the VM Allocation Policy
  def fetchVmAllocationPolicy: VmAllocationPolicyAbstract = {
    VM_ALLOCATION_POLICY match {
      case "BestFit" => new VmAllocationPolicyBestFit
      case "FirstFit" => new VmAllocationPolicyFirstFit
      case "RoundRobin" => new VmAllocationPolicyRoundRobin
      case _ => new VmAllocationPolicySimple
    }
  }

  // Based on the configs, sets the VM Scheduler Policy
  def fetchVmSchedulerPolicy: VmSchedulerAbstract = {
    VM_SCHEDULER_POLICY match {
      case "TimeShared" => new VmSchedulerTimeShared
      case "SpaceShared" => new VmSchedulerSpaceShared
      case _ => new VmSchedulerTimeShared
    }
  }

  // Creates the list of cloudlets and assigns configs to them.
  // Returns the cloudlets list
  def createCloudlet(CLOUDLETS: Int) = {
    val utilizationModel = new UtilizationModelDynamic(UTILIZATION_RATIO)
    val cloudlets = new util.ArrayList[Cloudlet]
    (1 to CLOUDLETS).map(_ => cloudlets.add(new CloudletSimple(CLOUDLETS_LENGTH,
      CLOUDLETS_PES,
      utilizationModel
    ).setSizes(CLOUDLETS_SIZE)))
    cloudlets
  }

  // Based on the configs entered, sets the Cloudlet Scheduler Policy
  def fetchCloudletSchedulerPolicy: CloudletSchedulerAbstract = {
    CLOUDLETS_SCHEDULER_POLICY match {
      case "CompletelyFair" => new CloudletSchedulerCompletelyFair
      case "TimeShared" => new CloudletSchedulerTimeShared
      case "SpaceShared" => new CloudletSchedulerSpaceShared
      case _ => new CloudletSchedulerTimeShared
    }
  }

  // Creates a list of PEs to be stored in hosts
  // Returns the PEs List
  def createHostPesList(HOSTS_PES: Int) = {
    val pesList = new util.ArrayList[Pe]
    (1 to HOSTS_PES).map(_ => pesList.add(new PeSimple(HOSTS_MIPS_CAPACITY)))
    pesList
  }

  // Prints the costs of the Software
  // Calculates the costs of Running the Cloudlets
  // Prints the total cost charged for using the Software
  // Returns the total costs
  def CalculateAndPrintCost: Double = {
    logger.info(s"-------------------------------------------")
    val SoftwareCost: Double = provider_config.getDouble("SaaS.CostOfSoftware")

    val CostPerCloudlet: Double = provider_config.getDouble("SaaS.CostPerCloudlet")
    val NumberOfCloudlets = broker0.getCloudletFinishedList.size()
    val CloudletTotalCost: Double = CostPerCloudlet * NumberOfCloudlets

    logger.info(s"The cost of using the software is $SoftwareCost")
    logger.info(s"The cost per cloudlet is $CostPerCloudlet and the number of cloudlets are $NumberOfCloudlets")
    logger.info(s"The total cost of cloudlets execution is $CloudletTotalCost")
    val TotalCost = SoftwareCost + CloudletTotalCost
    logger.info(s"The total cost of using this Service Model is $TotalCost")
    TotalCost

  }
}

object SimulationFour {

  def main(args: Array[String]): Unit = {
    new SimulationFour
  }
}
