package be.guycastle

import java.io.{BufferedInputStream, File, FileOutputStream}
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.{ZipEntry, ZipOutputStream}

import javax.imageio.ImageIO
import org.apache.commons.cli.{DefaultParser, Options}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object Pdf2Cbz {

  private final val LOGGER = LoggerFactory.getLogger("be.guycastle.Pdf2Cbz")

  private final val OPT_SOURCE: Char = 's'
  private final val OPT_RECURSIVE: Char = 'r'
  private final val OPT_DELETE_ORIGINAL: Char = 'd'
  private final val EXT_PDF = "pdf"

  private final val OPTIONS: Options = new Options()
    .addOption(OPT_SOURCE.toString, "source", true, "The source folder in which to find PDF files")
    .addOption(OPT_RECURSIVE.toString, "recursive", false, "Recursively go through all subfolders")
    .addOption(OPT_DELETE_ORIGINAL.toString, "deleteOriginal", false, "Delete the original PDF after conversion")

  def main(args: Array[String]): Unit = {
    val cmd = new DefaultParser().parse(OPTIONS, args)
    val source = cmd.getOptionValue(OPT_SOURCE, "")
    val recursive = cmd.hasOption(OPT_RECURSIVE)
    val deleteOriginal = cmd.hasOption(OPT_DELETE_ORIGINAL)
    FileUtils.iterateFiles(Paths.get(source).toFile, Array(EXT_PDF.toUpperCase, EXT_PDF.toLowerCase), recursive).asScala
      .foreach(extractImages(_, deleteOriginal))
  }

  private def extractImages(file: File, deleteOriginal: Boolean) = Try {
    val pdf = PDDocument.load(file)
    Try {
      zipImages(file, pdf.getDocumentCatalog.getPages.asScala.toSeq.flatMap(page => {
        val resources = page.getResources
        resources.getXObjectNames.asScala.map(resources.getXObject).filter(o => o.isInstanceOf[PDImageXObject]).map(_.asInstanceOf[PDImageXObject]).toSeq
      }))
    } match {
      case Failure(ex) =>
        LOGGER.error(s"An error occurred while converting ${file.getAbsolutePath}: ${ex.getMessage}", ex)
        pdf.close()
      case Success(cbz) =>
        LOGGER.info(s"CBZ created for ${FilenameUtils.getName(file.getName)}: ${cbz.getAbsolutePath}")
        pdf.close()
        if (deleteOriginal) file.delete()
    }
  }

  def zipImages(file: File, images:Seq[PDImageXObject]): File = {
    val cbzFile = s"${FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath)}/${FilenameUtils.getBaseName(file.getName)}.cbz"
    val pageCounter = new AtomicInteger(0)
    val fos = new FileOutputStream(Paths.get(cbzFile).toFile)
    val zipOs = new ZipOutputStream(fos)
    images.foreach(imageObj => {
      Try {
        val name = FilenameUtils.getName(s"${"%07d".format(pageCounter.getAndIncrement)}.png")
        val zipEntry = new ZipEntry(name)
        zipOs.putNextEntry(zipEntry)
        ImageIO.write(imageObj.getImage, "jpg", zipOs)
        zipOs.closeEntry()
        name
      } match {
        case Failure(ex) => LOGGER.warn(s"Failed to extract image:${file.getName}, page:${pageCounter.get}")
        case Success(name) => LOGGER.debug(s"Added $name to $cbzFile")
      }
    })
    zipOs.close()
    fos.close()
    Paths.get(cbzFile).toFile
  }

}