package tdsp;

import java.io.*;
import java.text.*;
import java.util.*;

import library.*;

import objects.*;
import function.*;

public class RDFTdspGeneration {

	/**
	 * @param file
	 */
	static String root 				= "file";
	static String adjListFile		= "RDF_AdjList.csv";
	static String pathKMLFile 		= "RDF_Path.kml";
	/**
	 * @param args
	 */
	static long startNode 		= 49304020;
	static long endNode 		= 958285311;
	static int startTime 		= 10;
	static int timeInterval 	= 15;
	static int timeRange 		= 60;
	static String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
	static String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
	static String SEPARATION		= ",";
	static String VERTICAL		= "|";
	static String COLON			= ":";
	static String SEMICOLON		= ";";
	static String UNKNOWN 			= "Unknown Street";
	static String YES				= "Y";
	static String NO				= "N";
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param graph
	 */
	static HashMap<Long, ArrayList<RDFToNodeInfo>> adjListMap = new HashMap<Long, ArrayList<RDFToNodeInfo>>();
	/**
	 * @param route
	 */
	static HashMap<String, RDFLinkInfo> nodeToLink = new HashMap<String, RDFLinkInfo>();
	/**
	 * @param path
	 */
	static ArrayList<Long> pathNodeList = new ArrayList<Long>();
	/**
	 * @param sign
	 * <signId, signObject>
	 */
	static HashMap<Long, RDFSignInfo> signMap = new HashMap<Long, RDFSignInfo>();
	
	public static void main(String[] args) {
		// read node
		RDFInput.readNodeFile(nodeMap);
		prepareRoute();
		// read link
		RDFInput.readLinkFile(linkMap, nodeMap, nodeToLink);
		RDFInput.readLinkName(linkMap);
		RDFInput.readLinkGeometry(linkMap);
		RDFInput.readLinkLane(linkMap);
		// read sign
		RDFInput.fetchSignOrigin(linkMap, signMap);
		RDFInput.fetchSignDest(linkMap, signMap);
		RDFInput.fetchSignElement(signMap);
		
		buildAdjList(0);
		tdspGreedy(startNode, endNode, startTime);
		//tdspAStar(startNode, endNode, startTime);
		RDFOutput.generatePathKML(pathNodeList, nodeToLink);
		turnByTurn();
	}
	
	public static void prepareRoute() {
		System.out.println("preparing route...");
		for(RDFNodeInfo node : nodeMap.values()) {
			node.prepareRoute();
		}
	}
	
	/**
	 * @deprecated we use base name instead
	 * @param stName
	 * @return
	 */
	private static String getUniformSTName(String stName) {
		String[] namePart = stName.split(";");
	 	int index = 0;
	 	boolean find = false;
		
	 	if(namePart.length > 1) {
	 		for(; index < namePart.length; index++) {
	 			for(int i = 0; i < namePart[index].length(); i++) {
	 				if(Character.isDigit(namePart[index].charAt(i))) {
	 					find = true;
	 					break;
	 				}
	 			}
	 			if(find)
	 				break;
	 		}
	 	}
	 	if(index == namePart.length)
	 		stName = namePart[0];
	 	else
	 		stName = namePart[index];
	 	return stName;
	 }
	
	public static String getTurnText(int turn) {
		String turnStr = "";
		switch(turn) {
			case Geometry.LEFT: 
				turnStr = "Turn left ";
				break;
			case Geometry.RIGHT:
				turnStr = "Turn right ";
				break;
			case Geometry.SLIGHTLEFT:
				turnStr = "Take slight left ";
				break;
			case Geometry.SLIGHTRIGHT:
				turnStr = "Take slight right ";
				break;
			case Geometry.SHARPLEFT:
				turnStr = "Take sharp  left ";
				break;
			case Geometry.SHARPRIGHT:
				turnStr = "Take sharp right ";
				break;
			case Geometry.UTURN:
				turnStr = "Take u-turn ";
				break;
		}
		return turnStr;
	}
	
	public static boolean isArterialClass(int functionalClass) {
		if(functionalClass >= 3 && functionalClass <= 5)
			return true;
		else
			return false;
	}
	
	public static boolean isHighwayClass(int functionalClass) {
		if(functionalClass >= 1 && functionalClass <= 2)
			return true;
		else
			return false;
	}
	
	public static RDFSignInfo getTargetSign(long linkId, LinkedList<RDFSignInfo> signList) {
		RDFSignInfo targetSign = null;
		if(signList == null)
			return targetSign;
		for(RDFSignInfo sign : signList) {
			if(sign.containDestLinkId(linkId)) {
				targetSign = sign;
				break;
			}
		}
		return targetSign;
	}
	
	public static boolean containText(LinkedList<RDFSignElemInfo> signElemList, String text) {
		for(RDFSignElemInfo elem : signElemList) {
			if(elem.getText().equals(text)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * get the routing elem form the sign dest object
	 * @param signDest
	 * @return
	 */
	public static LinkedList<RDFSignElemInfo> getSignElem(RDFSignDestInfo signDest) {
		LinkedList<RDFSignElemInfo> signElemList = null;
		ArrayList<RDFSignElemInfo> signElemArray = signDest.getSignElemList();
		for(RDFSignElemInfo signElem : signElemArray) {
			if(signElem.getTextType().equals("R")) {
				if(signElemList == null) {
					signElemList = new LinkedList<RDFSignElemInfo>();
				}
				signElemList.add(signElem);
			}
		}
		return signElemList;
	}
	
	public static String getTowardText(RDFSignDestInfo signDest) {
		String towardText = null;
		LinkedList<RDFSignElemInfo> towardElemList = getTowardElem(signDest);
		if(towardElemList != null && towardElemList.size() == 1) {	// only have one, we use it
			towardText = towardElemList.getFirst().getText();
		}
		return towardText;
	}
	
	/**
	 * get the toward elem form the sign dest object
	 * @param signDest
	 * @return
	 */
	public static LinkedList<RDFSignElemInfo> getTowardElem(RDFSignDestInfo signDest) {
		LinkedList<RDFSignElemInfo> towardElemList = null;
		ArrayList<RDFSignElemInfo> signElemArray = signDest.getSignElemList();
		for(RDFSignElemInfo signElem : signElemArray) {
			if(signElem.getTextType().equals("T")) {
				if(towardElemList == null) {
					towardElemList = new LinkedList<RDFSignElemInfo>();
				}
				towardElemList.add(signElem);
			}
		}
		return towardElemList;
	}
	
	public static String getDirectionStr(LinkedList<Integer> dirList) {
		if(dirList == null) {
			return null;
		}
		String dirStr = null;
		if(dirList.size() == 1) {	// one way
			for(int dir : dirList) {
				switch(dir) {
				case Geometry.EAST:
					dirStr = "E";
					break;
				case Geometry.NORTH:
					dirStr = "N";
					break;
				case Geometry.SOUTH:
					dirStr = "S";
					break;
				case Geometry.WEST:
					dirStr = "W";
					break;
				}
			}
		}
		return dirStr;
	}
	
	/**
	 * search forward to find if the road name contained in the available sign elem, if so, chose that for routing
	 * if the set only contain one elem, we chose it
	 * @param currentIndex
	 * @param signElemList
	 * @return
	 */
	public static RDFSignElemInfo searchPathSign(int currentIndex, LinkedList<RDFSignElemInfo> signElemList) {
		RDFSignElemInfo signElem = null;
		// only one sign available
		if(signElemList.size() == 1) {
			signElem = signElemList.getFirst();
		}
		else {	// search through the path
			long preNodeId = -1;
			for(int i = currentIndex; i < pathNodeList.size(); i++) {
				if(i == currentIndex) {
					preNodeId = pathNodeList.get(i);
					continue;
				}
				long curNodeId = pathNodeList.get(i);
				String nodeStr = preNodeId + "," + curNodeId;
				RDFLinkInfo link = nodeToLink.get(nodeStr);
				String baseName = link.getBaseName();
				// check base name
				for(RDFSignElemInfo elem : signElemList) {
					if(elem.getText().equals(baseName)) {
						if(!elem.getDirectionCode().equals("")) {
							// check direction
							String dirStr = getDirectionStr(link.getDirectionList());
							if(dirStr.equals(elem.getDirectionCode())) {
								signElem = elem;
								break;
							}
						}
						else {
							signElem = elem;
							break;
						}
					}
				}
				preNodeId = curNodeId;
			}
		}
		return signElem;
	}
	
	/**
	 * check if all the rest link's name is unknown, if so, use non-street-name routing
	 * @param currentIndex
	 * @return
	 */
	public static boolean checkRestAllUnknown(int currentIndex) {
		boolean restAllUnknown = true;
		long preNodeId = -1;
		for(int i = currentIndex; i < pathNodeList.size(); i++) {
			if(i == currentIndex) {
				preNodeId = pathNodeList.get(i);
				continue;
			}
			long curNodeId = pathNodeList.get(i);
			String nodeStr = preNodeId + "," + curNodeId;
			RDFLinkInfo link = nodeToLink.get(nodeStr);
			String baseName = link.getBaseName();
			if(!baseName.equals(UNKNOWN)) {
				restAllUnknown = false;
				break;
			}
			preNodeId = curNodeId;
		}
		return restAllUnknown;
	}
	
	public static void turnByTurn() {
		System.out.println("turn by turn...");
		int debug = 0;
		try {
			long preNodeId = -1;
			
			String preBaseName = "";
			String preStreetName = "";
			boolean preExitName = false;
			int preFunctionalClass = -1;
			int preDirIndex = -1;
			String lastRouteText = UNKNOWN;
			
			double distance = 0;
			boolean firstRoute = true;
			DecimalFormat df = new DecimalFormat("#0.0");
			
			boolean restAllUnknown = false;
			
			for(int i = 0; i < pathNodeList.size(); i++) {
				debug++;
				
				if(i == 0) {
					preNodeId = pathNodeList.get(i);
					continue;
				}
				
				long curNodeId = pathNodeList.get(i);
				String nodeStr = preNodeId + "," + curNodeId;
				RDFLinkInfo link = nodeToLink.get(nodeStr);
				
				long linkId = link.getLinkId();
				
				int functionalClass = link.getFunctionalClass();
				boolean exitName = link.isExitName();
				
				RDFNodeInfo preNode = nodeMap.get(preNodeId);
				RDFNodeInfo curNode = nodeMap.get(curNodeId);
				
				int curDirIndex = Geometry.getDirectionIndex(preNode.getLocation(), curNode.getLocation());
				
				String curBaseName = link.getBaseName();
				LinkedList<String> curStreetNameList = link.getStreetNameList();
				String curStreetName = curStreetNameList.getFirst();
				
				LinkedList<RDFSignInfo> signList = link.getSignList();
				
				if(i == 1) {	// initial prev
					preBaseName = curBaseName;
					preStreetName = curStreetName;
					preDirIndex = curDirIndex;
					preFunctionalClass = functionalClass;
				}
				
				// for arterial
				if(isArterialClass(preFunctionalClass) && isArterialClass(functionalClass)) {
					// sign table first
					RDFSignInfo sign = getTargetSign(linkId, signList);
					if(sign != null) {	// valid sign exist, take ramp on to highway
						RDFSignDestInfo signDest = sign.getSignDest(linkId);
						// get all the route sign elem available
						LinkedList<RDFSignElemInfo> signElemList = getSignElem(signDest);
						// search for the correct one
						RDFSignElemInfo signElem = searchPathSign(i, signElemList);
						// first route happen
						if(firstRoute) {
							System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStreetName + " toward " + curStreetName);
							firstRoute = false;
						}
						if(signElem != null) {	// find the target sign elem
							System.out.println( df.format(distance) + " miles");
							String signText = signElem.getText();
							String dirCode = signElem.getDirectionCode();
							String towardText = getTowardText(signDest);
							if(!dirCode.equals("")) {
								signText += " " + dirCode;
							}
							if(towardText != null) {
								signText += " toward " + towardText;
							}
							if(link.isRamp())
								signText = "Take ramp onto " + signText;
							else
								signText = "Merge onto " + signText;
							System.out.println(signText);
							distance = 0;
						}
					}
					else {	// check normal condition
						// pre or cur street has name and not exit name
						if((!preBaseName.equals(UNKNOWN) && !preExitName) || (!curBaseName.equals(UNKNOWN) && !exitName)) {
							// no turn need, cumulative distance
							if(Geometry.isSameDirection(curDirIndex, preDirIndex) && preBaseName.equals(curBaseName)) {
								
							}
							else if(!preBaseName.equals(curBaseName) && Geometry.isSameDirection(curDirIndex, preDirIndex)) {	// change road
								// first route happen
								if(firstRoute) {
									System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStreetName + " toward " + curStreetName);
									firstRoute = false;
								}
								if(!curStreetName.equals(UNKNOWN) && !exitName) {
									System.out.println( df.format(distance) + " miles");
									System.out.println("Merge onto " + curStreetName);
									distance = 0;
								}
							}
							else if(preBaseName.equals(curBaseName) && !Geometry.isSameDirection(curDirIndex, preDirIndex)) {	// change direction
								// first route happen
								if(firstRoute) {
									System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStreetName);
									firstRoute = false;
								}
								if(!curStreetName.equals(UNKNOWN) && !exitName) {
									System.out.println( df.format(distance) + " miles");
									int turn = Geometry.getTurn(preDirIndex, curDirIndex);
									String turnText = getTurnText(turn);
									turnText += "to stay on " + curStreetName;
									System.out.println(turnText);
									distance = 0;
								}
								else {
									if(restAllUnknown || (restAllUnknown = checkRestAllUnknown(i))) {	// all rest link has unknown name
										int turn = Geometry.getTurn(preDirIndex, curDirIndex);
										System.out.println( df.format(distance) + " miles");
										String turnText = getTurnText(turn);
										System.out.println(turnText);
										distance = 0;
									}
								}
							}
							else {	// change direction and road
								if(!curStreetName.equals(UNKNOWN) && !exitName) {
									// first route happen
									if(firstRoute) {
										System.out.println("Head " + Geometry.getDirectionStr(preDirIndex) + " on " + preStreetName + " toward " + curStreetName);
										firstRoute = false;
									}
									int turn = Geometry.getTurn(preDirIndex, curDirIndex);
									System.out.println( df.format(distance) + " miles");
									String turnText = getTurnText(turn);
									turnText += "onto " + curStreetName;
									System.out.println(turnText);
									distance = 0;
								}
								else {
									if(restAllUnknown || (restAllUnknown = checkRestAllUnknown(i))) {	// all rest link has unknown name
										int turn = Geometry.getTurn(preDirIndex, curDirIndex);
										System.out.println( df.format(distance) + " miles");
										String turnText = getTurnText(turn);
										System.out.println(turnText);
										distance = 0;
									}
								}
							}
						}
					}
				}
				// for highway
				if(isHighwayClass(preFunctionalClass) && isHighwayClass(functionalClass)) {
					// using sign table only
					RDFSignInfo sign = getTargetSign(linkId, signList);
					if(sign != null) {	// valid sign exist, take exit onto
						RDFSignDestInfo signDest = sign.getSignDest(linkId);
						
						// get all the route sign elem available
						LinkedList<RDFSignElemInfo> signElemList = getSignElem(signDest);
						
						// if the current name is contained in the available sign text, no need for routing
						if(!containText(signElemList, curBaseName)) {
							RDFSignElemInfo signElem = searchPathSign(i, signElemList);
							String signText = signElem.getText();
							// last route text is different from this one
							if(!signText.equals(lastRouteText)) {
								lastRouteText = signText;
								System.out.println( df.format(distance) + " miles");
								String dirCode = signElem.getDirectionCode();
								if(!dirCode.equals("")) {
									signText += " " + dirCode;
								}
								String towardText = getTowardText(signDest);
								if(towardText != null) {
									signText += " toward " + towardText;
								}
								if(signDest.getExitNumber() != null)
									signText = "Take the exit " + signDest.getExitNumber() + " on to " + signText;
								else
									signText = "Take the exit on to " + signText;
								System.out.println(signText);
								distance = 0;
							}
						}
					}
				}
				// from arterial to highway
				if(isArterialClass(preFunctionalClass) && isHighwayClass(functionalClass)) {
					//System.out.println("from arterial to highway");
				}
				// from highway to arterial
				if(isHighwayClass(preFunctionalClass) && isArterialClass(functionalClass)) {
					// using sign table
					RDFSignInfo sign = getTargetSign(linkId, signList);
					if(sign != null) {	// valid sign exist, take exit onto
						RDFSignDestInfo signDest = sign.getSignDest(linkId);
						
						LinkedList<RDFSignElemInfo> signElemList = getSignElem(signDest);

						System.out.println( df.format(distance) + " miles");
						/* here I just use the first text for simple, but may cause route problem */
						String signText = signElemList.getFirst().getText();
						if(signDest.getExitNumber() != null)
							signText = "Take the " + signText + " exit " + signDest.getExitNumber();
						else
							signText = "Take the " + signText + " exit";
						System.out.println(signText);
						distance = 0;
						// reset
						lastRouteText = UNKNOWN;
					}
				}
				distance += Geometry.calculateDistance(link.getPointList());
				
				// arrive destination
				if(i == pathNodeList.size() - 1) {
					if(distance > 0) {
						System.out.println( df.format(distance) + " miles");
						distance = 0;
					}
					System.out.println("Arrive destination");
				}
					
				preNodeId = curNodeId;
				preBaseName = curBaseName;
				preStreetName = curStreetName;
				preDirIndex = curDirIndex;
				preExitName = exitName;
				preFunctionalClass = functionalClass;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("turnByTurn: debug code " + debug);
		}
		System.out.println("turn by turn finish!");
	}
	
	public static void tdspGreedy(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		PriorityQueue<RDFNodeInfo> priorityQ = new PriorityQueue<RDFNodeInfo>(
				20, new Comparator<RDFNodeInfo>() {
			public int compare(RDFNodeInfo n1, RDFNodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		RDFNodeInfo current = nodeMap.get(startNode);	// get start node
		if(current == null) {
			System.err.println("cannot find start node, program exit!");
			System.exit(-1);
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		while ((current = priorityQ.poll()) != null) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode)	// find the end
				break;
			
			int timeIndex = startTime + current.getCost() / 60 / timeInterval;
			
			if (timeIndex > timeRange - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = timeRange - 1;
			
			ArrayList<RDFToNodeInfo> adjNodeList = adjListMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(RDFToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				RDFNodeInfo toNodeRoute = nodeMap.get(toNodeId);
				
				if(toNodeRoute.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeRoute.getCost()) {
					toNodeRoute.setCost(totalTime);
					toNodeRoute.setParentId(nodeId);
					priorityQ.offer(toNodeRoute);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeMap.get(current.getParentId());
				if(current == null) {
					System.err.println("cannot find intermediate node, program exit!");
					System.exit(-1);
				}
				pathNodeList.add(current.getNodeId());	// add intermediate node
			}
			
			if(current.getParentId() == -1) {
				System.err.println("cannot find the path, program exit!");
				System.exit(-1);
			}
			
			if(current.getParentId() == startNode)
				pathNodeList.add(startNode);	// add start node
			
			Collections.reverse(pathNodeList);	// reverse the path list
		}
		// prepare for next routing
		prepareRoute();
		System.out.println("find the path successful!");
	}
	
	public static void tdspAStar(long startNode, long endNode, int startTime) {
		System.out.println("start finding the path...");
		
		PriorityQueue<RDFNodeInfo> priorityQ = new PriorityQueue<RDFNodeInfo>(
				20, new Comparator<RDFNodeInfo>() {
			public int compare(RDFNodeInfo n1, RDFNodeInfo n2) {
				return n1.getCost() - n2.getCost();
			}
		});
		
		RDFNodeInfo current = nodeMap.get(startNode);	// get start node
		if(current == null) {
			System.err.println("cannot find start node, program exit!");
			System.exit(-1);
		}
		
		current.setCost(0);	// set start cost to 0
		
		priorityQ.offer(current);
		
		while ((current = priorityQ.poll()) != null) {
			long nodeId = current.getNodeId();
			
			if(nodeId == endNode)	// find the end
				break;
			
			int timeIndex = startTime + current.getCost() / 60 / timeInterval;
			
			if (timeIndex > timeRange - 1)	// time [6am - 9 pm], we regard times after 9pm as constant edge weights
				timeIndex = timeRange - 1;
			
			ArrayList<RDFToNodeInfo> adjNodeList = adjListMap.get(nodeId);
			if(adjNodeList == null)
				continue;
			
			int arrTime = current.getCost();
			
			for(RDFToNodeInfo toNode : adjNodeList) {
				long toNodeId = toNode.getNodeId();
				
				RDFNodeInfo toNodeRoute = nodeMap.get(toNodeId);
				
				if(toNodeRoute.isVisited())	// if the node is visited, we bypass it
					continue;
				
				int travelTime;
				if(toNode.isFix())	// fix time
					travelTime = toNode.getTravelTime();
				else	// fetch from time array
					travelTime = toNode.getTravelTimeArray()[timeIndex];
				
				// if we find a node with updated distance, just insert it to the priority queue
				// even we pop out another node with same id later, we know that it was visited and will ignore it
				int totalTime = arrTime + travelTime;
				if (totalTime < toNodeRoute.getCost()) {
					toNodeRoute.setCost(totalTime);
					toNodeRoute.setParentId(nodeId);
					priorityQ.offer(toNodeRoute);
				}
			}
		}
		
		if (startNode == endNode)
			System.out.println("start node is the same as end node.");
		else {
			current = nodeMap.get(endNode);
			if(current == null) {
				System.err.println("cannot find end node, program exit!");
				System.exit(-1);
			}
			pathNodeList.add(endNode);	// add end node
			
			while(current.getParentId() != -1 && current.getParentId() != startNode) {
				current = nodeMap.get(current.getParentId());
				if(current == null) {
					System.err.println("cannot find intermediate node, program exit!");
					System.exit(-1);
				}
				pathNodeList.add(current.getNodeId());	// add intermediate node
			}
			
			if(current.getParentId() == -1) {
				System.err.println("cannot find the path, program exit!");
				System.exit(-1);
			}
			
			if(current.getParentId() == startNode)
				pathNodeList.add(startNode);	// add start node
			
			Collections.reverse(pathNodeList);	// reverse the path list
		}
		System.out.println("start finding the path finish!");
	}
	
	public static void buildAdjList(int day) {
		String[] file = adjListFile.split("\\.");
		String tempAdjListFile = file[0] + "_" + days[day] + "." + file[1];
		
		System.out.println("loading adjlist file: " + tempAdjListFile);
		int debug = 0;
		try {
			FileInputStream fstream = new FileInputStream(root + "/" + tempAdjListFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			System.out.println("building graph, please wait...");

			while ((strLine = br.readLine()) != null) {
				debug++;
				if (debug % 100000 == 0)
					System.out.println("completed " + debug + " lines.");

				String[] splitStr = strLine.split("\\" + VERTICAL);
				long startNode = Long.parseLong(splitStr[0].substring(1));

				ArrayList<RDFToNodeInfo> toNodeList = new ArrayList<RDFToNodeInfo>();
				String[] nodeListStr = splitStr[1].split(SEMICOLON);
				for (int i = 0; i < nodeListStr.length; i++) {
					String nodeStr = nodeListStr[i];
					long toNode = Long.parseLong(nodeStr.substring(nodeStr.indexOf('n') + 1, nodeStr.indexOf('(')));
					String fixStr = nodeStr.substring(nodeStr.indexOf('(') + 1, nodeStr.indexOf(')'));
					RDFToNodeInfo toNodeInfo;
					if (fixStr.equals("F")) { // fixed
						int travelTime = Integer.parseInt(nodeStr.substring(nodeStr.indexOf(':') + 1));
						toNodeInfo = new RDFToNodeInfo(toNode, travelTime);
					} else { // variable
						String timeListStr = nodeStr.substring(nodeStr.indexOf(':') + 1);
						String[] timeValueStr = timeListStr.split(SEPARATION);
						int[] travelTimeArray = new int[timeValueStr.length];
						for (int j = 0; j < timeValueStr.length; j++)
							travelTimeArray[j] = Integer.parseInt(timeValueStr[j]);
						toNodeInfo = new RDFToNodeInfo(toNode, travelTimeArray);
					}
					toNodeList.add(toNodeInfo);
				}
				adjListMap.put(startNode, toNodeList);
			}

			br.close();
			in.close();
			fstream.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.err.println("buildList: debug code: " + debug);
		}
		System.out.println("building list finish!");
	}
}
