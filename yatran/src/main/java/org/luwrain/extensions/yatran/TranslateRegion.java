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
import org.luwrain.core.events.MessageEvent;
import org.luwrain.util.*;

class TranslateRegion implements Command
{
    static private final String KEY_PATH = "/org/luwrain/extensions/yatran/key";

    @Override public String getName()
    {
	return "yatran-translate-region";
    }

    @Override public void onCommand(Luwrain luwrain)
    {
	final Registry registry = luwrain.getRegistry();
	final RegistryAutoCheck check = new RegistryAutoCheck(registry);
	final String key = check.stringAny(KEY_PATH, "");
	if (key == null || key.trim().isEmpty())
	{
	    luwrain.message("Не задан ключ для доступа к функциям переводчика", Luwrain.MESSAGE_ERROR);//FIXME:
	    return;
	}
	final RegionContent data = luwrain.currentAreaRegion(true);
	if (data == null)
	    return;
	final Client client = new Client(key);
	final String text = data.toSingleLine();
	final Luwrain l = luwrain;
	final Runnable r = new Runnable(){ 
		@Override public void run()
		{
		    try {
			System.out.println(text);
			l.enqueueEvent(new MessageEvent(client.translate(text), Luwrain.MESSAGE_DONE));
		    }
		    catch (Exception e)
		    {
			e.printStackTrace();
			l.enqueueEvent(new MessageEvent("Произошла ошибка при обращении к серверу перевода", Luwrain.MESSAGE_ERROR));
		    }
		}};
	new Thread(r).start();
    }

}
