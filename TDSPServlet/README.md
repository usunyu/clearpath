TDSPServlet
===========

### Turn by turn Information
#### Servlet URL:
	http://128.125.163.86/TDSP_Servlet/TDSPQuerySuper6?start=<startLatitude,startLongitude> &end=<endLatitude, endLongitude>&time=<timeIndex>&update=<if arrival then false1, if departure_mode then false2>&day=<weekday>&Carpool=True
#### Example:
	http://128.125.163.86/TDSP_Servlet/TDSPQuerySuper6?start=33.7,-118.40406&end=34.0487,- 118.24&time=30&update=False1&day=Tuesday&Carpool=True
where,
* update: defines whether you want to make arrive by query or depart at query.

		update = false1 : depart at query
		update = false2 : arrive by query

* carpool: carpool mode is on/off, based on that value is set to be as True/False respectively.
* timeIndex: for 6:00 AM it is 0(Zero) and incremented by 1 every 15 minutes from 6:00 AM, so

		timeIndex = 0 for 6:00AM
		timeIndex= 1 for 6:15AM, and so on
		The max value of time can is 60 which is equivalent to 9:00PM.

### Output of Servlet
	33.7,-118.40406;33.73915,-118.39862;33.73947,-118.39838;.....................;34.04849,- 118.24213;34.0487,-118.24;44.399766666666665-348620-82468-30-
	32.36102821065472@<street_information> $0;
* Contains a pair of latitude-longitude, each two such set makes a path segment.
* The highlighted part just before @ contains information about TRAVEL_TIME, START_NODE, END_NODE, TOTAL_DISTANCE respectively.

### Street Information
* It is just after @
* Direction, streetname, miles (they all separate by ","), which means that turn "direction" onto "steetnames" and then move "miles" miles.
* NORTH,Orchard Ave,-118.39861,33.73907,0.32
* <Direction, Street_name, turn_point_longitude, turn_point_latitude, miles_to_go_on_street>
* For First Street direction can be NORTH, SOUTH, EAST or WEST.
* For next streets direction

###### R--represents turn right
	R,W SUMMERLAND AVE,-118.30959,33.747,0.97
###### SR--represents turn slight right
	SR,E 4th STREET,-118.24722,34.04808,0.29
###### L--represents turn left
###### SL--represents turn slight left
###### F---represents move forward.
###### M-VLR--represents Move Via Left Ramp, occurs when we prepare to getonto the highway
###### EXIT- represents exit from the freeway
###### HE(distance) ---- which is used when we move from one highway to another highway without going through other arterials.
###### Merge--represents Merge onto ongoing road
#### Example:
	NORTH,ORCHARD AVE,-118.39861,33.73907,0.32,R,PALOS VERDES DR S,- 118.39648,33.74297,4.35,SL,W 25th STREET,-118.32981,33.72749,1.04,L,S WESTERN AVE,-118.31287,33.72296,1.99,R,W SUMMERLAND AVE,-118.30959,33.747,0.97,R,N GAFFEY STREET,-118.29285,33.74767,0.12,(S)--118.29231-33.74599-L-0.09-SR,I- 110,-118.29172,33.74712,20.66,SL,PASADENA FREEWAY,- 118.27393,34.03821,0.89,SR,22 B,-118.26581,34.04832,0.10,(S)--118.26421- 34.049-SL-0.65-L,W 4th STREET,-118.25603,34.05386,0.07,(S)--118.25502- 34.05321-SR-0.07-SL,W 4th STREET,-118.25421,34.05241,0.03,(S)--118.25378- 34.05211-SL-0.08-SR,W 4th STREET,-118.25255,34.05156,0.39,SR,E 4th STREET,- 118.24722,34.04808,0.29,L,S SAN PEDRO STREET,-118.243,34.04582, 0.25

### Translation:
* Head North on ORCHARD AVE and go for 0.32 miles.
* Turn Right onto PALOS VERDES DR S and go for 4.35 miles, and so on.

### Accident Information
* Just after the $ it contains accident information, including latitude, longitude, accident_info, backlog on road due to accident(in miles) etc.
