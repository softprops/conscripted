package conscript
import sbt._
import sbt.processor.BasicProcessor

/**
 * Generates a [conscript](https://github.com/n8han/conscript) launchconfig
 * file from a provided sbt project
 */
class Conscripted extends BasicProcessor {

  /**
   * Expects one arg which represents the name of
   * the conscript to be generate
   */
  def apply(project: Project, args: String) {
    project match {
      case dp: DefaultProject => dp.getMainClass(true) match {
        case Some(main) =>
          val lc = Template format (
            project.version,
            project.organization,
            project.name,
            main,
            project.buildScalaVersion,
            repositories.mkString("\n")
          )
        FileUtilities.write(launchConfig(project, args), lc, project.log)
        case _ => project.log.error("Unable to obtain a `main` class")
      }
      case _ => project.log.error(
        "Can not generate conscript file for projects of type %s" format(
          project.getClass.getName)
      )
    }
  }

  def launchConfig(project: Project, scriptName: String) =
    new java.io.File("src/main/conscript/%s/launchconfig")

  /** defines the named default repos to resolve conscript deps.
   *  use what sbt uses by default */
  lazy val repositories = defaultRepositories

  private def defaultRepositories =
    "local" ::
    "maven-local" ::
    "scala-tools-releases" ::
    "maven-central" :: Nil

  private val Template = """
   |[app]
   |  version: %s
   |  org: %s
   |  name: %s
   |  class: %s
   |[scala]
   |  version: %s
   |[repositories]
   |  %s
   """.stripMargin
}
