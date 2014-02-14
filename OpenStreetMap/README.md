OpenStreetMap
=============

### GPS Project based on OpenStreetMap

#### 1) put your "map.osm" in the "file" folder of the project

#### 2) osm2wkt/Osm2wkt
this step will get rid of the none-routable path, and please press "N" when it asked for fixCompleteness.
* read the data from OSM file
* get rid of unroutable data
* generate xxx.wkts file

#### 3) controller/OSMInputFileGeneration
* read the data from OSM file
* generate xxx_node.csv and xxx_way.csv from data

#### 4) controller/OSMOutputFileGeneration
* read the data from xxx_node.csv and xxx_way.csv
* according xxx.wkts, overwrite xxx_node.csv and xxx_way.csv

#### 5) controller/OSMDivideWayToEdge
* divide the long way to seperate edges based on intersect

#### 6) controller/OSMGenerateKML(Optional)
* generate edge kml
* generate node kml

#### 7) controller/OSMGenerateAdjList
* generate xxx_adjlist.csv file

All the preprocessing steps (1~7) can be done in the controller/OSMGenerateAll.
   
#### 8) controller/OSMRouting
* using A* algorithm for path routing
* using A* algorithm with fibonacci heap for path routing
* using bidirectional hierarchy routing algorithm for path routing
* show turn by turn information
   
#### 9) test/CompareTdspTdspHTimeCost
* compare the response time and cost time between A* algorithm and hierarchy routing algorithm

#### 10) test/AnalyzeTravelTimeMatrix
* calculate the travel time based on matrix data
