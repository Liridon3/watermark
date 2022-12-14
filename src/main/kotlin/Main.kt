package watermark
import java.io.File
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import kotlin.system.exitProcess
var backgroundTransparencyColor: Color? = null
var positionMethod: String? = null

open class Image (
    var imageFile: File? = null, var width: Int = 0, var height: Int = 0,
    var components: Int = 0, var colorComponents: Int = 0, var bitsPerPixel: Int = 0, var transparency: String = "")

fun main() {
    val originalFile: File = fileIsAcceptable("image", "image")
    val bufferedOriginalImage: BufferedImage = ImageIO.read(originalFile)
    val originalImage: Image = origImageRead(originalFile)
    val watermarkFile: File = fileIsAcceptable("watermark image", "watermark")
    val bufferedWatermarkImage: BufferedImage = ImageIO.read(watermarkFile)
    watermarkSmallerThanImage(bufferedWatermarkImage, bufferedOriginalImage)
    val watermarkImage: Image = origImageRead(watermarkFile)
    val hasWatermarkAlphaChannel = watermarkImage.transparency == "TRANSLUCENT"
    val useWatermarkAlphaChannel = useWatermarkAlphaChannel(hasWatermarkAlphaChannel)
    setTransparencyColor(hasWatermarkAlphaChannel)
    val weightPercentage: Int? = weightPercent()
    positionMethod()
    writeImage(watermarkImage, bufferedOriginalImage, bufferedWatermarkImage, weightPercentage, useWatermarkAlphaChannel, hasWatermarkAlphaChannel)
}

fun fileIsAcceptable (imageName: String, imageName2: String): File {
    println("Input the $imageName filename:")
    val imageFile = File(readln())
    if (!imageFile.exists()) {
        println("The file $imageFile doesn't exist.")
        exit()
    }
    val bufferedImage: BufferedImage = ImageIO.read(imageFile)
    if (bufferedImage.colorModel.numColorComponents != 3) {
        println("The number of $imageName2 color components isn't 3.")
        exit()
    } else if (bufferedImage.colorModel.pixelSize != 24 && bufferedImage.colorModel.pixelSize != 32) {
        println("The $imageName2 isn't 24 or 32-bit.")
        exit()
    }
    return imageFile
}

fun useWatermarkAlphaChannel (hasWatermarkAlphaChannel: Boolean): String {
    var useAlphaChannel = ""
    if (hasWatermarkAlphaChannel) {
        println("Do you want to use the watermark's Alpha channel?")
        (if(readln() == "yes") {"yes"} else {"no"}).also { useAlphaChannel = it }
    }
    return useAlphaChannel
}

fun watermarkSmallerThanImage (waterMarkBufferedImage: BufferedImage, originalImage: BufferedImage) {
    if (waterMarkBufferedImage.width > originalImage.width || waterMarkBufferedImage.height > originalImage.height)
    {
        println("The watermark's dimensions are larger.")
        exit()
    }
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

fun writeImage(watermarkImage: Image, bufferedOriginal: BufferedImage, bufferedWatermark: BufferedImage,
               weight: Int?, useWatermarkAlphaChannel: String, hasWatermarkAlphaChannel: Boolean) {
    val startingPosition = if (positionMethod == "single") {
        singlePositionMethod(bufferedOriginal, bufferedWatermark)
    } else {
        listOf<Int>(0, 0)
    }
    println("Input the output image filename (jpg or png extension):")
    val outputName = File(readln())
    if (outputName.extension != "jpg" && outputName.extension != "png") {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exit()
    } else {
        val blendedImage = BufferedImage(bufferedOriginal.width, bufferedOriginal.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until bufferedOriginal.width)
            for (y in 0 until bufferedOriginal.height) {
                if (positionMethod == "single") {
                    if (x in startingPosition[0] until bufferedWatermark.width + startingPosition[0] &&
                        y in startingPosition[1] until bufferedWatermark.height + startingPosition[1]
                    ) {
                        val xX = x - startingPosition[0]
                        val yY = y - startingPosition[1]
                        writeMethod(
                            bufferedOriginal,
                            bufferedWatermark,
                            weight,
                            useWatermarkAlphaChannel,
                            hasWatermarkAlphaChannel,
                            blendedImage,
                            x,
                            y,
                            xX,
                            yY
                        )
                    } else {
                        val i = Color(bufferedOriginal.getRGB(x, y))
                        Color(i.red, i.green, i.blue)
                        blendedImage.setRGB(x, y, i.rgb)
                    }
                } else {
                    val xX = x % bufferedWatermark.width
                    val yY = y % bufferedWatermark.height
                    writeMethod(
                        bufferedOriginal,
                        bufferedWatermark,
                        weight,
                        useWatermarkAlphaChannel,
                        hasWatermarkAlphaChannel,
                        blendedImage,
                        x,
                        y,
                        xX,
                        yY
                    )
                }
            }
        ImageIO.write(blendedImage, outputName.extension, outputName)
        println("The watermarked image $outputName has been created.")
    }
}

fun setTransparencyColor (hasWatermarkAlphaChannel: Boolean): Color? {
    if (!hasWatermarkAlphaChannel) {

        println("Do you want to set a transparency color?")
        if (readln() == "yes") {
            println("Input a transparency color ([Red] [Green] [Blue]):")
            try {
                val input = readln()
                val list = mutableListOf(input.split(" ").map(String::toInt))
                val (r: Int, g: Int, b: Int) = input.split(" ").map(String::toInt)
                require(r in 0..255) { errorInputNotAcceptable("The transparency color input is invalid.") }
                require(g in 0..255) { errorInputNotAcceptable("The transparency color input is invalid.") }
                require(b in 0..255) { errorInputNotAcceptable("The transparency color input is invalid.") }
                require(list[0].lastIndex == 2) { errorInputNotAcceptable("The transparency color input is invalid.") }
                backgroundTransparencyColor = Color(r, g, b)
            } catch (e: Exception) {
                errorInputNotAcceptable("The transparency color input is invalid.")
            }
        }
    }
    return backgroundTransparencyColor
}

fun positionMethod (): String? {
    println("Choose the position method (single, grid):")
    try {
        positionMethod = readln()
        require(positionMethod == "single" || positionMethod == "grid")
    } catch (e: Exception) {
        errorInputNotAcceptable("The position method input is invalid.")
    }
    return positionMethod
}

fun singlePositionMethod(bufferedOriginal: BufferedImage, bufferedWatermark: BufferedImage): MutableList<Int> {
    var diffX: Int = bufferedOriginal.width - bufferedWatermark.width
    var diffY: Int = bufferedOriginal.height - bufferedWatermark.height
    var x: Int = -1
    var y: Int = -1
    var xy: MutableList<Int> = mutableListOf(x, y)
    if (positionMethod == "single") {
        try {
            println("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
            val inputList: List<String> = readln().split(" ")
            x = inputList[0].toInt()
            y = inputList[1].toInt()
            require(inputList.lastIndex == 1)
            if (x !in 0..diffX || y !in 0..diffY)
                errorInputNotAcceptable("The position input is out of range.")
            xy[0] = x
            xy[1] = y
        } catch (e: Exception) {
            errorInputNotAcceptable("The position input is invalid.")
        }
    }
    return xy
}

fun writeMethod (bufferedOriginal: BufferedImage, bufferedWatermark: BufferedImage, weight: Int?,
                 useWatermarkAlphaChannel: String, hasWatermarkAlphaChannel: Boolean, blendedImage: BufferedImage, x: Int, y: Int, xX: Int, yY: Int) {

    if (useWatermarkAlphaChannel == "yes") {
        val i = Color(bufferedOriginal.getRGB(x, y))
        val w = Color(bufferedWatermark.getRGB(xX, yY), true)
        val color = if (w.alpha == 255) {
            Color(
                (weight!! * w.red + (100 - weight) * i.red) / 100,
                (weight * w.green + (100 - weight) * i.green) / 100,
                (weight * w.blue + (100 - weight) * i.blue) / 100
            )
        } else {
            Color(i.red, i.green, i.blue)
        }
        blendedImage.setRGB(x, y, color.rgb)
    } else if (backgroundTransparencyColor != null && !hasWatermarkAlphaChannel) {
        val i = Color(bufferedOriginal.getRGB(x, y))
        val w = Color(bufferedWatermark.getRGB(xX, yY))
        val wo = if (w == backgroundTransparencyColor) {
            Color(w.red, w.green, w.blue, 0)
        } else w
        val color = if (wo.alpha == 255) {
            Color(
                (weight!! * wo.red + (100 - weight) * i.red) / 100,
                (weight * wo.green + (100 - weight) * i.green) / 100,
                (weight * wo.blue + (100 - weight) * i.blue) / 100
            )
        } else {
            Color(i.red, i.green, i.blue)
        }
        blendedImage.setRGB(x, y, color.rgb)
    } else {
        val i = Color(bufferedOriginal.getRGB(x, y))
        val w = Color(bufferedWatermark.getRGB(xX, yY))
        val color = Color(
            (weight!! * w.red + (100 - weight) * i.red) / 100,
            (weight * w.green + (100 - weight) * i.green) / 100,
            (weight * w.blue + (100 - weight) * i.blue) / 100
        )
        blendedImage.setRGB(x, y, color.rgb)
    }
}

fun errorInputNotAcceptable (error: String) {
    println(error)
    exit()
}

fun exit () {
    exitProcess(0)
}
