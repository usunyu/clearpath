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
connect to DB:
static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
static String userName = "clearp";
static String password = "clearp";
static Connection connHome = null;

change query:
String sql = "SELECT avg(t2.SPEED) FROM arterial_Averages3_full3 T2 where month = 'May' GROUP BY  TIME ORDER BY TIME";

2) GetAverageSpeedForHighways
change query:
String sql = "SELECT avg(t2.SPEED) FROM highway_averages_august_clean T2 where month = 'August' GROUP BY  TIME ORDER BY TIME"; 


3) CreateListForArterials1to5New
change sensor number:
static int numElem = 4575;

connect to DB:
static String url_home = "jdbc:oracle:thin:@gd.usc.edu:1521:adms";
static String userName = "clearp";
static String password = "clearp";
static Connection connHome = null;

change query
if(day.equals("All"))
     sql = "select avg(speed) from arterial_averages3_full where link_id="+LinkIds[i]+" group by time order by time";
else
     sql = "select speed from arterial_averages3_full3 where day='"+day+"' and month = 'May' and link_id= '"+LinkIds[i]+"' order by time";  

4) CreateListForHighways1to5New
connect to DB:
static String url_home = "jdbc:oracle:thin:@gd.usc.edu:1521/ADMS";
static String userName = "DING";
static String password = "rth323";



sql = "select speed from HIGHWAY_AVERAGES_AUGUST_CLEAN where day='" + day
                              + "' and month = 'August' and link_id= '" + LinkIds[i]
                              + "' order by time";



5) CreateAdjList1to5New
private static String [] days = {"Monday"}; 