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

import java.util.*;
import java.io.*;
import javax.sound.sampled.AudioFormat;

import org.luwrain.speech.*;
import org.luwrain.core.*;

class Emacspeak implements Channel
{
    private long nextId = 1;
    private boolean def = false;
    private String name = "";
    private String command = "";
    private Process process;
    private OutputStream stream;
    private BufferedWriter writer;
    private int defPitch = DEFAULT_PARAM_VALUE;
    private int defRate = DEFAULT_PARAM_VALUE;

    @Override public boolean initByRegistry(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	final Settings settings = Settings.create(registry, path);
	name = settings.getName("???");
	command = settings.getCommand("");
	def = settings.getDefault(false);
	if (command.trim().isEmpty())
	{
	    Log.error("emacspeak", "unable to initialize emacspeak speech channel  from registry path " + path + ":no command given");
	    return false;
	}
	if (name.trim().isEmpty())
	    name = "Emacspeak (" + command + ")";
	if (!startProcess())
	{
	    Log.error("emacspeak", "unable to start an emacspeak server for channel \'" + name + "\' with command \'" + command + "\'");
	    return false;
	}
	Log.debug("emacspeak", "emacspeak speech channel with name \'" + name + "\' initialized, command=\'\'" + command + "\'");
	return true;
    }

    @Override public boolean initByArgs(String[] args)
    {
	NullCheck.notNullItems(args, "args");
	if (args.length < 1 || args[0].trim().isEmpty())
	{
	    Log.error("linux", "unable to initialize emacspeak speech channel using string arguments:no command given");
	    return false;
	}
	command = args[0];
	if (args.length >= 2)
	    name = args[1];
	if (args.length >= 3 && args[2].trim().toLowerCase().equals("default"))
	    def = true;
	if (name.trim().isEmpty())
	    name = "Emacspeak (" + command + ")";
	if (!startProcess())
	{
	    Log.error("emacspeak", "unable to start an emacspeak server for channel \'" + name + "\' by command \'" + command + "\'");
	    return false;
	}
	Log.debug("emacspeak", "emacspeak speech channel with name \'" + name + "\' initialized, command=\'\'" + command + "\'");
	return true;
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

    @Override public void setCurrentVoice(String name)
    {
    }

    @Override public Voice[] getVoices()
    {
	return new Voice[0];
    }

    @Override public String getChannelName()
    {
	return name;
    }

    @Override public Set<Features>  getFeatures()
    {
	    return EnumSet.of(Features.CAN_SYNTH_TO_SPEAKERS);
    }

@Override public boolean isDefault()
    {
	return def;
    }

    @Override public int getDefaultPitch()
    {
	return defPitch;
    }

    @Override public void setDefaultPitch(int value)
    {
	defPitch = Channel.adjustParamValue(value);
    }

    @Override public int getDefaultRate()
    {
	return defRate;
    }

    @Override public void setDefaultRate(int value)
    {
	defRate = Channel.adjustParamValue(value);
    }

    @Override public long speak(String text, Listener listener,
				int relPitch, int relRate)
    {
	try {
	    writer.write("q {" + text + "}\n");
	    writer.write("d\n");
	writer.flush();
	stream.flush();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	return -1;
    }

    @Override public long speakLetter(char letter, Listener listener,
 int relPitch, int relRate)
    {
	try {
	    writer.write("l {" + letter + "}\n");
	writer.flush();
	stream.flush();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
	return -1;
    }

    @Override public boolean synth(String text, int pitch, int rate, 
				   AudioFormat format, OutputStream stream)
    {
	return false;
    }

    @Override public AudioFormat[] getSynthSupportedFormats()
    {
	return new AudioFormat[0];
    }

    @Override public void silence()
    {
	try {
	    writer.write("s\n");
	writer.flush();
	stream.flush();
	}
	catch(IOException e)
	{
	    e.printStackTrace();
	}
    }

    @Override public void close()
    {
    }

    private boolean startProcess()
    {
	try {
	process = new ProcessBuilder(command).start();
//	    p.getOutputStream().close();
	stream = process.getOutputStream();
	process.getInputStream().close();

writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

	return true;
	}
	catch(IOException e)
	{
	    Log.error("linux", "unable to start an emacspeak server " + command + ":" + e.getMessage());
	    e.printStackTrace();
	    return false;
	}
    }
}
