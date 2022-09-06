package watermark
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import kotlin.system.exitProcess

open class Image (
    var imageFile: File? = null, var width: Int = 0, var height: Int = 0,
    var components: Int = 0, var colorComponents: Int = 0, var bitsPerPixel: Int = 0, var transparency: String = "")

fun main() {

    val originalFile: File = fileIsAcceptable("image", null)
    val bufferedOriginalImage: BufferedImage = ImageIO.read(originalFile)
    val originalImage: Image = origImageRead(originalFile)
    val watermarkFile: File = fileIsAcceptable("watermark image", originalImage)
    val bufferedWatermarkImage: BufferedImage = ImageIO.read(watermarkFile)
    val watermarkImage: Image = origImageRead(watermarkFile)
    val weightPercentage: Int? = weightPercent()
    writeImage(watermarkImage, bufferedOriginalImage, bufferedWatermarkImage, weightPercentage)
}

fun fileIsAcceptable (imageName: String, originalImage: Image?): File {
    println("Input the $imageName filename:")
    val imageFile = File(readln())
    if (!imageFile.exists()) {
        println("The file $imageFile doesn't exist.")
        exit()
    }
    val bufferedImage: BufferedImage = ImageIO.read(imageFile)
    if (bufferedImage.colorModel.numColorComponents != 3) {
        println("The number of image color components isn't 3.")
        exit()
    } else if (bufferedImage.colorModel.pixelSize != 24 && bufferedImage.colorModel.pixelSize != 32) {
        println("The image isn't 24 or 32-bit.")
        exit()
    }else if (imageName == "watermark image")
        if  (bufferedImage.width != originalImage!!.width || bufferedImage.height != originalImage.height)
            println("The image and watermark dimensions are different.")
    exit()
    return imageFile
}


fun origImageRead (imageFile: File): Image {
    val image: BufferedImage = ImageIO.read(imageFile)
    val origImage = Image()
    origImage.imageFile = imageFile
    origImage.width = image.width
    origImage.height = image.height
    origImage.components = image.colorModel.numComponents
    origImage.colorComponents = image.colorModel.numColorComponents
    origImage.bitsPerPixel = image.colorModel.pixelSize
    origImage.transparency = when(image.transparency) {1 -> "OPAQUE" 2 -> "BITMASK" else -> "TRANSLUCENT"}
    return  origImage
}

fun weightPercent (): Int? {
    println("Input the watermark transparency percentage (Integer 0-100):")
    val percentage = readln().toIntOrNull()
    if (percentage !is Int) {
        println("The transparency percentage isn't an integer number.")
        exit()
    } else if (percentage !in 0..100) {
        println("The transparency percentage is out of range.")
        exit()
    }
    return percentage
}

fun writeImage(watermarkImage: Image, bufferedOriginal: BufferedImage, bufferedWatermark: BufferedImage, weight: Int?) {
    println("Input the output image filename (jpg or png extension):")
    val outputName = File(readln())
    if (outputName.extension != "jpg" || outputName.extension != "png") {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exit()
    }
    val blendedImage = BufferedImage(watermarkImage.width, watermarkImage.height, BufferedImage.TYPE_INT_RGB)
    for (x in 0 until bufferedOriginal.width)
        for (y in 0 until bufferedOriginal.height) {
            val i = Color(bufferedOriginal.getRGB(x, y))
            val w = Color(bufferedWatermark.getRGB(x, y))
            val color = Color(
                (weight!! * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
            blendedImage.setRGB(x, y, color.rgb)
        }
    ImageIO.write(blendedImage, outputName.extension, outputName)
    println("The watermarked image $outputName has been created.")
}

fun exit () {
    exitProcess(0)
}