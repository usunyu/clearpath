package input;

import java.util.*;

import objects.*;
import function.*;

public class RDFInputFileGeneration {

	/**
	 * @param node
	 */
	static HashMap<Long, RDFNodeInfo> nodeMap = new HashMap<Long, RDFNodeInfo>();
	/**
	 * @param link
	 */
	static HashMap<Long, RDFLinkInfo> linkMap = new HashMap<Long, RDFLinkInfo>();
	
	public static void main(String[] args) {
		//fetchNode();
		//writeNodeFile();
		
		/*fetchLink();					deprecated */
		/*fetchGeometry();				deprecated */
		/*writeLinkFile();			 	deprecated */
		
		//fetchWriteLink();
		/*readFetchWriteGeometry();	 	deprecated */
		
		//fetchWriteGeometry();
		
		/** deprecated
		 *  Step 1) add the post code needed (deprecated not accurate and continue) 
		 *  		fetch link from post code (deprecated)
		 *  		fetch link by area (lat, lon)
		 *  		write link info to RDF_Link.txt
		 */
		/*initialPostCode();		 	deprecated */
		/*fetchLinkByPostCode();	 	deprecated */
		//fetchLinkByAreaAll();
		//writeLinkDetail();
		/**
		 *  Step 2) read the info from RDF_Link.txt
		 *  		fetch the node info according the read data
		 *  		write the node info to RDF_Node.txt
		 */
		//readLinkFile();
		//fetchNodeBySet();
		//writeNodeFile();
		/**
		 *  Step 3) read the info from RDF_Link.txt
		 *			fetch geometry points
		 */
		//readLinkFile();
		//fetchGeometry();
		//writeLinkWithGeometry();
		
		/**
		 * Step 1) fetch linkId and nodeId by area(lat, lon)
		 */
		RDFInput.fetchLinkNodeIdByArea(linkMap, nodeMap);
		/**
		 * Step 2) fetch node lat, lon and zlevel
		 */
		RDFInput.fetchNodeInfoById(nodeMap);
		/**
		 * Step 3) fetch link info
		 */
		RDFInput.fetchLinkInfoById(linkMap);
		/**
		 * Step 4) eliminate non-nav link
		 */
		RDFInput.eliminateNonNavLink(linkMap);
		/**
		 * Step 5) eliminate unrelated node
		 */
		RDFInput.eliminateUnrelatedNode(linkMap, nodeMap);
		/**
		 * Step 6) fetch link road name
		 */
		RDFInput.fetchLinkRoadById(linkMap);
		/**
		 * Step 7) write node to file
		 */
		RDFOutput.writeNodeFile(nodeMap);
		/**
		 * Step 8) write link to file
		 */
		RDFOutput.writeLinkFile(linkMap);
		/**
		 * Step 9) write street name
		 */
		RDFOutput.writeStreetName(linkMap);
		/**
		 * Step 10) fetch link geometry points
		 */
		RDFInput.fetchLinkGeometryById(linkMap);
		/**
		 * Step 11) write link geometry points
		 */
		RDFOutput.writeLinkGeometry(linkMap);
		/**
		 * Step 12) fetch lane information
		 */
		RDFInput.fetchLaneInfoByLink(linkMap);
		/**
		 * Step 13) write lane information
		 */
		RDFOutput.writeLinkLaneFile(linkMap);
	}
}




