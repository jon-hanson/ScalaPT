ScalaPT
============

# Introduction

![Screenshot](https://github.com/jon-hanson/ScalaPT/blob/master/examples/cornell2.png)

ScalaPT is a rewrite of Kevin Beason's [smallpt](http://www.kevinbeason.com/smallpt/) global illumination renderer in Scala.
The code was rewritten with several goals in mind:

* Port to Scala. The Scala code has been rewritten to be more idiomatic and avoid mutable data where possible.
* Make the underlying implementation more readily understandable (the original code was designed to be as short as possible).
* Render each frame of the image progressively (the original rendered each pixel completely before moving onto the next).

While the application is running it displays a window containing the image as it renders:

<img src="https://github.com/jon-hanson/ScalaPT/blob/master/examples/screenshot.png" width="256">

# Usage

The project is written entirely in Scala and builds with the supplied SBT build file.

Once built, run the `scalapt.MainFrame` class, which accepts the following optional arguments:

Parameter | Default | Description
----|----|----
name | Cornell | Scene name.
width | 1024 | Width in pixels of rendered image.
height | 768 | Height in pixels of rendered image.
frames | 1024 | Number of frames to render.
outFile | | Filename to save final image to.

For the output filename, the format is inferred from the suffix.
Supported format types are those supported by the Java [ImageIO](https://docs.oracle.com/javase/8/docs/api/javax/imageio/ImageIO.html) write method,
which, at present, includes JPG, GIF and PNG.
If the file has no suffix then it defaults to PNG.
