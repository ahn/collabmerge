package fi.tut.collabmerge;


public class Author {
	
	final String name;
	final String email;
	final boolean isMerger;
	final boolean isMergeHead;
	public Author(String name, String email, boolean isMerger, boolean isMergeHead) {
		this.name = name;
		this.email = email;
		this.isMerger = isMerger;
		this.isMergeHead = isMergeHead;
	}
}
