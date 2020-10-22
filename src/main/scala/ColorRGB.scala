/*
 * Copyright (c) 2011-2015 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jpsacha at gmail.com
 */

//this is also stolen from https://github.com/bytedeco/javacv-examples
//but I've e-mailed the author asking for permission and they agreed

import java.awt.Color

object ColorRGB {

  def fromBGR(b: Array[Byte]): ColorRGB = {
    require(b.length == 3)
    ColorRGB(b(2) & 0xFF, b(1) & 0xFF, b(0) & 0xFF)
  }

  def fromBGR(b: Array[Int]): ColorRGB = {
    require(b.length == 3)
    ColorRGB(b(2), b(1), b(0))
  }
}

case class ColorRGB(red: Int, green: Int, blue: Int) {

  def this(color: Color) = this(color.getRed, color.getGreen, color.getBlue)

  def toColor = new Color(red, green, blue)
}