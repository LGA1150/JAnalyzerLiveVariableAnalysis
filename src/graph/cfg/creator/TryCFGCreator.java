package graph.cfg.creator;

import graph.cfg.CFGEdge;
import graph.cfg.ControlFlowGraph;
import graph.cfg.ExecutionPoint;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;

public class TryCFGCreator implements StatementCFGCreator {

	@SuppressWarnings("unchecked")
	@Override
	public List<PossiblePrecedeNode> create(ControlFlowGraph currentCFG,
			Statement astNode, List<PossiblePrecedeNode> precedeNodeList) {

		TryStatement tryStatement = (TryStatement)astNode;
		// 1 Create a virtual start node for the try block of the statement, 
		ExecutionPoint startNode = ExecutionPointFactory.createVirtualStart(tryStatement);
		currentCFG.addNode(startNode);
		
		// 2. For the possible precede node in the list precedeNodeList, if its reason type is PPR_SEQUENCE, then add edge <precedeNode, 
		//    startNode> to the current CFG, and remove the precede node from the list to form a new precedeNodeList.
		precedeNodeList = StatementCFGCreatorHelper.generateEdgeForSequentPrecedeNode(currentCFG, precedeNodeList, startNode);	
		
		// 3 Create a precede node list tryPrecedeNodeList, which only contains the node startNode, since the precede node of the try block 
		//   body is startNode
		List<PossiblePrecedeNode> tryPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
		tryPrecedeNodeList.add(new PossiblePrecedeNode(startNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		
		// 4 Create CFG for the body of the labeled statement, and get new precede node list tryPrecedeNodeList
		Block tryBody = tryStatement.getBody();
		StatementCFGCreator creator = StatementCFGCreatorFactory.getCreator(tryBody);
		tryPrecedeNodeList = creator.create(currentCFG, tryBody, tryPrecedeNodeList);
		
		// 5 Create a virtual end node for the try block of the statement 
		ExecutionPoint tryBlockEndNode = ExecutionPointFactory.createVirtualEnd(tryStatement);
		currentCFG.addNode(tryBlockEndNode);
		
		// 6 Traverse tryPrecedeNodeList, for the precede node tryPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge <tryPrecedeNode, 
		//   tryBlockEndNode> to currentCFG, else add it to precedeNodeList in the above 2
		for (PossiblePrecedeNode tryPrecedeNode : tryPrecedeNodeList) {
			if (tryPrecedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
				currentCFG.addEdge(new CFGEdge(tryPrecedeNode.getNode(), tryBlockEndNode, tryPrecedeNode.getLabel()));
			} else precedeNodeList.add(tryPrecedeNode);
		}
		
		// 7 Create a virtual start node for the finally block of the statement, add it to currentCFG, and add an edge <tryBlockEndNode, finallyStartNode> 
		ExecutionPoint finallyStartNode = ExecutionPointFactory.createFinallyStart(tryStatement);
		currentCFG.addNode(finallyStartNode);
		currentCFG.addEdge(new CFGEdge(tryBlockEndNode, finallyStartNode, null));
		
		// 8 Get the catch clauses for the statement, and create CFG for these statements
		List<CatchClause> catchClauseList = (List<CatchClause>)tryStatement.catchClauses();
		for (CatchClause catchClause : catchClauseList) {
			// 8.1 Get the exception declaration as the label of this catch clause
			String label = StatementCFGCreatorHelper.astNodeToString(catchClause.getException());
			
			// 8.2 Create a precede node list catchPrecedeNodeList, which only contains the node tryBlockEndNode, since the precede node of the catch clause 
			//   body is tryBlockEndNode
			List<PossiblePrecedeNode> catchPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
			catchPrecedeNodeList.add(new PossiblePrecedeNode(tryBlockEndNode, PossiblePrecedeReasonType.PPR_SEQUENCE, label));

			// 8.3 Create CFG for the body of the catch clause
			Block catchBody = catchClause.getBody();
			creator = StatementCFGCreatorFactory.getCreator(catchBody);
			catchPrecedeNodeList = creator.create(currentCFG, catchBody, catchPrecedeNodeList);
			
			// 8.4 Traverse catchPrecedeNodeList, for the precede node catchPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge 
			//     <catchPrecedeNode, finallyStartNode> to currentCFG, else add it to precedeNodeList in the above 2
			for (PossiblePrecedeNode catchPrecedeNode : catchPrecedeNodeList) {
				if (catchPrecedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
					currentCFG.addEdge(new CFGEdge(catchPrecedeNode.getNode(), finallyStartNode, catchPrecedeNode.getLabel()));
				} else precedeNodeList.add(catchPrecedeNode);
			}
		}
		
		// 9 Get the finally block of the statement, if it is not null, create CFG for it
		Block finallyBlock = tryStatement.getFinally();
		if (finallyBlock != null) {
			// 9.1 Create a precede node list finallyPrecedeNodeList, which only contains the node finallyStartNode, since the precede node of the 
			//     finally block is finallyStartNode
			List<PossiblePrecedeNode> finallyPrecedeNodeList = new LinkedList<PossiblePrecedeNode>();
			finallyPrecedeNodeList.add(new PossiblePrecedeNode(finallyStartNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
			// 9.2 Create CFG for the finally block statement
			creator = StatementCFGCreatorFactory.getCreator(finallyBlock);
			finallyPrecedeNodeList = creator.create(currentCFG, finallyBlock, finallyPrecedeNodeList);

			// 9.3 Create a virtual end node tryEndNode for the finally block of the statement, add it to currentCFG 
			ExecutionPoint tryEndNode = ExecutionPointFactory.createFinallyEnd(tryStatement);
			currentCFG.addNode(tryEndNode);

			// 9.3 Traverse finallyPrecedeNodeList, for the precede node finallyPrecedeNode in the list, if it is a PPR_SEQUENCE node, add edge 
			//     <finallyPrecedeNode, tryEndNode> to currentCFG, else add it to precedeNodeList in the above 2
			for (PossiblePrecedeNode finallyPrecedeNode : finallyPrecedeNodeList) {
				if (finallyPrecedeNode.getReason() == PossiblePrecedeReasonType.PPR_SEQUENCE) {
					currentCFG.addEdge(new CFGEdge(finallyPrecedeNode.getNode(), tryEndNode, finallyPrecedeNode.getLabel()));
				} else precedeNodeList.add(finallyPrecedeNode);
			}
			// 9.4 Add tryEndNode to precedeNodeList, and return
			precedeNodeList.add(new PossiblePrecedeNode(tryEndNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		} else {
			// finallyBlock == null, then the finallyStartNode created above is the end of the entire of try statement, add it to precedeNodeList, and return
			precedeNodeList.add(new PossiblePrecedeNode(finallyStartNode, PossiblePrecedeReasonType.PPR_SEQUENCE, null));
		}
		return precedeNodeList;
	}

}
