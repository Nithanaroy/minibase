package iterator;

public class MyTuple implements Comparable<MyTuple> {
	public int id;
	public int duration;
	public int cost;

	@Override
	public int compareTo(MyTuple o) {
		return this.id - o.id;
	}

}
