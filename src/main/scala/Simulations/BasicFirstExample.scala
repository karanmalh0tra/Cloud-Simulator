package Simulations

import HelperUtils.{CreateLogger, ObtainConfigReference}
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple
import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.datacenters.DatacenterSimple
import org.cloudbus.cloudsim.hosts.HostSimple
import org.cloudbus.cloudsim.resources.{Pe, PeSimple}
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelDynamic
import org.cloudbus.cloudsim.vms.VmSimple
import org.cloudsimplus.builders.tables.CloudletsTableBuilder

import collection.JavaConverters.*
import scala.collection.mutable.ListBuffer

class BasicFirstExample

object BasicFirstExample:

  val config = ObtainConfigReference("firstExample") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data")
  }
  val logger = CreateLogger(classOf[BasicFirstExample])

  def Start() =
    val simulation = new CloudSim();

    val hostPes = List(
      new PeSimple(config.getLong("firstExample.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity")),
      new PeSimple(config.getLong("cloudSimulator.host.mipsCapacity"))
    )
    logger.info(s"Created one processing element: $hostPes")

    val hostList = List(
      new HostSimple(config.getLong("firstExample.host.RAMInMBs"),
        config.getLong("firstExample.host.BandwidthInMBps"),
        config.getLong("firstExample.host.StorageInMBs"),
        hostPes.asJava)
    )
    logger.info(s"Created hosts: $hostList")

    val datacenter0 = new DatacenterSimple(simulation, hostList.asJava);
    val broker0 = new DatacenterBrokerSimple(simulation);

    val vmList = List(new VmSimple(config.getLong("firstExample.host.mipsCapacity"), config.getLong("firstExample.vm.PEs"))
      .setRam(config.getLong("firstExample.vm.RAMInMBs"))
      .setBw(config.getLong("firstExample.vm.BandwidthInMBps"))
      .setSize(config.getLong("firstExample.vm.StorageInMBs")),
      new VmSimple(config.getLong("firstExample.host.mipsCapacity"), config.getLong("firstExample.vm.PEs"))
        .setRam(config.getLong("firstExample.vm.RAMInMBs"))
        .setBw(config.getLong("firstExample.vm.BandwidthInMBps"))
        .setSize(config.getLong("firstExample.vm.StorageInMBs"))
    )

    logger.info(s"Create virtual machine: $vmList")

    val utilizationModel = new UtilizationModelDynamic(config.getDouble("firstExample.utilizationRatio"))
    val cloudletList = List(
      new CloudletSimple(config.getLong("firstExample.cloudlet.length"),
        config.getInt("firstExample.cloudlet.PEs"),
        utilizationModel
      ).setSizes(config.getLong("firstExample.cloudlet.size")),
      new CloudletSimple(config.getLong("firstExample.cloudlet.length"),
        config.getInt("firstExample.cloudlet.PEs"),
        utilizationModel
      ).setSizes(config.getLong("firstExample.cloudlet.size")),
      new CloudletSimple(config.getLong("firstExample.cloudlet.length"),
        config.getInt("firstExample.cloudlet.PEs"),
        utilizationModel
      ).setSizes(config.getLong("firstExample.cloudlet.size")),
      new CloudletSimple(config.getLong("firstExample.cloudlet.length"),
        config.getInt("firstExample.cloudlet.PEs"),
        utilizationModel
      ).setSizes(config.getLong("firstExample.cloudlet.size"))
    )

    logger.info(s"Create a list of cloudlets: $cloudletList")

    broker0.submitVmList(vmList.asJava);
    broker0.submitCloudletList(cloudletList.asJava);

    logger.info("Starting cloud simulation...")
    simulation.start();

    val finishedCloudlets = broker0.getCloudletFinishedList();
    new CloudletsTableBuilder(finishedCloudlets).build();
