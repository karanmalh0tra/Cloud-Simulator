package Simulations

import HelperUtils.ObtainConfigReference
import Simulations.BasicCloudSimPlusExample.config
import com.typesafe.config.ConfigFactory
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.junit.{Assert, Test}
import org.junit.Assert.{assertEquals, assertNotNull}


class CloudSimulationTest {

  val config = ConfigFactory.load("application.conf")
  val cloudSimulator = new SimulationOne

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

  // @Test
  // Check cost is not <= 0

}