== ClearPath

== Manual

Open Street Map Process:

please make sure osm2wkt and OpenStreetMap project in the same folder.

In osm2wkt Project:

1) put your "map.osm" in the osm2wkt/file folder

2) osm2wkt /Osm2wkt, this step will get rid of the none-routable path, and please press No when it asked for fixCompleteness.

In OpenStreetMap Project:

3) put your "map.osm" in the OpenStreetMap/file

4) input/OSMInputFileGeneration

5) output/OSMOutputFileGeneration

6) (Optional) kml/OSMGenerateKMLMap and kml/OSMGenerateKMLNode to generate kml

7) output/OSMDivideWayToEdge

8) adjlist/OSMGenerateAdjList

9) path/OSMRouting for path routing


CA GNDEMO Process:

In PatternGeneration Project:

1) input/CAInputFileGeneration

2) (Optional) output/CAOutputKMLGeneration to generate kml

3) pattern/CAAdjListPattern


Clean Data Process:

In PatternGeneration Project:

1) input/InputFileGeneration

     writeAverageCube
     
     readAverageCube(i, 0);
     
     changeInterval();
     
     writeAverage15Cube(i, 0);
     
     renameAverageFile(i, 0);
     
2) process/DataClean

3) output/OutputDatabaseGeneration


AdjList Create Process:

In ArterialPatternGeneration_shireesh Project:

1) GetAverageSpeedForArterials1to5New

2) GetAverageSpeedForHighways

3) CreateListForArterials1to5New

4) CreateListForHighways1to5New

5) CreateAdjList1to5New
