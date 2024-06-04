# CS 4300 Homework 3

## Usage

Run `ScenegraphViewer`. 
The program accepts a single command-line argument. 
This argument is the path of an “input configuration” text file.
The following configurations are provided:
- `configurations/ymca.txt` (Shows 4 humanoids doing YMCA dance)
- `configurations/building.txt` (Shows simple building)

The program will load the respective scenegraph from resources.

### Configuration File

#### Format
```
Scenegraph Filename
Camera x Position
Camera y Position
Camera z Position
Camera x Look-at Position
Camera y Look-at Position
Camera z Look-at Position
Camera x Up Position
Camera y Up Position
Camera z Up Position
```
