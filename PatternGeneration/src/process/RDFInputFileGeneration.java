package process;

import data.*;
import function.*;

public class RDFInputFileGeneration {
	
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
		RDFInput.fetchLinkNodeIdByArea(RDFData.linkMap, RDFData.nodeMap);
		/**
		 * Step 2) fetch node lat, lon and zlevel
		 */
		RDFInput.fetchNodeInfoById(RDFData.nodeMap);
		/**
		 * Step 3) fetch link info
		 */
		RDFInput.fetchLinkInfoById(RDFData.linkMap);
		/**
		 * Step 4) eliminate non-nav link
		 */
		RDFInput.eliminateNonNavLink(RDFData.linkMap);
		/**
		 * Step 5) eliminate unrelated node
		 */
		RDFInput.eliminateUnrelatedNode(RDFData.linkMap, RDFData.nodeMap);
		/**
		 * Step 6) fetch link road name
		 */
		RDFInput.fetchLinkRoadById(RDFData.linkMap);
		/**
		 * Step 7) write node to file
		 */
		RDFOutput.writeNodeFile(RDFData.nodeMap);
		/**
		 * Step 8) write link to file
		 */
		RDFOutput.writeLinkFile(RDFData.linkMap);
		/**
		 * Step 9) write street name
		 */
		RDFOutput.writeStreetName(RDFData.linkMap);
		/**
		 * Step 10) fetch link geometry points
		 */
		RDFInput.fetchLinkGeometryById(RDFData.linkMap);
		/**
		 * Step 11) write link geometry points
		 */
		RDFOutput.writeLinkGeometry(RDFData.linkMap);
		/**
		 * Step 12) fetch lane information
		 */
		RDFInput.fetchLaneInfoByLink(RDFData.linkMap);
		/**
		 * Step 13) write lane information
		 */
		RDFOutput.writeLinkLaneFile(RDFData.linkMap);
		/**
		 * Step 14) mark manual carpool
		 */
		RDFInput.addManualCarpool();
		RDFInput.markManualCarpool(RDFData.linkMap);
	}
}

