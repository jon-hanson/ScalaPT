ScalaPT
============

Global Illumination in several hundred lines of Scala.

# Introduction

![Screenshot](https://github.com/jon-hanson/ScalaPT/blob/master/examples/cornell2.png)

ScalaPT is a rewrite in Scala of Kevin Beason's [smallpt](http://www.kevinbeason.com/smallpt/) global illumination renderer.

Smallpt (and therefore ScalaPT) solves the [Rendering Equation](https://en.wikipedia.org/wiki/Rendering_equation)
using a [Monte Carlo](https://en.wikipedia.org/wiki/Monte_Carlo_method) approach,
whereby multiple light paths are fired per pixel and averaged over.
Each path is traced backwards through the scene as it bounces off various surfaces.
The incoming ray for each bounce is chosen at random,
governed by the [bidirectional distribution functions](https://en.wikipedia.org/wiki/Bidirectional_scattering_distribution_function)
for the material of the surface in question.

This approach, while slow to converge,
is a relatively simple means of obtaining photorealistic images,
which include natural effects such as ambient occlusion, light bleeding,
reflections, refraction and caustics.

The code was rewritten with several goals in mind:

* Port to Scala. The Scala code has been rewritten to be more idiomatic and avoid mutable data where possible.
* Make the underlying implementation more readily understandable (the original code was designed to be as short as possible).
* Render each frame (i.e. each iteration) of the image progressively (the original rendered each pixel completely before moving onto the next).

Consequently, the source code lacks the brevity of the original - excluding the windowing and I/O code it's around 800 lines in total, so about 8 times longer.

While the application is running it displays a window containing the image as it renders:

<img src="https://github.com/jon-hanson/ScalaPT/blob/master/examples/screenshot.png" width="257"/>

Note, I haven't, at this stage, added any optimizations, so the rendered image is relatively slow to converge.
As per the original smallpt, to arrive at a completely noise-free image can require around 25k iterations,
which, for the default image size, can take several hours on a modern PC.

# Usage

The project is written entirely in Scala (v2.11.8), builds with the supplied SBT (v0.3.11) build file, and runs on Java 1.8.0.

Once built, run the `scalapt.Main` class.
If you run with no arguments, then the resultant error message will document the supported cmd-line options:
```
Usage: scalpt [options]

  -i, --sceneFile filename
                           input scene description file
  -w, --width pixels       image width
  -h, --height pixels      image height
  -n, --frames count       number of frames to render
  -s, --seed number        random number seed
  -d, --display            display the image as it renders
  -o, --imageFile filename
                           image output file name
  -f, --framesDir filename
                           frame output directory
```
Note:
* Sample scenes are provided in the scenes sub-directory.
* For the output filename, the format is inferred from the suffix.
  * Supported format types are those supported by the Java [ImageIO](https://docs.oracle.com/javase/8/docs/api/javax/imageio/ImageIO.html) write method,
which, at present, includes JPG, GIF and PNG.
  * If the file has no suffix then it defaults to PNG format.

# Notes

ScalaPT differs from the original in several places:

* Each frame (or iteration) is rendered before the next, and merged into the aggregated result, to allow the image to be displayed as it is progressively refined.
* The original had what looked like a bug, whereby a bright light path could become trapped inside the glass sphere. The Russian Roulette termination would not terminate the path as the path brightness was too high, which eventually leads to a stack overflow. ScalaPT addresses this by increasing the chance of termination as the call stack depth increases.
* Infinite, one-way planes are used in place of giant spheres for the box walls.
* Scene definitions can be read from a JSON file.
* Random number generation replaced with a State monad (which wraps an XorShift random number generator).

# Examples

## "RGB"

![RGB](https://github.com/jon-hanson/ScalaPT/blob/master/examples/rgb.png)

## "Horizon"

![Horizon](https://github.com/jon-hanson/ScalaPT/blob/master/examples/horizon.png)
