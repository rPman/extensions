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

import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

import org.luwrain.core.NullCheck;

class Client
{
    static private final String LANG_LIST_URL = "https://translate.yandex.net/api/v1.5/tr/getLangs?";
    static private final String TRANSLATE_URL = "https://translate.yandex.net/api/v1.5/tr/translate?";

    private String key;

    Client(String key)
    {
	this.key = key;
	NullCheck.notNull(key, "key");
	if (key.trim().isEmpty())
	    throw new IllegalArgumentException("key may not be empty");
    }

    String translate(String text) throws IOException, MalformedURLException, 
					 ParserConfigurationException, SAXException
    {
	final StringBuilder b = new StringBuilder();
	b.append(TRANSLATE_URL);
	b.append("key=");
	b.append(key);
	b.append("&text=");
	b.append(URLEncoder.encode(text));
	b.append("&lang=en-ru");
	final URL url = new URL(b.toString());
	final StringBuilder res = new StringBuilder();
	final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	final Document document = builder.parse(new InputSource(url.openStream()));
	final NodeList nodes = document.getElementsByTagName("text");
	for (int i = 0;i < nodes.getLength();++i)
	{
	    final Node node = nodes.item(i);
	    if (node.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    final Element el = (Element)node;
	    final String v = el.getTextContent();
	    if (v == null)
		continue;
	    res.append(v);
	    res.append(" ");
	}
	return res.toString().trim();
    }
}
