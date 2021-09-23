import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{SimulationOne, SimulationTwo}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Starting with SimulationOne")
    SimulationOne()
    logger.info("Finished SimulationOne simulation...")
    logBetweenSimulations()
    logger.info("Starting with SimulationTwo")
    SimulationTwo()
    logger.info("Finished SimulationTwo simulation...")

  def logBetweenSimulations() = {
    logger.info("-"*50)
    logger.info("Logs between two simulation")
    logger.info("-"*50)
  }

class Simulation