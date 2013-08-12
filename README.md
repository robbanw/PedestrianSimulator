PedestrianSimulator v.1.00
==========================
By Robert Wideberg & Christoffer Wiss


--Assets used--
* Style-7 font by Sizenko Alexander
* Icons from www.sjjb.co.uk/mapicons/


--Introduction--

Welcome to the Pedestrian Simulator, a Java application that simulates pedestrian behavior in urban environments.
It was created as a part of our Bachelor thesis project at KTH (Spring 2013). 

Features:
* Pedestrian types (student = blue, professor = cyan, civilian = magenta) which determines schedule activities as well as color.
* Schedule system which determines when a pedestrian should enter and exit the map as well as type-specific activities.
* Needs system which determines where a pedestrian should head (during non-scheduled time).
* Ability to fast-forward through the simulation.
* Sleek GUI which allows the user to control the simulator at ease.
* Toggleable target lines (will show where pedestrians are currently heading).
* Cost filter - shows the path cost of the various map components (used in the pedestrians pathfinding).
* Heat filter - shows how crowded the different areas of the map are.

If you are interested in learning more about the simulator you can check out the thesis at the following link: 
http://www.csc.kth.se/utbildning/kth/kurser/DD143X/dkand13/Group9Petter/report/Robert.Wideberg.Christoffer.Wiss.report.pdf 


--Download & Installation--

Windows:
Download the Pedestrian Simulator from: 
http://www.csc.kth.se/~robwid/pedestriansimulator/PedestrianSimulator_v1.00.zip.
Save the .zip file to disk and then extract it. Launch the PedestrianSimulator.exe file to start the application.

Linux & Mac:
Download the Pedestrian Simulator from: 
http://www.csc.kth.se/~robwid/pedestriansimulator/PedestrianSimulator_jar_v1.00.tar.gz.
Save the compressed file to disk and then extract it. Run the supplied .jar file with the command java -jar PedestrianSimulator.jar.

NOTE: If you have a java version older than 1.6.0_21 you will need to recompile the .java files in the /src/ directory and create a new .jar file.
Linux users can run the MakeNewJar.sh file by typing sh MakeNewJar.sh to create a new jar.
There is no such script file for Mac users but they can study MakeNewJar.sh and replace the Linux specific syntax with the appropriate Mac syntax. 


--Startup guide--

1. Load a .OSM map file. This can be downloaded from http://www.openstreetmap.org/ by navigating to an area of interest
   and then use the export option.
2. Pedestrians are added to the map and are each given an individual schedule.
3. Select a pedestrian by clicking on it with the left mouse button (easiest done after pausing the simulator).
   A green box now appears around the pedestrian to indicate that it is selected. You can now follow its needs on 
   the panel to the right.
4. Deselect the pedestrian by clicking the left mouse button anywhere else on the map.


--Hotkeys--

UP    - Sets simulation speed to max. (64x).
DOWN  - Sets simulation speed to min. (1x)
LEFT  - Decreases simulation speed (1/2x of current speed).
RIGHT - Increases simulation speed (2x of current speed).
SPACE - Pauses/Unpauses the simulation.

Q - Toggles anti-aliasing (i.e. smoothening of edges, untoggle anti-aliasing if the simulator runs poorly).
T - Toggles target lines for all pedestrians.
C - Sets the path cost filter as the current active filter (shows the path cost of the various map components, used in the pedestrians' pathfinding).
D - Sets the heatmap filter as the current active filter (shows how crowded the different areas of the map are).


--Known problems--

Linux (Ubuntu):
* Java windows are sometimes resized to a very small or weird size on startup. 
- This behavior seem to only happen on Ubuntu systems. Simply restart the program when this happens.
