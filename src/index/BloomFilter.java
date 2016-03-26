package index;

import java.util.BitSet;

public class BloomFilter {
	private int reductionFactor;
	private int array_size;
	private BitSet b;
	private int size;

	public BloomFilter(int array_size, int reduction_factor) {
		this.reductionFactor = reduction_factor;
		this.array_size = array_size;
		this.size = array_size / reduction_factor;
		b = new BitSet(size);
	}

	public void setBit(int original_index) {
		if (original_index < array_size)
			b.set(Math.min(original_index / reductionFactor, size - 1));
	}

	public int nextSetChunk(int fromIndex) {
		if (fromIndex >= array_size)
			return -1;
		int i = b.nextSetBit(Math.min(fromIndex / reductionFactor, size - 1));
		if (i == -1)
			return -1;
		return i * reductionFactor;
	}

	public static void main(String[] args) {
		int a[] = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		BloomFilter b = new BloomFilter(a.length, 2);

		a[3] = 1;
		b.setBit(5);

		System.out.println(b.nextSetChunk(0));
		System.out.println(b.nextSetChunk(4));
		System.out.println(b.nextSetChunk(5));
		System.out.println(b.nextSetChunk(6));
	}
}
