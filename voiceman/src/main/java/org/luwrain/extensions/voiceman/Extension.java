
package org.luwrain.extensions.voiceman;

import org.luwrain.core.*;
import org.luwrain.core.extensions.*;

public class Extension extends EmptyExtension
{
    @Override public String init(Luwrain luwrain)
    {
	return null;
    }

    @Override public org.luwrain.speech.Factory[] getSpeechFactories(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new org.luwrain.speech.Factory[]{new SpeechFactory(luwrain)};
    }
}
