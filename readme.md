ScalaPT
============

# Introduction

ScalaPT is a rewrite of Kevin Beason's [smallpt](http://www.kevinbeason.com/smallpt/) global illumination renderer in Scala.
The original code was rewritten in order to make the underlying implementation more readily understandable,
whereas the original code was designed to be as short as possible.

While the application is running it displays a window containing the images as it renders.

# Usage

The project is written entirely in Scala and builds with the supplied SBT build file.

To run it, run the `scalapt.MainFrame` class, which accepts the following optional arguments:

Parameter | Default | Description
----------|---------|---
name | Cornell | Scene name.
width | 1024 | Width in pixels of rendered image.
height | 768 | Height in pixels of rendered image.
frames | 1024 | Number of frames to render.
outFile | <none> | Filename to save final image to.

For the output filename, the format is inferred from the suffix.
Supported format types are those support by the Java [ImageIO](https://docs.oracle.com/javase/8/docs/api/javax/imageio/ImageIO.html),
which includes JPG, GIF and PNG.
If the file has no suffix then it defaults to PNG.

# Sample Image

