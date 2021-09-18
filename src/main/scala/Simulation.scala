import HelperUtils.{CreateLogger, ObtainConfigReference}
import Simulations.{BasicCloudSimPlusExample, BasicFirstExample}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Simulation:
  val logger = CreateLogger(classOf[Simulation])

  @main def runSimulation =
    logger.info("Constructing a cloud model...")
    BasicCloudSimPlusExample.Start()
    logger.info("Finished BasicCloudSimPlus simulation...")
    BasicFirstExample.Start()
    logger.info("Finished BasicFirstExample simulation...")

class Simulation