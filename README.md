# bifocals

A kinect library for quil.

## External Dependencies

Bifocals relies on the OpenNI drivers and middleware for kinect, which must be
installed seperately. See the SimpleOpenNI [installation instructions][1] for
more info.

You must have a kinect plugged in to your computer. This may require a [usb
adapter][2] for your kinect.

[1]: http://code.google.com/p/simple-openni/wiki/Installation
[2]: http://duckduckgo.com/?q=kinect+usb+adapter

## Usage

Bifocals is available from clojars. Add `[bifocals "0.0.2"]` to your project.clj
then `(:require [bifocals.core :as bifocals)` in your `ns` statement.

There are some well-commented examples of using bifocals in the examples
folder. To run the examples from this repo, first run `lein compile` to compile
the java wrapper. Then fire up a REPL (`lein repl`) and type, e.g.,
`(use 'bifocals.examples.2D)`.

All public functions and vars in the core namespace have docstrings (generated
documentation forthcoming: having some issues with autodoc and classpaths).

## License

Copyright (C) 2012 Dan Lidral-Porter

The Java wrapper bifocals uses to communicate with OpenNI is a slightly modified
[SimpleOpenNI][3], which is (C) 2011 Max Rheiner / Interaction Design Zhdk.

[3]: http://code.google.com/p/simple-openni/

Distributed under the GNU General Public License, v3. See LICENSE for full text.
