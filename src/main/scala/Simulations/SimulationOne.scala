package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRoundRobin
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.{Cloudlet, CloudletSimple}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.{Host, HostSimple}
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.{Vm, VmSimple}
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*
import scala.collection.mutable.ListBuffer
import java.util

class SimulationOne {
  /*
  Fetch configs from resources and setup logger
  */
  val config = fetchConfig()
  val logger = CreateLogger(classOf[SimulationOne])

  val HOSTS: Int = config.getInt(("SimulationOne.host.count"))
  val HOSTS_RAM: Int = config.getInt("SimulationOne.host.RAMInMBs")
  val HOSTS_BANDWIDTH: Long = config.getLong("SimulationOne.host.BandwidthInMBps")
  val HOSTS_STORAGE: Long = config.getLong("SimulationOne.host.StorageInMBs")
  val HOSTS_PES: Int = config.getInt("SimulationOne.host.PEs")
  val HOSTS_MIPS_CAPACITY: Long = config.getLong("SimulationOne.host.mipsCapacity")

  val VMS: Int = config.getInt("SimulationOne.vm.count")
  val VMS_MIPS_CAPACITY: Long = config.getLong("SimulationOne.vm.mipsCapacity")
  val VMS_PES: Int = config.getInt("SimulationOne.vm.PEs")
  val VMS_RAM: Long = config.getLong("SimulationOne.vm.RAMInMBs")
  val VMS_BANDWIDTH: Long = config.getLong("SimulationOne.vm.BandwidthInMBps")
  val VMS_STORAGE: Long = config.getLong("SimulationOne.vm.StorageInMBs")

  val CLOUDLETS: Int = config.getInt("SimulationOne.cloudlet.count")
  val CLOUDLETS_LENGTH: Long = config.getLong("SimulationOne.cloudlet.length")
  val CLOUDLETS_PES: Int = config.getInt("SimulationOne.cloudlet.PEs")
  val CLOUDLETS_SIZE: Long = config.getLong("SimulationOne.cloudlet.size")

  val UTILIZATION_RATIO: Double = config.getDouble("SimulationOne.utilizationRatio")


  val simulation = new CloudSim();
  val hostList = createHostList(HOSTS)
  logger.info(s"Created hosts: $hostList")

  val datacenter0 = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyRoundRobin());
  val broker0 = new DatacenterBrokerSimple(simulation);

  val vmList = createVmList(VMS)

  logger.info(s"Create virtual machine: $vmList")

  val cloudletList = createCloudlet(CLOUDLETS)

  logger.info(s"Create a list of cloudlets: $cloudletList")

  broker0.submitVmList(vmList);
  broker0.submitCloudletList(cloudletList);

  logger.info("Starting cloud simulation...")
  simulation.start();

  val finishedCloudlets = broker0.getCloudletFinishedList();
  new CloudletsTableBuilder(finishedCloudlets).build();

  def createHostList(HOSTS: Int) = {
    val hostlist = new util.ArrayList[Host]
    (1 to HOSTS).map(hostPesList => hostlist.add(new HostSimple(HOSTS_RAM,
      HOSTS_BANDWIDTH,
      HOSTS_STORAGE, createHostPesList(HOSTS_PES), false)
    ))
    hostlist
  }

  def createVmList(VmCount: Int) = {
    val vmlist = new util.ArrayList[Vm]
    (1 to VmCount).map(_ => vmlist.add(new VmSimple(VMS_MIPS_CAPACITY, VMS_PES)
      .setRam(VMS_RAM)
      .setBw(VMS_BANDWIDTH)
      .setSize(VMS_STORAGE)))
    vmlist

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
}

object SimulationOne {

  def main(args: Array[String]): Unit = {
    new SimulationOne
  }
}
