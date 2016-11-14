
package org.luwrain.extensions.voiceman;

import java.util.*;
import javax.sound.sampled.AudioFormat;

import java.net.*;
import java.io.*;

import org.luwrain.core.*;
import org.luwrain.speech.*;

public class VoiceMan implements Channel
{
    static private final String DEFAULT_NAME = "voiceman";
    static private final String DEFAULT_HOST = "localhost";
    static private final int DEFAULT_PORT = 5511;
    static private final String CMDLINE_HOST = "--voiceman-host=";
    static private final String CMDLINE_PORT = "--voiceman-hport=";

    private Socket sock = null;
    private PrintStream output = null;
    private int defaultPitch = 50;
    private int defaultRate = 50;
    private String name = DEFAULT_NAME;
    private boolean def = true;

    @Override public boolean initByRegistry(Registry registry, String regPath)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(regPath, "regPath");
	String host = null;
	int port = 0;
	if (registry != null && regPath != null && !regPath.trim().isEmpty())
	{
	    final Settings s = RegistryProxy.create(registry, regPath, Settings.class);
	    host = s.getHost(DEFAULT_HOST);
	    port = s.getPort(DEFAULT_PORT);
	    name = s.getName(DEFAULT_NAME);
	    def = s.getDefault(false);
	    Log.debug("voiceman", "hname value from the registry is \'" + (name != null?name:"") + "\'");
	    Log.debug("voiceman", "host value from the registry is " + (host != null?host:""));
	    Log.debug("voiceman", "hport value from the registry is " + port);
	}
	/*
	final CmdLineUtils cmdLineUtils = new CmdLineUtils(cmdLine);
	final String h = cmdLineUtils.getFirstArg(CMDLINE_HOST);
	final String p = cmdLineUtils.getFirstArg(CMDLINE_HOST);
	if (h != null && !h.trim().isEmpty())
	{
	    Log.info("voiceman", "using host value from the command line:" + h);
	    host = h;
	}
	if (p != null && !p.trim().isEmpty())
	{
	    Log.info("voiceman", "using port value from the registry:" + p);
	    try {
		port = Integer.parseInt(p);
	    }
	    catch(NumberFormatException e)
	    {
		e.printStackTrace();
	    }
	}
	if (host == null || host.trim().isEmpty())
	{
	    Log.warning("voiceman", "unable to get host value, using default");
	    host = DEFAULT_HOST;
	}
	if (port == 0)
	{
	    Log.warning("voiceman", "unable to get port value, using default");
	    port = DEFAULT_PORT;
	}
	*/
	return connect(host, port);
    }

    @Override public boolean initByArgs(String[] args)
    {
	NullCheck.notNullItems(args, "args");
	return false;
    }

    private boolean connect(String host, int port)
    {
	Log.debug("voiceman", "connecting to " + host + ":" + port);
	try {
	    sock = new Socket(host, port);
	    output = new PrintStream(sock.getOutputStream(), true, "UTF-8");
	    return true;
	}
	catch(IOException e)
	{
	    sock = null;
	    output = null;
	    Log.error("voiceman", "unable to connect to " + host + ":" + port + ":" + e.getMessage());
	    e.printStackTrace();
	    return false;
	}
    }

    @Override public long speak(String text, Listener listener,
				int relPitch, int relRate, boolean cancelPrevious)
    {
	if (cancelPrevious)
	    silence();
	sendPitch(defaultPitch + relPitch);
	sendRate(defaultRate + relRate);
	sendText(text);
	sendPitch(defaultPitch);
	sendRate(defaultRate);
	return -1;
    }

    @Override public long speakLetter(char letter, Listener listener,
				      int relPitch, int relRate, boolean cancelPrevious)
    {
	if (cancelPrevious)
	    silence();
	sendPitch(defaultPitch + relPitch);
	sendRate(defaultRate + relRate);
	sendLetter(letter);
	sendPitch(defaultPitch);
	sendRate(defaultRate);
	return -1;
    }




    private void sendPitch(int value)
    {
	if (value < 0)
	    output.println("P:0"); else
	    if (value > 100)
		output.println("P:100"); else
		output.println("P:" + value);
    }

    private void sendRate(int value)
    {
	if (value < 0)
	    output.println("R:0"); else
	    if (value > 100)
		output.println("R:100"); else
		output.println("R:" + value);
    }

    private void sendText(String text)
    {
	NullCheck.notNull(text, "text");
	output.println("T:" + text.replaceAll("\n", " "));
    }

    private void sendLetter(char letter)
    {
	String s = "L:";
	s += letter;
	output.println(s);
    }

    @Override public AudioFormat[] getSynthSupportedFormats()
    {
	return new AudioFormat[0];
    }

    @Override public void close()
    {
	if (output != null)
	    output.close();
	try {
	    if (sock != null)
		sock.close();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	output = null;
	sock = null;
    }

    @Override public boolean synth(String text, 
				   int pitch, int rate, 
				   AudioFormat format, OutputStream stream)
    {
	return false;
    }

    @Override public void silence()
    {
	if (output == null)
	    return;
	output.println("S:");
	output.flush();
    }

    @Override public void setCurrentVoice(String name)
    {
    }

    @Override public Set<Features>  getFeatures()
    {
	return EnumSet.of(Features.CAN_SYNTH_TO_SPEAKERS);
    }

    @Override public boolean isDefault()
    {
	return def;
    }

    @Override public String getChannelName()
    {
	return name;
    }

    @Override public PuncMode getCurrentPuncMode()
    {
	return PuncMode.ALL;
    }

    @Override public void setCurrentPuncMode(PuncMode mode)
    {
    }

    @Override public String getCurrentVoiceName()
    {
	return "";
    }

    @Override public Voice[] getVoices()
    {
	return new Voice[0];
    }

    @Override public int getDefaultPitch()
    {
	return defaultPitch;
    }

    @Override public void setDefaultPitch(int value)
    {
	int v = value;
	if (v < 0)
	    v = 0;
	    if (v > 100)
		v = 100;
	defaultPitch = v;
	sendPitch(defaultPitch);
    }

    @Override public int getDefaultRate()
    {
	return defaultRate;
    }

    @Override public void setDefaultRate(int value)
    {
	int v = value;
	if (v < 0)
	    v = 0;
	    if (v > 100)
		v = 100;
	    defaultRate = v;
	    sendRate(defaultRate);
    }
}
