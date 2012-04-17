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

Bifocals is available from clojars. Add `[bifocals 0.0.1]` to your project.clj
then `(:require [bifocals.core :as bifocals)` in your `ns` statement.

There are some well-commented examples of using bifocals in the `examples`
folder. Additionally, all the functions and vars in the core namespace have
docstrings.

## License

Copyright (C) 2012 Dan Lidral-Porter

The Java wrapper bifocals uses to do communicate with OpenNI is a slightly
modified [SimpleOpenNI][3], which is (C) 2011 Max Rheiner / Interaction Design
Zhdk.

[3]: http://code.google.com/p/simple-openni/

Distributed under the GNU General Public License, v3. See LICENSE for full text.
