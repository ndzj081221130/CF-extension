package sock;
public enum TxEventType{
	/**
	 * transaction start
	 */
	TransactionStart,
	/**
	 * transaction end
	 */
	TransactionEnd,
	/**
	 * The transaction first request service from other components. There maybe none, one, or more(e.g., multi-branch)
	 */
	FirstRequestService,
	/**
	 * Dynamic dependences potentially changes, e.g., request service from other components, if, while, switch, etc.
	 */
	DependencesChanged;
}
//TransactionStart = "TransactionStart"
//TransactionEnd = "TransactionEnd"
//FirstRequestService = "FirstRequestService"
//DependencyChanged = "DependencyChanged"