/* File LRU.java */

package bufmgr;

import java.util.HashMap;

import global.PageId;

public class LRUK extends Replacer {

	private HashMap<Integer, Long> _LAST;
	private HashMap<Integer, Long[]> _HIST;

	private final float CORRELATED_REF_PERIOD = 0.35F;
	private final int K = 8;

	public void setBufferManager(BufMgr mgr) {
		super.setBufferManager(mgr);
	}

	public LRUK(BufMgr mgrArg) {
		super(mgrArg);
		_LAST = new HashMap<>();
		_HIST = new HashMap<>();
	}

	// Called when page is present in the buffer
	public void pin(int frameNo) throws InvalidFrameNumberException {
		super.pin(frameNo);
		int p = mgr.frameTable()[frameNo].pageNo.pid; // page in this frame
		long t = System.currentTimeMillis();
		if (t - LAST(p) > CORRELATED_REF_PERIOD) {
			long diff = LAST(p) - HIST(p, 1);
			for (int i = 2; i <= K; i++) {
				setHIST(p, i, HIST(p, i - 1) + diff);
			}
			setHIST(p, 1, t);
			setLAST(p, t);
		} else {
			setLAST(p, t);
		}
	}

	// Called when page is not found in buffer
	@Override
	public int pick_victim_for_page(PageId new_page) throws BufferPoolExceededException, PagePinnedException {
		long t = System.currentTimeMillis();
		int victim_frame = -1;

		long min = t;
		PageId victim = null;
		PageId pages[] = getPages();
		for (int i = 0; i < pages.length; i++) {
			PageId page = pages[i];
			int q = page.pid;

			if (q == -1) {
				victim_frame = i;
				victim = page;
				break;
			}

			int frame = mgr.hshTable().lookup(page);
			if (t - LAST(q) > CORRELATED_REF_PERIOD && state_bit[frame].state != Pinned && HIST(q, K) < min) {
				victim = page;
				min = HIST(q, K);
			}
		}
		if (victim == null)
			return -1;

		if (victim_frame == -1)
			victim_frame = mgr.hshTable().lookup(victim); // get frame number from victim page

		state_bit[victim_frame].state = Pinned;
		(mgr.frameTable())[victim_frame].pin();

		int p = new_page.pid;
		if (HIST(p, 1) == -1) {
			// History for new_page doesn't exist. Lets create!
			initHIST(p);
			for (int i = 2; i <= K; i++) {
				setHIST(p, i, 0);
			}
		} else {
			for (int i = 2; i <= K; i++) {
				setHIST(p, i, HIST(p, i - 1));
			}
		}
		setHIST(p, 1, t);
		setLAST(p, t);

		return victim_frame;
	};

	// Will not be called for LRUK, instead pick_victim_for_page() is called
	public int pick_victim() {
		return -1;
	}

	public String name() {
		return "LRUK";
	}

	public void info() {
		super.info();

		System.out.print("LRUK REPLACEMENT");
		int frames[] = getFrames();
		for (int i = 0; i < frames.length; i++) {
			if (i % 5 == 0)
				System.out.println();
			System.out.print("\t" + frames[i]);

		}
		System.out.println();
	}

	private PageId[] getPages() {
		PageId pages[] = new PageId[mgr.getNumBuffers()];
		int i = 0;
		for (FrameDesc frame : mgr.frameTable()) {
			pages[i] = (frame.pageNo);
			i++;
		}
		return pages;
	}

	/**
	 * Using frame table in Buffer Manager, compute a list of page IDs present in all frames
	 */
	public int[] getFrames() {
		int frames[] = new int[mgr.getNumBuffers()];
		int i = 0;
		for (FrameDesc frame : mgr.frameTable()) {
			frames[i] = (frame.pageNo.pid);
			i++;
		}
		return frames;
	}

	/**
	 * @param i 1 based index
	 */
	private void setHIST(int pagenumber, int i, long v) {
		_HIST.get(pagenumber)[i - 1] = v;
	}

	/** Only memory is assigned. Initialization is assumed to be done by the callee */
	private void initHIST(int pagenumber) {
		_HIST.put(pagenumber, new Long[K + 1]);
	}

	/**
	 * @param i 1 based index
	 */
	public long HIST(int pagenumber, int i) {
		if (_HIST.containsKey(pagenumber))
			return _HIST.get(pagenumber)[i - 1];
		return -1;
	}

	private void setLAST(int pagenumber, long v) {
		_LAST.put(pagenumber, v);
	}

	public long LAST(int pagenumber) {
		return _LAST.get(pagenumber);
	}

	/**
	 * Returns the backward K distance (relative) of the given page
	 * 
	 * @param pagenumber
	 * @param k
	 * @return
	 */
	public long back(int pagenumber, int k) {
		// return System.currentTimeMillis() - HIST(pagenumber, k); // TODO
		return HIST(pagenumber, k);
	}

}
