# Assignment 4

## Running the Program

Run `DroneViewer`. This program accepts a single command-line argument. 
The argument is the path of an input configuration text file.
These configurations specify the position and orientation of the global camera.
The following configurations are provided: 
* `configurations/ymca.txt`
* `configurations/building.txt`

The program will load the respective scenegraph from resources. 

## View
There are 2 scenes rendered - one that is displayed with a stationary global camera and one that is displayed via a movable drone. Whichever camera is toggled will have its view displayed with the other camera's view in the top right corner. See toggling below.

## User Controls

The drone camera is keyboard-controlled, and can be used to interactively fly around in the scene. This camera can also be “seen” from the global camera’s view point.

* "Space" : toggle between main camera and drone camera
* "up" : Camera moves up
* "down" : Camera moves down
* "right" : Camera moves to the right
* "left" : Camera moves to the left
* "a" : Camera turns left in place
* "d" : Camera turns right in place
* "w" : Camera turns in place
* "s" : Camera turns down in place
* "f" : Camera tilts right in place
* "c" : Camera tilts left in place

### Drone Camera
* The red vector is the camera up vector.
* The blue vector is the camera right vector.
* The green vector is the vector opposite the camera's gaze.
