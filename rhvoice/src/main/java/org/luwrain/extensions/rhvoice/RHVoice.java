
package org.luwrain.extensions.rhvoice;

import org.luwrain.speech.Voice;

public class RHVoice implements Voice
{
	private String name;
	
	public RHVoice(String name)
	{
		this.name=name;
	}
	
	@Override public boolean isMale()
	{
		return true;
	}

	@Override public String getVoiceName()
	{
		return name;
	}

}
