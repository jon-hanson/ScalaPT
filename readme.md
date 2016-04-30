ScalaPT
============

# Introduction

![Screenshot](https://github.com/jon-hanson/ScalaPT/blob/master/examples/cornell2.png)

ScalaPT is a rewrite of Kevin Beason's [smallpt](http://www.kevinbeason.com/smallpt/) global illumination renderer in Scala.
The code was rewritten with several goals in mind:

* Port to Scala. The Scala code has been rewritten to be more idiomatic and avoid mutable data where possible.
* Make the underlying implementation more readily understandable (the original code was designed to be as short as possible).
* Render each frame of the image progressively (the original rendered each pixel completely before moving onto the next).

Consequently, the source code lacks the brevity of the original - it's probably around 1k lines in total, so about 10 times longer.

While the application is running it displays a window containing the image as it renders:

<img src="https://github.com/jon-hanson/ScalaPT/blob/master/examples/screenshot.png" width="256">

Note, I haven't, at this stage, added any optimizations, so the rendered image is slow to converge. As per the original smallpt, to arrive at a completely noise-free image can require around 25k iterations, which, for the default image size, can take around 12 hours on a modern PC.

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

# Notes

ScalaPT differs from the original in several places:

* Each frame (or iteration) is rendered before the next, and merged into the last, to allow the image to be displayed as it is progressively refined.
* The original had what looked like a bug, whereby a bright light path could become trapped inside the glass sphere. The Russian roulette termination would not terminate the path as the path brightness was too high, which eventually caused a stack overflow. ScalaPT addresses this by increasing the chance of termination as the call stack depth increases.
* Infinite, one-way planes are used in place of giant spheres for the box walls.
