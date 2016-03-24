package iterator;

public class MyTuple implements Comparable<MyTuple> {
	public int id;
	public int duration;
	public int cost;

	public MyTuple(int id, int duration, int cost) {
		super();
		this.id = id;
		this.duration = duration;
		this.cost = cost;
	}

	@Override
	public int compareTo(MyTuple o) {
		return this.id - o.id;
	}

	@Override
	public String toString() {
		return String.format("[%d,%d,%d]", this.id, this.duration, this.cost);
	}

}
