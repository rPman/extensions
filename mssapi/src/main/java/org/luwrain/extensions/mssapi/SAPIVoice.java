
package org.luwrain.extensions.mssapi;

import org.luwrain.speech.Voice;

public class SAPIVoice implements Voice
{
	private String id;
	
	private boolean isMale;
	private String name;
	
	public SAPIVoice(String id,String name,boolean isMale)
	{
		this.id=id;
		this.name=name;
		this.isMale=isMale;
	}
	
	@Override public boolean isMale()
	{
		return isMale;
	}

	@Override public String getVoiceName()
	{
		return name;
	}

}
