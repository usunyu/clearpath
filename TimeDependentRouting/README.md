TimeDependentRouting
====================

### Performance Time Dependent Routing for large amount data

#### 1) Prepare
* Import the project into Eclipse;
* In the global/OSMParam line 113, set the name you like;
* In order to generate full week's pattern need 4 files: node, edge(new format), old edge(original format), pattern(what you provide us) file;
* For example, you set the name as "osm", please rename those 4 file as: "osm_node.csv", "osm_edge.csv", "osm_edge_old.csv" and "osm_pattern.csv";
* Put all those files in the "file" folder in the project.

#### 2) Generate adjList
* Run main/GenerateAdjList.

#### 3) Generate JAR file
* Refresh the project;
* File -> Export... -> Java -> Runnable JAR file;
* Select TimeDependentRouting as Launch configuration, chose and input Export destination;
* Finish.

#### 4) Time Dependent Routing
* Put the location.csv file in the same directory as TimeDependentRouting.jar;
* Run the program by command such as "java -jar -Xms512m -Xmx8192m TimeDependentRouting.jar 1 location.csv result.csv Monday".

### Usage of JAR file

#### 1. Generate AdjList:
* GenerateAdjList.jar include two files in the "file" folder:
* 1) osm_edge_old.csv, which is the old format of edge files;
* 2) osm_pattern.csv, which is the pattern you provide us. Please replace them as you need but follow the same file name.

* After replace the files, please run the program by the command java -jar -Xms512m -Xmx8192m GenerateAdjList.jar. And it will generate osm_adjlist.csv file.

* Download the GenerateAdjList.jar from: https://drive.google.com/file/d/0B2Hdm6Qw4iDoRkNPWVFkTzVtNTg/edit?usp=sharing

#### 2. Time Dependent Routing:
* TimeDependentRouting.jar include three files in the "file" folder:
* 1) osm_edge.csv, which is new format of edge with negative edge id;
* 2) osm_node.csv, which is node file with lat and lon;
* 3) osm_adjlist.csv, which is adjacency list with one week's pattern, replace it with the file generated from first step.

* Then run the program by the command such as java -jar -Xms512m -Xmx8192m TimeDependentRouting.jar 1 location.csv result.csv Monday. And it will generate a report file.

* Download the TimeDependentRouting.jar from: https://drive.google.com/file/d/0B2Hdm6Qw4iDobVlEMGw1T0lOcGM/edit?usp=sharing
