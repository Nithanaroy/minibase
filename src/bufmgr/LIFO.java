package bufmgr;

import java.util.ArrayList;

public class LIFO extends Replacer {

	private ArrayList<Integer> frames;
	private int nframes;

	protected LIFO(BufMgr mgr) {
		super(mgr);
		frames = null;
	}

	@Override
	protected void setBufferManager(BufMgr mgr) {
		super.setBufferManager(mgr);
		frames = new ArrayList<Integer>(mgr.getNumBuffers());
		nframes = 0;
	};

	@Override
	public int pick_victim() throws BufferPoolExceededException, PagePinnedException {
		int numBuffers = mgr.getNumBuffers();
		int frame;

		if (nframes < numBuffers) {
			frame = nframes++;
			frames.add(frame);
			state_bit[frame].state = Pinned;
			(mgr.frameTable())[frame].pin();
			return frame;
		}

		for (int i = numBuffers - 1; i >= 0; i--) {
			frame = frames.get(i);
			if (state_bit[frame].state != Pinned) {
				state_bit[frame].state = Pinned;
				(mgr.frameTable())[frame].pin();
				frames.add(frames.remove(i)); // Move this frame to the end
				return frame;
			}
		}

		return -1;
	}

	/**
	 * Page replacement policy name
	 */
	@Override
	public String name() {
		return "LIFO";
	}

}