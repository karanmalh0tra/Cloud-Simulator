import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, SimulationOne}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    logger.info("Starting with SimulationOne")
    SimulationOne
    logger.info("Finished SimulationOne simulation...")

class Simulation