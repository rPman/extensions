
package org.luwrain.extensions.mssapi;

import org.luwrain.core.*;
import org.luwrain.speech.*;

class SpeechFactory implements Factory
{
    private Luwrain luwrain;

SpeechFactory(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	this.luwrain = luwrain;
    }

    @Override public String getServedChannelType()
    {
	return "sapi";
    }

    @Override public Channel newChannel()
    {
    	return new SapiChannel();
    }

    @Override public org.luwrain.cpanel.Section newSettingsSection(org.luwrain.cpanel.Element el, String registryPath)
    {
	return null;
    }
}
