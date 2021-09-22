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

  val config = fetchConfig()
  val logger = CreateLogger(classOf[SimulationOne])

  val simulation = new CloudSim();

  val hostList = createHostList(config.getInt(("SimulationOne.host.count")))
  logger.info(s"Created hosts: $hostList")

  val datacenter0 = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyRoundRobin());
  val broker0 = new DatacenterBrokerSimple(simulation);

  val vmList = createVmList(config.getInt("SimulationOne.vm.count"))

  logger.info(s"Create virtual machine: $vmList")

  val cloudletList = createCloudlet(config.getInt("SimulationOne.cloudlet.count"))

  logger.info(s"Create a list of cloudlets: $cloudletList")

  broker0.submitVmList(vmList);
  broker0.submitCloudletList(cloudletList);

  logger.info("Starting cloud simulation...")
  simulation.start();

  val finishedCloudlets = broker0.getCloudletFinishedList();
  new CloudletsTableBuilder(finishedCloudlets).build();

  def createHostList(hostCount: Int) = {
//    val hostPesList = createHostPesList(config.getInt("SimulationOne.host.PEs"))
//    logger.info(s"Created one processing element: hostPes")
    val hostlist = new util.ArrayList[Host]
    (1 to hostCount).map(hostPesList => hostlist.add(new HostSimple(config.getInt("SimulationOne.host.RAMInMBs"),
        config.getLong("SimulationOne.host.BandwidthInMBps"),
        config.getLong("SimulationOne.host.StorageInMBs"), createHostPesList(config.getInt("SimulationOne.host.PEs")), false)
    ))
    hostlist
  }

  def createVmList(VmCount: Int) = {
    val vmlist = new util.ArrayList[Vm]
    (1 to VmCount).map(_ => vmlist.add(new VmSimple(config.getLong("SimulationOne.vm.mipsCapacity"), config.getLong("SimulationOne.vm.PEs"))
      .setRam(config.getLong("SimulationOne.vm.RAMInMBs"))
      .setBw(config.getLong("SimulationOne.vm.BandwidthInMBps"))
      .setSize(config.getLong("SimulationOne.vm.StorageInMBs"))))
    vmlist

  }

  def createCloudlet(cloudletCount: Int) = {
    val utilizationModel = new UtilizationModelDynamic(config.getDouble("SimulationOne.utilizationRatio"))
    val cloudlets = new util.ArrayList[Cloudlet]
    (1 to cloudletCount).map(_ => cloudlets.add(new CloudletSimple(config.getLong("SimulationOne.cloudlet.length"),
      config.getInt("SimulationOne.cloudlet.PEs"),
      utilizationModel
    ).setSizes(config.getLong("SimulationOne.cloudlet.size"))))
    cloudlets
  }

  def fetchConfig() = {
    ObtainConfigReference("SimulationOne") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data")
    }
  }

  def createHostPesList(pesCount: Int) = {
    val pesList = new util.ArrayList[Pe]
    (1 to pesCount).map(_ => pesList.add(new PeSimple(config.getLong("SimulationOne.host.mipsCapacity"))))
    pesList
  }
}

object SimulationOne {

  def main(args: Array[String]): Unit = {
    new SimulationOne
  }
}
