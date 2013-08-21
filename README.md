CleanPath
=========

Android GPS


Connect to Server
mstsc 

host: 128.125.163.86
user: clearp
pw: clearp


geodb.usc.edu
user: clearp
pw: clearp



TASK(August 20)
[X]We have been testing the your LA Monday, Tuesday,…Sunday   LA addlist files. The highways are coming very good. But it looks like  our speed estimations for Arterials  are  high. As you know we are using the speed_categories to find the  travel-time for Arterials. So to be more realistic, can you pls introduce %20 penalty to all arterial streets (funclass=3 and funcclass=4 edges) for all times (6AM to 9PM). Remember we had rush hour penalty   (%15), pls keep that  as it is, so you will have total of %35 (%20+%15) penalty for arterials rush hours, %20 for arterial non-rush hours.
Can you pls run the pattern ONLY for TUESDAY for 15 minutes?  Can you pls run this while you are working on OpenStreetsMap? Pls let me know when you start running the new Tuesday.
 

TASK(August 19)
[ ]https://www.gaia-gis.it/fossil/spatialite-tools/wiki?name=OSM+tools
[ ]https://www.gaia-gis.it/fossil/spatialite-tools/wiki?name=spatialite_osm_net
[ ]http://osgeo-org.1560.x6.nabble.com/Building-a-connected-graph-with-OSM-data-Splitting-LineStrings-at-their-intersections-td4323604.html
[ ]http://tm.kit.edu/~mayer/osm2wkt/ (linestrings)


TASK(August 16)
Realtime Update



TASK(August 16)
Finished importing Navteq RDF data  to our oracle database.  This is another format that I mentioned  previously but it is very similar to GN_LINKS and GN_NODES, except there is no TMC information 


NAVTEQRDF/NAVTEQRDF@gd2.usc.edu:1521:navteq 

[ ]1) Can you pls connect to this database and let me know if you can create adjclist from NAVTEQRDF schema? Similar to GN_LINKS and GN_NODES, there are links and nodes tables but with different names. 
[ ]2) Can you pls compare the link_ids in both  GN_LINKS and NAVTEQRDF.LINKS (don't remember the name now) table to see if the link_ids are same. If you we can use TMC table in the NAVTEQRDF schema as well. 



TASK(August 16)
[X]Shiva and I  are testing a new shortest path generation where we would like to use static weights for edges. Hence, we need to add static weight in the adjacency list generated, with (s) as the separator. New format of adjacency list will be as follows:
n1(v)1,2,2,3,4,5,2,2,..,1,(s),5;n2(f)10,(s)10;....
where, for example, (s),5  represent the static(=5) weight between n0 and n1 and (s)10 represent static(=10) weight between n0 and n2 
What will this static weight would come from? The answer is Table - tmc_monthly_5min
Column - avg_travel_time - this is in minutes as far as Shiva calculated, so pls convert to seconds
Please email me if you have any questions. Can you generate the new adjlist by Saturday morning? 


TASK(August 12)
[X]1) Do we have all 7days patterns for August patterns? 
[X]2) How long does it take to create one day pattern? 
[ ]3) Can you pls start creating September, October, November, December patterns. When creating these patterns pls exclude the vacation/holidays days (e.g. Labor Day, thanksging and all others)  so that averages are good. For example, lets assume that  september 3 (Tuesday) is labor day, when creating Tuesday pattern in Septemeber you will only use 3 Tuesdays(excluding Sept 3).  Also pls note that you will generate a "labor day" pattern only using September 3 data.  It will same for all other holidays. 



Bug(August 6):
[X]more than year (Check)
[X]133(280718563) to 1159710(83891384)


link from:274044(886723024) to:1677518(886723033)


TASK(August 6)
ok can you pls start early then. So that you can test the DatalistGeneration.java as soon as possible
When you run DatalistGeneration.java  with CA_AdjList_Weekday.txt, pls record the following and let me know 

[X]a) How much memory you consume? 
[X]b) How long does it take to load the  CA_AdjList_Weekday.txt in to memory? with (ie BuildList.java)
[X]c) What is the average response time of tdsp_2() for random 100 selected source and destination and departure time 

We will record above items for 

[X] 1)   edges weights for every 5 minutes with each weight is rounded integer
[X] 2)   edges weights for every 15 minutes with each weight is rounded integer 
[X] 3)   edges weights for every 15 minutes with each weight is rounded integer and funclass=5 edges only have one values instead of 60. 

Pls send me the results incrementally. For example send me the results when you finish 1a) ,1b),1c). 


TASK (August 5)
[X]1) Can you pls generate snapshots (picture) from for August Tuesday patterns for the following times,  7:30,8:00,8:30. 9:00 AM and  4:30, 5:00,5:30,6:00PM. 
I will generate GOOGLE images myself and compare them with yours. Can you pls send me these snapshots today by 9pm? 



TASK (August 4)
[X]1) Lets create for every (five) 5 minutes this time. But pls make your code flexible to generate for every 15 minutes as well

[X]2) pls generate weights (traveltimes) as rounded seconds (no decimal) instead of milisecond in the code. Pls make sure that there is no zero travel time either if it rounds to zero (0)  make it 1
For example 1.3 seconds-->1 second, 1.9 second-->2 second,  0.4 second -->1 second, 0.1 second -->1 second. 



TASK (August 2)

[X]1) Pls connect to following database using gndemo/gndemo username/password. We will generate California adjlist using the tables here.

GNDEMO/GNDEMO@gd2.usc.edu:1521:navteq
[X]2) You will see gn_links, gn_nodes, tmc_monthly_5min. The tables gn_links, gn_nodes cover entire California. Pls use these two tables to create  adjlist for california. 
[X]3) The tmc_monthly_5min table includes  the weight of each edge (in gn_link) for every 5 minutes. 
The relationship between tmc_monthly_5min and GN_LINKS is as follows.
GN_LINKS table contains TMC_CODE corresponding to TMC_PATH_ID  column in tmc_monthly_5min table (so you will join 
these two tables based on GN_LINKS.TMC_CODE=tmc_monthly_5min.tmc_path_id). 

One trick in gn_links is that you need to translate  
N and P in TMC_CODE  into "-", "+" to get  the tmc_path_id in tmc_monthly_5min table. 
For example, 
select tmc_code  from gn_links where link_id=932502154  -->  "106N05070" 
To get the speed of this edge in tmc_monthly_5min  table 
you need to select * from tmc_monthly_5min where tmc_path_id like '106-05070' and  month=X and weekday=X (as you can see 106N05070 --> 106-05070)

[X]4) There is also one trick in tmc_monthly_5min table. That is "minute=0" is midnight in UTC. Sorry that is how Navteq guys did it :(.
So, you will have to shift the minute by 7 or 8  hours (? dont know exact shift pls check).  
For example, If you look at minute=420 (i.e., 07:00 UTC = midnight PDT) then this TMC has an average speed ~100km/h as expected.

[X]5) You don't need to worry about zlevels becuause zlevels are already taken care of in nodes table. Also don't worry about 
unfolding the curvy edges.  Bidirectional links are also  split into into two, by using minus (-) infort of the link_id (e.g,
link_id=77 and link_id=-77 represent the exact same edge but different directions). 
So pls only use gn_links and gn_nodes tables without any special care. 

Can you pls create adjlist for entire California and send me by Sunday 5Pm? Pls let me know when you read this email and  if you have any questions.

P.S: Sorry for the long email, I wanted to meet and explain these but I just finished creating these tables and understanding the relationship between them. 


TASK (August 1)
[X]1) Create August all day Adjlist
[X]2) Different Data Source
[X]3) Create Adjlist for CA
[X]4) Create (f) fixed for only fun_class=5








AdjList Create Process
1) GetAverageSpeedForArterials1to5New
connect to DB:
static String url_home = "jdbc:oracle:thin:@geodb.usc.edu:1521/geodbs";
static String userName = "clearp";
static String password = "clearp";
static Connection connHome = null;

change query:
String sql = "SELECT avg(t2.SPEED) FROM arterial_Averages3_full3 T2 where month = 'May' GROUP BY  TIME ORDER BY TIME";

2) GetAverageSpeedForHighways

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



5) CreateAdjList1to5New
private static String [] days = {"Monday"}; 



TASK (July 26)
[X]1) ding.highway_average_cube pattern
[X]2) Oracle stored procedure Using Java
          SELECT * FROM highway_averages3_new4
          SELECT * FROM DING.highway_averages3_cube
[X]3)  investigating the patterns for I-10 and 405. Pls look into raw data and let me know.  

Hi Jiayun, 
Pls see our color coded path for source=Santa Monica and Destination=USC. As you can see we show GREEN in the begining of the path (just before the first orange line going from Santa Monica to USC). But pls check Google maps that are we show green is colored with red and black (ie. less than 10 m/h). I think we should have atleast show yellow of the same region.

Yu
Can you pls look into pattern of the regions I mentioned above (in addition to 405) we have been discussing. Pls let me know why we are showing high speed in those regions. I would love to find the reason it will correct everything all of a sudden.  Pls let me know you comments by tomorrow morning? 

Ugur 

Sensor: 759257, 737229, 737237, 737246, 737257






TASK (July 20)
[X]1) AdjList_Monday.txt pattern
[X]2) Oracle stored procedure Using Java

See the note from Yu below " PS:I found some speed value in highway_averages3_new4 are 0, run "SELECT COUNT(*) FROM highway_averages3_new4 WHERE speed=0" in database, the result is 158429."
I think you were cleaning (assigning values to these sensors). Can you explain Yu how to do that?. 

Now I beleive that we should really correct our pattern generation module.  To do so, we can write a  Oracle stored procedure, which can scan the data in highway_averages3 table and fill in the missing values and update the 0 speeds.  You know the logic about this. Then, your pattern generation Java code can ready everthing from one solid table. Can you pls explain Yu how to fill the missing and 0 values.   

Basically, for a given sensor, we look into past data (same time, same month, same day etc) and generate an approximate value to replace with 0 (or missing - non existing- data). 
Can you pls work on the Oracle stored procedure to do so. Pls let me know when you are done. 



Can you connect to server below. Pls use same username and password (clearp/clearp).  if you cannot connect using Clearp user, pls use (username/password: ding / rth323). 
You will see that higway_congestion_config table is there. 

Pls look at higway_congestion_config  carefully you may see more sensors  (I dont remember exactly howmany) 



server name:          gd.usc.edu
server port:            1521
SID:                      adms




TASK (July 20)
Yu this is very excellent analysis. Pls keep up your good work! Ok here are my comments 

[X]1) Can you pls use the following color codes 

 For highways:
 Green: more than 50 mph
 Yellow:  between 25 and 50 mph.
 Red :  between 10 and 25 mph.
 Black: less than 10 mph

 For arterials:
 Red: less than 15 mph
 Yellow: between 15 and 25 mph
 Green: more than 25 mph

[X]2) As you can see from both pictures, our patterns and google pattern are quite different on 405 (I think 405-South) between  South Valley and Santa Monica. 

[X]3) Can you use DING.HIGHWAY_AVERAGES3_CUBE  (DING is schema name) table to generate the new pattern with above color codes. 



TASK (July 19)
[X]1) highway_sensor_close
[X]2) color 0-10 black 10-25 red 25-50 yellow 50- green


TASK (July 18)
1) I-10, CA-91, I-110 Edge Sensor KML (complete)

2) 看下那个老的navteq里面是在哪里确定一个城市的范围的，比如说咱们要生成LA的边 就要现在数据库中找出哪些edges是属于LA的，一边来说可能会有一个城市范围的边界连线什么的 这样我们就可以在这个区间里面选了 你看下这个东西在哪里 (complete)


TASK (July 10)

1) Highway KML (compete)
3) Sensor KML (compete)
4) Highway Pattern (compete)
5) Color (compete)
6) pls note that that  we want to see the patterns of the edges (as a chart) on the map. You can visualize the pattern on a pop up screen after clicking on the edge.  This will tell us what we are doing good or bad.  (compete)



FIGUEROA
startNode=49250387 (34.01404, -118.28284)

EndNode=49260081 (34.05835, -118.2513)

Distance = 3.603165092802251 mile
20 minutes | 10.8 mph
15 minutes | 14.4 mph
10 minutes | 21.6 mph


TASK (July 2)
1) Pattern Chart (Completed)


distance的单位是mile 英里
然后pattern里面的值是用 distance/speed*60*60*1000 的得到的毫秒  speed的单位是mile per hour
你算速度就用这个公式换算就行了
z_level可以不同  比如地势由起伏的地方或者你从桥上下来 z_level就会不一样



TASK (May 30)
1) Connect DB (Completed)
2) Filter Sensors (Completed)
3) Filter Edges (Completed)
4) Match Sensors to Edges (Completed)
5) Pattern (Completed)
6) Show in Google Map (Completed)


TIP
The geom format stored in database, KML (longitude, latitude)
And format in Google Map (latitude, longitude)





FAQ
Q: How to manager node_id?

A: using Nodes_G_12345.csv
Q: link_id "24009477" why 1 in database 8 in Edges.csv?

TIP
Direction:     North:0     South:1     East:2     West:3

TASK Go through Code
GetAverageSpeedForArterials.java (completed)
CreateListForArterials1to5New.java (completed)
GetAverageSpeedForHighways.java (completed)
CreateListForHighways1to5New.java (completed)
CreateAdjList1to5New.java (completed)

TASK (May 16)
1) Set up environment (completed)
2) Run the core and generate pattern (completed)
3) Update Arterial Pattern (test  Figuerao) (completed)
4) Update Highway Pattern (completed)
5) Real time update.
