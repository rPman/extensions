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
import org.luwrain.speech.*;
import org.luwrain.cpanel.SimpleSection;

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
	return "emacspeak";
    }

    @Override public Channel newChannel()
    {
	return new Emacspeak();
    }

    @Override public org.luwrain.cpanel.Section newSettingsSection(org.luwrain.cpanel.Element el, String registryPath)
    {
	NullCheck.notNull(el, "el");
	NullCheck.notNull(registryPath, "registryPath");
	final Settings settings = Settings.create(luwrain.getRegistry(), registryPath);
	return new SimpleSection(el, settings.getName("???"), (controlPanel)->SettingsForm.create(controlPanel, registryPath));
    }
}
