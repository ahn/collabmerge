package fi.tut.collabmerge;


public class Author {
	
	public final String name;
	public final String email;
	public final boolean isMerger;
	public final boolean isMergeHead;
	public Author(String name, String email, boolean isMerger, boolean isMergeHead) {
		this.name = name;
		this.email = email;
		this.isMerger = isMerger;
		this.isMergeHead = isMergeHead;
	}
}
