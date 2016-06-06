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

package org.luwrain.extensions.yatran;

import org.luwrain.core.*;
import org.luwrain.core.extensions.*;
import org.luwrain.i18n.*;

public class Extension extends EmptyExtension
{
    static private final String STRINGS_PROPERTIES_RESOURCE = "org/luwrain/extensions/yatran/strings.properties";

    private TranslateRegion translateRegion = null;

    @Override public Command[] getCommands(Luwrain luwrain)
    {
	if (translateRegion == null)
	    translateRegion = new TranslateRegion();
	return new Command[]{translateRegion};
    }

    @Override public org.luwrain.cpanel.Factory[] getControlPanelFactories(Luwrain luwrain)
    {
	NullCheck.notNull(luwrain, "luwrain");
	return new org.luwrain.cpanel.Factory[]{new ControlPanelFactory(luwrain)};
    }

    @Override public void i18nExtension(Luwrain luwrain, I18nExtension ext)
    {
	NullCheck.notNull(ext, "ext");
	try {
	    ext.addStrings("ru", Strings.NAME, PropertiesProxy.create(ClassLoader.getSystemResource(STRINGS_PROPERTIES_RESOURCE), "yatran.", Strings.class));
	}
	catch(java.io.IOException e)
	{
	    e.printStackTrace();
	}
    }
}
