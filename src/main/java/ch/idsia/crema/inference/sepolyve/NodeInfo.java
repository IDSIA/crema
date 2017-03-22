package ch.idsia.crema.inference.sepolyve;

public class NodeInfo {
	private int node;
	private long verticesIn;
	private long verticesOut;
	private long time;
	
	public NodeInfo() {
	}

	public NodeInfo(int node, long verticesIn, long verticesOut, long time) {
		super();
		this.node = node;
		this.verticesIn = verticesIn;
		this.verticesOut = verticesOut;
		this.time = time;
	}

	public int getNode() {
		return node;
	}

	public void setNode(int node) {
		this.node = node;
	}

	public long getVerticesIn() {
		return verticesIn;
	}

	public void setVerticesIn(long verticesIn) {
		this.verticesIn = verticesIn;
	}

	public long getVerticesOut() {
		return verticesOut;
	}

	public void setVerticesOut(long verticesOut) {
		this.verticesOut = verticesOut;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
}
