package imagic.mobile.object;

import java.util.Comparator;

public class TagInfo implements Comparator<TagInfo>, Comparable<TagInfo>{

	private String name;
	private int numberImages;

	public int getNumberImages() {
		return numberImages;
	}
	public void setNumberImages(int numberImages) {
		this.numberImages = numberImages;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TagInfo(String name, int numberImages) {
		super();
		this.name = name;
		this.numberImages = numberImages;
	}

	// Overriding the compareTo method
	public int compareTo(TagInfo d){
		return d.numberImages - this.numberImages;
	}

	// Overriding the compare method to sort the age 
	public int compare(TagInfo d, TagInfo d1){
		return d1.numberImages - d.numberImages;
	}
}
