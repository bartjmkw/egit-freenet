import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

	/** Type of patch used by this file. */
	public static enum PatchType {
		/** A traditional unified diff style patch of a text file. */
		UNIFIED,

		/** An empty patch with a message "Binary files ... differ" */
		BINARY,

		/** A Git binary patch, holding pre and post image deltas */
		GIT_BINARY;
	}

	/** Type of patch used to modify this file */
	PatchType patchType;

	/** The hunks of this file */
	private List<HunkHeader> hunks;

		patchType = PatchType.UNIFIED;
	/** @return style of patch used to modify this file */
	public PatchType getPatchType() {
		return patchType;
	}

	/** @return true if this patch modifies metadata about a file */
	public boolean hasMetaDataChanges() {
		return changeType != ChangeType.MODIFY || newMode != oldMode;
	}

	/** @return hunks altering this file; in order of appearance in patch */
	public List<HunkHeader> getHunks() {
		if (hunks == null)
			return Collections.emptyList();
		return hunks;
	}

	void addHunk(final HunkHeader h) {
		if (h.getFileHeader() != this)
			throw new IllegalArgumentException("Hunk belongs to another file");
		if (hunks == null)
			hunks = new ArrayList<HunkHeader>();
		hunks.add(h);
	}

	int parseTraditionalHeaders(int ptr) {
		final int sz = buf.length;
		while (ptr < sz) {
			final int eol = nextLF(buf, ptr);
			if (match(buf, ptr, HUNK_HDR) >= 0) {
				// First hunk header; break out and parse them later.
				break;

			} else if (match(buf, ptr, OLD_NAME) >= 0) {
				oldName = p1(parseName(oldName, ptr + OLD_NAME.length, eol));
				if (oldName == DEV_NULL)
					changeType = ChangeType.ADD;

			} else if (match(buf, ptr, NEW_NAME) >= 0) {
				newName = p1(parseName(newName, ptr + NEW_NAME.length, eol));
				if (newName == DEV_NULL)
					changeType = ChangeType.DELETE;

			} else {
				// Possibly an empty patch.
				break;
			}

			ptr = eol;
		}
		return ptr;
	}
