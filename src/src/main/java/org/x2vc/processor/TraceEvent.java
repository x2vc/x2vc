package org.x2vc.processor;

import java.util.Objects;

/**
 * Standard implementation of {@link ITraceEvent}.
 */
public class TraceEvent implements ITraceEvent {

	private static final long serialVersionUID = -5689740211638377974L;
	private int traceID;
	private String elementName;

	/**
	 * @param traceID
	 * @param elementName
	 */
	public TraceEvent(int traceID, String elementName) {
		super();
		this.traceID = traceID;
		this.elementName = elementName;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.elementName, this.traceID);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TraceEvent other = (TraceEvent) obj;
		return Objects.equals(this.elementName, other.elementName) && this.traceID == other.traceID;
	}

	@Override
	public int getTraceID() {
		return this.traceID;
	}

	@Override
	public String getElementName() {
		return this.elementName;
	}

}
