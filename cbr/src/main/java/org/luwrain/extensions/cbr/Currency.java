//URL: http://www.cbr.ru/scripts/XML_daily.asp?date_req=02/03/2002

package org.luwrain.extensions.cbr;

import java.util.*;
import java.net.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

public class Currency
{ 
    private URL urlFrom = null;

    public Currency()
    {
	try {
	    urlFrom = new URL("http://www.cbr.ru/scripts/XML_daily.asp?date_req=02/03/2002");
	}
	catch (MalformedURLException e)
		{
		    urlFrom = null;
		}
    }

    public Currency(URL urlFrom)
    {
	this.urlFrom = urlFrom;
    }

	public void read()  throws SAXException, IOException, ParserConfigurationException 
	{
	if (urlFrom == null)
	    return;
        URLConnection con = urlFrom.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	try {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    Document document = builder.parse(new InputSource(in));
	    NodeList nodes = document.getDocumentElement().getChildNodes();
	    for (int i = 0;i < nodes.getLength();i++)
	    {
		Node node = nodes.item(i);
		if (node.getNodeType() != Node.ELEMENT_NODE)
		    continue;
		Element current = (Element)node;
		System.out.println(current.getTagName());
		if (current.getTagName().equals("Valute"))
		    processValute(current);
	    }
	}
	finally {
	    in.close();;
	    //	    con.close();
	}
	}

    private void processValute(Element e) throws IOException
    {
	if (e == null)
	    return;
	    /*
		NamedNodeMap nameMap = e.getAttributes();
		Node n = nameMap.getNamedItem("name");
		if (n == null)
			throw new IOException("One of the \'database\' tags in config file does not contain required argument \'name\'");
		db.name = n.getTextContent();
*/
	NodeList nodes = e.getChildNodes();
	for (int i = 0;i < nodes.getLength();i++) 
	{
	    Node node = nodes.item(i);
	    if (node.getNodeType() != Node.ELEMENT_NODE)
		continue;
	    Element current = (Element)node;
	    System.out.println(current.getTagName());
	    System.out.println(node.getTextContent());
	}
    }

    public static void main(String[] args)
    {
	try {
	    Currency c = new Currency();
	    c.read();
	}
	catch(Exception e)
	{
	    e.printStackTrace();
	}
    }
} //class ConfigFileReader;
