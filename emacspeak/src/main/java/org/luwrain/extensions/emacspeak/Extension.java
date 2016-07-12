/*
   Copyright 2012-2016 Michael Pozhidaev <michael.pozhidaev@gmail.com>

   This file is part of the LUWRAIN.

   LUWRAIN is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public
   License as published by the Free Software Foundation; either
   version 3 of the License, or (at your option) any later version.

   LUWRAIN is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.
*/

package org.luwrain.extensions.emacspeak;

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
