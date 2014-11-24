package imagic.mobile.utils;

import java.util.List;

public interface RetrieveTagsListener{
	public void onFail(String debug);		
	public void onSucess(List<String> tags);
}
