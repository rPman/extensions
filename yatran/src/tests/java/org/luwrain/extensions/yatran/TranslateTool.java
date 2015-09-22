
package org.luwrain.extensions.yatran;

public class TranslateTool
{
    static public void main(String[] args) throws Exception
    {
	if (args.length < 2)
	{
	    System.out.println("No key and text to translate");
	    return;
	}
	final Client client = new Client(args[0]);
	System.out.println(client.translate(args[1]));
    }
}
