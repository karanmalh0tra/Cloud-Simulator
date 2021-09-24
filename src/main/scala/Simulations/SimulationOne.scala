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
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.schedulers.cloudlet.{CloudletSchedulerAbstract, CloudletSchedulerCompletelyFair, CloudletSchedulerSpaceShared, CloudletSchedulerTimeShared}
import org.cloudbus.cloudsim.schedulers.vm.{VmSchedulerAbstract, VmSchedulerSpaceShared, VmSchedulerTimeShared}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmCost, VmSimple}
import org.cloudsimplus.builders.tables.{CloudletsTableBuilder, TextTableColumn}

import java.text.DecimalFormat
import collection.JavaConverters.*
import scala.collection.mutable.ListBuffer
import java.util

class SimulationOne {
  /*
  Fetch configs from resources and setup logger
  */
  val config = fetchConfig()
  val customer_config = ConfigFactory.load("cloud-customer")
  val provider_config = ConfigFactory.load("cloud-provider")
  val logger = CreateLogger(classOf[SimulationOne])

  val HOSTS: Int = config.getInt(("SimulationOne.host.count"))
  val HOSTS_RAM: Int = config.getInt("SimulationOne.host.RAMInMBs")
  val HOSTS_BANDWIDTH: Long = config.getLong("SimulationOne.host.BandwidthInMBps")
  val HOSTS_STORAGE: Long = config.getLong("SimulationOne.host.StorageInMBs")
  val HOSTS_PES: Int = config.getInt("SimulationOne.host.PEs")
  val HOSTS_MIPS_CAPACITY: Double = config.getDouble("SimulationOne.host.mipsCapacity")

  val VMS: Int = config.getInt("SimulationOne.vm.count")
  val VMS_MIPS_CAPACITY: Double = config.getDouble("SimulationOne.vm.mipsCapacity")
  val VMS_PES: Int = config.getInt("SimulationOne.vm.PEs")
  val VMS_RAM: Long = config.getLong("SimulationOne.vm.RAMInMBs")
  val VMS_BANDWIDTH: Long = config.getLong("SimulationOne.vm.BandwidthInMBps")
  val VMS_STORAGE: Long = config.getLong("SimulationOne.vm.StorageInMBs")
  val VM_ALLOCATION_POLICY: String = config.getString("SimulationOne.VmAllocationPolicy")
  val VM_SCHEDULER_POLICY: String = config.getString("SimulationOne.vm.scheduler")

  val CLOUDLETS: Int = config.getInt("SimulationOne.cloudlet.count")
  val CLOUDLETS_LENGTH: Long = config.getLong("SimulationOne.cloudlet.length")
  val CLOUDLETS_PES: Int = config.getInt("SimulationOne.cloudlet.PEs")
  val CLOUDLETS_SIZE: Long = config.getLong("SimulationOne.cloudlet.size")
  val CLOUDLETS_SCHEDULER_POLICY: String = config.getString("SimulationOne.cloudlet.scheduler")

  val UTILIZATION_RATIO: Double = config.getDouble("SimulationOne.utilizationRatio")

  // COSTING
  val COST_PER_SECOND: Double = config.getDouble("SimulationOne.CostPerSecond")
  val COST_PER_MEM: Double = config.getDouble("SimulationOne.CostPerMem")
  val COST_PER_STORAGE: Double = config.getDouble("SimulationOne.CostPerStorage")
  val COST_PER_BW: Double = config.getDouble("SimulationOne.CostPerBW")

  // Done: Network Latency
  val NETWORK_BW: Double = config.getDouble("SimulationOne.NetworkBW")
  val NETWORK_LATENCY: Double = config.getDouble("SimulationOne.NetworkLatency")

  val simulation = new CloudSim
  val hostList = createHostList(HOSTS)
  logger.info(s"Created hosts: $hostList")

  val datacenter0 = new DatacenterSimple(simulation, hostList, fetchVmAllocationPolicy);
  datacenter0.getCharacteristics
    .setCostPerSecond(COST_PER_SECOND)
    .setCostPerMem(COST_PER_MEM)
    .setCostPerStorage(COST_PER_STORAGE)
    .setCostPerBw(COST_PER_BW)
  val broker0 = new DatacenterBrokerSimple(simulation);

  configureNetwork()

  val vmList = createVmList(VMS)

  logger.info(s"Create virtual machine: $vmList")

  val cloudletList = createCloudlet(CLOUDLETS)

  logger.info(s"Create a list of cloudlets: $cloudletList")

  broker0.submitVmList(vmList);
  broker0.submitCloudletList(cloudletList);

  logger.info("Starting cloud simulation...")
  simulation.start();

  val TotalCost: Double = printOutput

  def configureNetwork() = {
    val networkTopology = new BriteNetworkTopology()
    simulation.setNetworkTopology(networkTopology)
    networkTopology.addLink(datacenter0,broker0,NETWORK_BW,NETWORK_LATENCY)
  }

  def createHostList(HOSTS: Int) = {
    val hostlist = new util.ArrayList[Host]
    (1 to HOSTS).map(hostPesList => hostlist.add(new HostSimple(HOSTS_RAM,
      HOSTS_BANDWIDTH,
      HOSTS_STORAGE, createHostPesList(HOSTS_PES), false)
      .setVmScheduler(fetchVmSchedulerPolicy)
    ))
    hostlist
  }

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

  def fetchVmAllocationPolicy: VmAllocationPolicyAbstract = {
    VM_ALLOCATION_POLICY match {
      case "BestFit" => new VmAllocationPolicyBestFit
      case "FirstFit" => new VmAllocationPolicyFirstFit
      case "RoundRobin" => new VmAllocationPolicyRoundRobin
      case _ => new VmAllocationPolicySimple
    }
  }

  def fetchVmSchedulerPolicy: VmSchedulerAbstract = {
    VM_SCHEDULER_POLICY match {
      case "TimeShared" => new VmSchedulerTimeShared
      case "SpaceShared" => new VmSchedulerSpaceShared
      case _ => new VmSchedulerTimeShared
    }
  }

  def createCloudlet(CLOUDLETS: Int) = {
    val utilizationModel = new UtilizationModelDynamic(UTILIZATION_RATIO)
    val cloudlets = new util.ArrayList[Cloudlet]
    (1 to CLOUDLETS).map(_ => cloudlets.add(new CloudletSimple(CLOUDLETS_LENGTH,
      CLOUDLETS_PES,
      utilizationModel
    ).setSizes(CLOUDLETS_SIZE)))
    cloudlets
  }

  def fetchCloudletSchedulerPolicy: CloudletSchedulerAbstract = {
    CLOUDLETS_SCHEDULER_POLICY match {
      case "CompletelyFair" => new CloudletSchedulerCompletelyFair
      case "TimeShared" => new CloudletSchedulerTimeShared
      case "SpaceShared" => new CloudletSchedulerSpaceShared
      case _ => new CloudletSchedulerTimeShared
    }
  }

  def fetchConfig() = {
    ObtainConfigReference("SimulationOne") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data")
    }
  }

  def createHostPesList(HOSTS_PES: Int) = {
    val pesList = new util.ArrayList[Pe]
    (1 to HOSTS_PES).map(_ => pesList.add(new PeSimple(HOSTS_MIPS_CAPACITY)))
    pesList
  }

  def printOutput: Double = {
    logger.info(s"-------------------------------------------")
    // vars are confined to method scopes and are used to keep adding cost while iterating through the Vms
    var VmProcessingCost: Double = 0.0
    var VmMemoryCost: Double = 0.0
    var VmStorageCost: Double = 0.0
    var VmBwCost: Double = 0.0
    var VmTotalCost: Double = 0.0

    val CloudletFinishedList = broker0.getCloudletFinishedList
    var CloudletProcessingCost: Double = 0.0
    var CloudletMemoryCost: Double = 0.0
    var CloudletStorageCost: Double = 0.0
    var CloudletBwCost: Double = 0.0
    var CloudletTotalCost: Double = 0.0
    broker0.getVmCreatedList.forEach {
      Vm => {
        val cost = new VmCost(Vm)
        VmProcessingCost += cost.getProcessingCost
        VmMemoryCost += cost.getMemoryCost
        VmStorageCost += cost.getStorageCost
        VmBwCost += cost.getBwCost
        VmTotalCost += VmProcessingCost + VmMemoryCost + VmStorageCost + VmBwCost
        logger.info(s"$Vm's costs ${cost.getProcessingCost} to process, ${cost.getMemoryCost} memory cost, ${cost.getStorageCost} storage cost and ${cost.getBwCost} bandwidth cost")
      }
    }

    broker0.getCloudletFinishedList.forEach {
      (cloudlet: Cloudlet) => {
        CloudletProcessingCost += (cloudlet.getActualCpuTime*cloudlet.getCostPerSec)
        CloudletMemoryCost += (cloudlet.getFileSize*cloudlet.getCostPerSec*COST_PER_MEM)
        CloudletStorageCost += (cloudlet.getActualCpuTime*cloudlet.getFileSize*COST_PER_STORAGE)
        CloudletBwCost += (cloudlet.getActualCpuTime*cloudlet.getCostPerBw)
        CloudletTotalCost += CloudletProcessingCost + CloudletMemoryCost + CloudletStorageCost + CloudletBwCost
        logger.info(s"$cloudlet's processing cost is ${cloudlet.getActualCpuTime*cloudlet.getCostPerSec}" +
          s" memory cost is ${cloudlet.getFileSize*cloudlet.getCostPerSec*COST_PER_MEM}" +
          s" storage cost is ${cloudlet.getActualCpuTime*cloudlet.getFileSize*COST_PER_STORAGE}" +
          s" bw cost is ${cloudlet.getActualCpuTime*cloudlet.getCostPerBw}")
      }
    }

    new CloudletsTableBuilder(broker0.getCloudletFinishedList).setTitle("IaaS").build()
    logger.info(s"Total cost " + "$" + s"$VmTotalCost for ${broker0.getVmsNumber} VMs which includes " + "$" + s"$VmProcessingCost Processing Cost, " + "$" + s"$VmMemoryCost Memory Cost, " + "$" + s"$VmStorageCost Storage" +
      s"Cost and " + "$" + s"$VmBwCost Bandwidth Cost")
    logger.info(s"Total cost " + "$" + s"$CloudletTotalCost for ${broker0.getCloudletFinishedList.size()} Cloudlets which includes " + "$" + s"$CloudletProcessingCost Processing Cost, " + "$" + s"$CloudletMemoryCost Memory Cost, " + "$" + s"$CloudletStorageCost Storage" +
      s"Cost and " + "$" + s"$CloudletBwCost Bandwidth Cost")
    val TotalCost = VmTotalCost + CloudletTotalCost
    logger.info(s"The total cost is $TotalCost")
    TotalCost

  }
}


object SimulationOne {

  def main(args: Array[String]): Unit = {
    new SimulationOne
  }
}
