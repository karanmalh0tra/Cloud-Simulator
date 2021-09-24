package Simulations

import HelperUtils.ObtainConfigReference
import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.{Assert, Test}
import org.junit.Assert.{assertEquals, assertNotNull, assertTrue}


class CloudSimulationTest {

  val config = ConfigFactory.load("application.conf")
  val cloudSimulator = SimulationOne()

  @Test
  def testCheckConfig = {
    assertNotNull(config)
  }

  @Test
  def testHostCount = {
    assertEquals(cloudSimulator.hostList.size(), config.getInt("SimulationOne.host.count"))
  }

  @Test
  def testVmCount = {
    assertEquals(cloudSimulator.vmList.size(), config.getInt("SimulationOne.vm.count"))
  }

  @Test
  def testCloudletsCount = {
    assertEquals(cloudSimulator.cloudletList.size(), config.getInt("SimulationOne.cloudlet.count"))
  }

  @Test
  def testHostAndVMConfig = {
    assertTrue(config.getInt("SimulationOne.host.count")*config.getInt("SimulationOne.host.RAMInMBs")>=
      config.getInt("SimulationOne.vm.count")*config.getInt("SimulationOne.vm.RAMInMBs"))
  }

  // @Test
  @Test
  def testCost = {
    assertTrue(cloudSimulator.TotalCost > 0)
  }

}
