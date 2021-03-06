/*
 * Copyright (C) 2009, Google Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spearce.jgit.revwalk;

import java.util.List;

import org.spearce.jgit.lib.Constants;
import org.spearce.jgit.lib.ObjectId;
import org.spearce.jgit.lib.RepositoryTestCase;

public class FooterLineTest extends RepositoryTestCase {
	public void testNoFooters_EmptyBody() {
		final RevCommit commit = parse("");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_NewlineOnlyBody1() {
		final RevCommit commit = parse("\n");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_NewlineOnlyBody5() {
		final RevCommit commit = parse("\n\n\n\n\n");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_OneLineBodyNoLF() {
		final RevCommit commit = parse("this is a commit");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_OneLineBodyWithLF() {
		final RevCommit commit = parse("this is a commit\n");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_ShortBodyNoLF() {
		final RevCommit commit = parse("subject\n\nbody of commit");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testNoFooters_ShortBodyWithLF() {
		final RevCommit commit = parse("subject\n\nbody of commit\n");
		final List<FooterLine> footers = commit.getFooterLines();
		assertNotNull(footers);
		assertEquals(0, footers.size());
	}

	public void testSignedOffBy_OneUserNoLF() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Signed-off-by: A. U. Thor <a@example.com>");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("A. U. Thor <a@example.com>", f.getValue());
		assertEquals("a@example.com", f.getEmailAddress());
	}

	public void testSignedOffBy_OneUserWithLF() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Signed-off-by: A. U. Thor <a@example.com>\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("A. U. Thor <a@example.com>", f.getValue());
		assertEquals("a@example.com", f.getEmailAddress());
	}

	public void testSignedOffBy_IgnoreWhitespace() {
		// We only ignore leading whitespace on the value, trailing
		// is assumed part of the value.
		//
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Signed-off-by:   A. U. Thor <a@example.com>  \n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("A. U. Thor <a@example.com>  ", f.getValue());
		assertEquals("a@example.com", f.getEmailAddress());
	}

	public void testEmptyValueNoLF() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Signed-off-by:");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("", f.getValue());
		assertNull(f.getEmailAddress());
	}

	public void testEmptyValueWithLF() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Signed-off-by:\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("", f.getValue());
		assertNull(f.getEmailAddress());
	}

	public void testShortKey() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "K:V\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("K", f.getKey());
		assertEquals("V", f.getValue());
		assertNull(f.getEmailAddress());
	}

	public void testNonDelimtedEmail() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Acked-by: re@example.com\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Acked-by", f.getKey());
		assertEquals("re@example.com", f.getValue());
		assertEquals("re@example.com", f.getEmailAddress());
	}

	public void testNotEmail() {
		final RevCommit commit = parse("subject\n\nbody of commit\n" + "\n"
				+ "Acked-by: Main Tain Er\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(1, footers.size());

		f = footers.get(0);
		assertEquals("Acked-by", f.getKey());
		assertEquals("Main Tain Er", f.getValue());
		assertNull(f.getEmailAddress());
	}

	public void testSignedOffBy_ManyUsers() {
		final RevCommit commit = parse("subject\n\nbody of commit\n"
				+ "Not-A-Footer-Line: this line must not be read as a footer\n"
				+ "\n" // paragraph break, now footers appear in final block
				+ "Signed-off-by: A. U. Thor <a@example.com>\n"
				+ "CC:            <some.mailing.list@example.com>\n"
				+ "Acked-by: Some Reviewer <sr@example.com>\n"
				+ "Signed-off-by: Main Tain Er <mte@example.com>\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(4, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("A. U. Thor <a@example.com>", f.getValue());
		assertEquals("a@example.com", f.getEmailAddress());

		f = footers.get(1);
		assertEquals("CC", f.getKey());
		assertEquals("<some.mailing.list@example.com>", f.getValue());
		assertEquals("some.mailing.list@example.com", f.getEmailAddress());

		f = footers.get(2);
		assertEquals("Acked-by", f.getKey());
		assertEquals("Some Reviewer <sr@example.com>", f.getValue());
		assertEquals("sr@example.com", f.getEmailAddress());

		f = footers.get(3);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("Main Tain Er <mte@example.com>", f.getValue());
		assertEquals("mte@example.com", f.getEmailAddress());
	}

	public void testSignedOffBy_SkipNonFooter() {
		final RevCommit commit = parse("subject\n\nbody of commit\n"
				+ "Not-A-Footer-Line: this line must not be read as a footer\n"
				+ "\n" // paragraph break, now footers appear in final block
				+ "Signed-off-by: A. U. Thor <a@example.com>\n"
				+ "CC:            <some.mailing.list@example.com>\n"
				+ "not really a footer line but we'll skip it anyway\n"
				+ "Acked-by: Some Reviewer <sr@example.com>\n"
				+ "Signed-off-by: Main Tain Er <mte@example.com>\n");
		final List<FooterLine> footers = commit.getFooterLines();
		FooterLine f;

		assertNotNull(footers);
		assertEquals(4, footers.size());

		f = footers.get(0);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("A. U. Thor <a@example.com>", f.getValue());

		f = footers.get(1);
		assertEquals("CC", f.getKey());
		assertEquals("<some.mailing.list@example.com>", f.getValue());

		f = footers.get(2);
		assertEquals("Acked-by", f.getKey());
		assertEquals("Some Reviewer <sr@example.com>", f.getValue());

		f = footers.get(3);
		assertEquals("Signed-off-by", f.getKey());
		assertEquals("Main Tain Er <mte@example.com>", f.getValue());
	}

	public void testFilterFootersIgnoreCase() {
		final RevCommit commit = parse("subject\n\nbody of commit\n"
				+ "Not-A-Footer-Line: this line must not be read as a footer\n"
				+ "\n" // paragraph break, now footers appear in final block
				+ "Signed-Off-By: A. U. Thor <a@example.com>\n"
				+ "CC:            <some.mailing.list@example.com>\n"
				+ "Acked-by: Some Reviewer <sr@example.com>\n"
				+ "signed-off-by: Main Tain Er <mte@example.com>\n");
		final List<String> footers = commit.getFooterLines("signed-off-by");

		assertNotNull(footers);
		assertEquals(2, footers.size());

		assertEquals("A. U. Thor <a@example.com>", footers.get(0));
		assertEquals("Main Tain Er <mte@example.com>", footers.get(1));
	}

	private RevCommit parse(final String msg) {
		final StringBuilder buf = new StringBuilder();
		buf.append("tree " + ObjectId.zeroId().name() + "\n");
		buf.append("author A. U. Thor <a@example.com> 1 +0000\n");
		buf.append("committer A. U. Thor <a@example.com> 1 +0000\n");
		buf.append("\n");
		buf.append(msg);

		final RevWalk walk = new RevWalk(db);
		walk.setRetainBody(true);
		final RevCommit c = new RevCommit(ObjectId.zeroId());
		c.parseCanonical(walk, Constants.encode(buf.toString()));
		return c;
	}
}
