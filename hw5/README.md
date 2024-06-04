# Assignment 5

### Usage

Run `DroneViewer`. This program accepts a single command-line argument. 
The argument is the path of an input configuration text file.
These configurations specify the position and orientation of the global camera.
The following configurations are provided: 
* `configurations/ymca.txt`
* `configurations/building.txt`

The program will load the respective scenegraph from resources. 

### View

There are 2 views rendered - one that is displayed with a stationary global camera and one that is displayed via a 
movable drone. Whichever camera is toggled will have its view displayed with the other camera's view in the top right 
corner. See toggling below.

### Controls

The drone camera is keyboard-controlled, and can be used to interactively fly around in the scene. 
This camera can also be “seen” from the global camera’s view point.

* "Space" : Toggle between global camera and drone camera
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
* "+" : Camera zooms in
* "-" : Camera zooms out

## Design of Animation

- _Which parts of the design were changed to support animation, and why?_

First, the drone model scenegraph was created.
It has 4 propellers, see `<transform name="propeller-1">` and so on.
These are represented by `TransformNode` in the `sgraph` package.
`TransformNode` has the field `animation_transform` which can be set with `setAnimationTransform(Matrix4f mat)`.
This animation transformation is used when drawing the propellers (or other transform nodes).
We can animate our scenegraph instance calling the `animate(float time)` function.
We've modified this function to load the the drone propeller nodes from the nodes map and then animate them based on 
the input time.
This is just a simple rotation.

- _What are the limitations/compromises in your design?_

There are some limitations that can be noted in our design.
Our drone is tightly coupled to our program. We load from a constant resource in `JOGLFrame` and configure
the drone position and direction in `View`. Ideally, a user should be able to configure these in the input configuration
but this was not a requirement for the assignment. When we animate the propellers in `Scenegraph`, our program searches
for the particular "propeller-1" and so on nodes that are defined by our drone model. This isn't great. If a user wants 
to use a different drone, this will likely break. Additionally, it's not very scalable should we want to introduce more
animations. There are many ways to approach this problem. That being said, this animation works and could be improved 
in future assignments to resolve the issue. 

- _If another developer uses your code to animate a different scene, how would they go about it? Be specific:
   which parts of the code would be changed for which purpose._
   
To animate a different scene, first define a different scene and add it to resources with a corresponding input 
configuration. Additionally, be sure to configure the drone (assuming they want it) in the view as they please. To 
animate parts of their scene, include whatever parts they want animated as transformation nodes. In the animate function 
for their scenegraph, load those nodes and set their animation transformations accordingly. 
