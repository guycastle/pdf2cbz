package be.guycastle

import java.awt.image.BufferedImage
import java.io.{File, FileOutputStream}
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.{ZipEntry, ZipOutputStream}

import javax.imageio.ImageIO
import org.apache.commons.cli.{CommandLine, DefaultParser, Options}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.{ImageType, PDFRenderer}
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

object Pdf2Cbz {

  private final val LOGGER = LoggerFactory.getLogger("be.guycastle.Pdf2Cbz")

  private final val OPT_SOURCE: Char = 's'
  private final val OPT_RECURSIVE: Char = 'r'
  private final val OPT_DELETE_ORIGINAL: Char = 'd'
  private final val OPT_DPI: Char = 'i'
  private final val OPT_ONLY_IMAGES: Char = 'm'
  private final val EXT_PDF = "pdf"

  private final val DEFAULT_DPI: Float = 300

  private final val OPTIONS: Options = new Options()
    .addOption(OPT_SOURCE.toString, "source", true, "The source folder in which to find PDF files")
    .addOption(OPT_RECURSIVE.toString, "recursive", false, "Recursively go through all subfolders")
    .addOption(OPT_DELETE_ORIGINAL.toString, "deleteOriginal", false, "Delete the original PDF after conversion")
    .addOption(OPT_ONLY_IMAGES.toString, "onlyImages", false, "Use this flag if you only want to extract images")

  def main(args: Array[String]): Unit = {
    val cmd = new DefaultParser().parse(OPTIONS, args)
    val source = cmd.getOptionValue(OPT_SOURCE, ".")
    val recursive = cmd.hasOption(OPT_RECURSIVE)
    FileUtils.iterateFiles(Paths.get(source).toFile, Array(EXT_PDF.toUpperCase, EXT_PDF.toLowerCase), recursive).asScala
      .foreach(convertPdfToCbz(_, cmd))
  }

  private def convertPdfToCbz(file: File, cmd: CommandLine): Unit = {
    val pdf = PDDocument.load(file)
    Try {
      val conversionMethod = if (cmd.hasOption(OPT_ONLY_IMAGES)) extractImages _ else convertToImages _
      zipImages(file, conversionMethod(pdf, cmd))
    } match {
      case Failure(ex) =>
        LOGGER.error(s"An error occurred while converting ${file.getAbsolutePath}: ${ex.getMessage}", ex)
        pdf.close()
      case Success(cbz) =>
        LOGGER.info(s"CBZ created for ${FilenameUtils.getName(file.getName)}: ${cbz.getAbsolutePath}")
        pdf.close()
        if (cmd.hasOption(OPT_DELETE_ORIGINAL)) file.delete()
    }
  }

  private def extractImages(pdf: PDDocument, cmd: CommandLine): Seq[BufferedImage] = pdf.getDocumentCatalog.getPages.asScala.toSeq.flatMap(page => {
    val resources = page.getResources
    resources.getXObjectNames.asScala.map(resources.getXObject).filter(o => o.isInstanceOf[PDImageXObject]).map(_.asInstanceOf[PDImageXObject].getImage).toSeq
  })

  private def convertToImages(pdf: PDDocument, cmd: CommandLine): Seq[BufferedImage] = {
    val renderer = new PDFRenderer(pdf)
    for (i <- 0 until pdf.getNumberOfPages) yield renderer.renderImageWithDPI(i, Try(cmd.getOptionValue(OPT_DPI).toFloat).toOption.getOrElse(DEFAULT_DPI))
  }

  def zipImages(file: File, images:Seq[BufferedImage]): File = {
    val cbzFile = s"${FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath)}/${FilenameUtils.getBaseName(file.getName)}.cbz"
    val pageCounter = new AtomicInteger(0)
    val fos = new FileOutputStream(Paths.get(cbzFile).toFile)
    val zipOs = new ZipOutputStream(fos)
    images.foreach(imageObj => {
      Try {
        val name = FilenameUtils.getName(s"${"%07d".format(pageCounter.getAndIncrement)}.png")
        val zipEntry = new ZipEntry(name)
        zipOs.putNextEntry(zipEntry)
        ImageIO.write(imageObj, "jpg", zipOs)
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