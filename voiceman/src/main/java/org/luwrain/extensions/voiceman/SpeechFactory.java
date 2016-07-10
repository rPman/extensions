
package org.luwrain.extensions.voiceman;

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
	return "voiceman";
    }

    @Override public Channel newChannel()
    {
	return new VoiceMan();
    }

    @Override public org.luwrain.cpanel.Section newSettingsSection(org.luwrain.cpanel.Element el, String registryPath)
    {
	return null;
    }
}
