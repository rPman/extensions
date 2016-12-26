
package org.luwrain.extensions.rhvoice;

import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.Encoding;

import com.github.olga_yakovleva.rhvoice.RHVoiceException;
import com.github.olga_yakovleva.rhvoice.SynthesisParameters;
import com.github.olga_yakovleva.rhvoice.TTSClient;
import com.github.olga_yakovleva.rhvoice.TTSEngine;
import com.github.olga_yakovleva.rhvoice.VoiceInfo;

import org.luwrain.core.*;

class Channel implements org.luwrain.speech.Channel
{
    static private final String LOG_COMPONENT = "rhvoice";

    	static private final int UPPER_CASE_PITCH_MODIFIER = 30;
        static final int AUDIO_LINE_BUFFER_SIZE=3200; // minimal req value is 3200 (1600 samples max give rhvoice and each one 2 byte size
	static final float FRAME_RATE = 24000f;

        static private final double RATE_MIN  = 0.5f;
    static private final double RATE_MAX  = 2.0f;
    static private final double PITCH_MIN = 0.5f;
    static private final double PITCH_MAX = 2.0f;

    private int curPitch = 30;
    private int curRate = 60;

    private String name = "";
    private boolean defaultChannel = false;


    //These variable are accessible for SpeakingThread
TTSEngine tts = null;
SynthesisParameters params = null;
AudioFormat audioFormat = null;
SourceDataLine audioLine = null;

    private SpeakingThread thread = null;

    @Override public boolean initByRegistry(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
	final Settings sett = Settings.create(registry, path);
	name = sett.getName("");
	defaultChannel = sett.getDefault(false);
	if (name.trim().isEmpty())
	{
	    Log.error(LOG_COMPONENT, "channel in " + path + " does not have any name");
	    return false;
	}
    	return initByArgs(new String[]{sett.getVoiceName("")});
    }

    // currently args must contains single string - voice name
    @Override public boolean initByArgs(String[] args)
    {
    	NullCheck.notNullItems(args,"rhvoice argument");
	try {
	    System.loadLibrary("RHVoice_core");
	}
	catch(Exception e)
	{
	    Log.warning(LOG_COMPONENT, "unable to load RHVoice_core:" + e.getClass().getName() + ":" + e.getMessage());
	}
	try {
	    TTSEngine.init();
	    final Path dataPath = Paths.get("rhvoice", "data");
	    final Path configPath = Paths.get("rhvoice", "config");
	    Log.debug(LOG_COMPONENT, "data path:" + dataPath.toString());
	    Log.debug(LOG_COMPONENT, "config path:" + configPath.toString());
	    final Path enPath = Paths.get("data", "languages", "English");
	    final Path ruPath = Paths.get("data", "languages", "Russian");
	    //	    tts=new TTSEngine("rhvoice"+File.separator+"data","rhvoice"+File.separator+"config",new String[]{"data"+File.separator+"languages"+File.separator+"English","data"+File.separator+"languages"+File.separator+"Russian"},null);
	    tts = new TTSEngine(dataPath.toString(), configPath.toString(), new String[]{
		    enPath.toString(),
		    ruPath.toString(),
		}, null);
	} 
	catch(RHVoiceException e)
	{
	    Log.error(LOG_COMPONENT, "rhvoice refuses to initialize:" + e.getClass().getName() + ":" + e.getMessage());
	    e.printStackTrace();
	    return false;
	}
	//Selecting the voice
	final String voiceName = (args.length > 0 && !args[0].isEmpty())?args[0]:suggestVoice();
	if (voiceName.isEmpty())
	{
	    Log.error(LOG_COMPONENT, "unable to choose suitable voice");
	    return false;
	}
	Log.debug(LOG_COMPONENT, "selecting voice \'" + voiceName + "\'");
	params=new SynthesisParameters();
	setDefaultPitch(curPitch);
	setDefaultRate(curRate);
	params.setVoiceProfile(voiceName);
	params.setSSMLMode(true);
	return initAudioOutput();
    }

    private boolean initAudioOutput()
    {
	try {
	    audioFormat  =  new AudioFormat(Encoding.PCM_SIGNED, FRAME_RATE, 
					    Short.SIZE, 1, (1 * Short.SIZE / 8), FRAME_RATE, false);
	    final DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
	    audioLine = (SourceDataLine) AudioSystem.getLine(info);
	    audioLine.open(audioFormat,AUDIO_LINE_BUFFER_SIZE);
	    audioLine.start();
	    return true;
	} 
	catch(Exception e)
	{
	    Log.error(LOG_COMPONENT, "unable to init audio line:" + e.getClass().getName() + ":" + e.getMessage());
	    return false;
	}
    }

    private String suggestVoice()
    {
	String voiceRu = null;
	String voiceEn = null;
	final List<VoiceInfo> voices = tts.getVoices();
	for(VoiceInfo voice: voices)
	{
	    Log.debug(LOG_COMPONENT, "available voice:" + voice.getName() + 
		      ", lang:" +voice.getLanguage().getName() + 
		      " (a2s:" + voice.getLanguage().getAlpha2Code() +
		      ",a2c:" + voice.getLanguage().getAlpha2CountryCode() +
		      ",a3s:"+voice.getLanguage().getAlpha3Code() +
		      ",a2c:"+voice.getLanguage().getAlpha3CountryCode() +
		      ")");
	    if(voiceRu == null && voice.getLanguage().getName().equals("Russian")) 
		voiceRu = voice.getName();
	    if(voiceEn == null && voice.getLanguage().getName().equals("English")) 
		voiceEn = voice.getName();
	}
	if(voiceRu == null && voiceEn == null)
	{
	    Log.warning(LOG_COMPONENT, "no voices neither Russian nor English");
	    return "";
	}
	if(voiceRu == null)
	    return voiceEn;
	if(voiceEn == null)
	    return voiceRu;
	return voiceRu + "+" + voiceEn;
    }

    @Override public org.luwrain.speech.Voice[] getVoices()
    {
    	final org.luwrain.speech.Voice[] voices=new org.luwrain.speech.Voice[tts.getVoices().size()];
    	int i = 0;
    	for(VoiceInfo voice:tts.getVoices())
	    voices[i++]=new RHVoice(voice.getName());
    	return voices;
    }

    @Override public String getChannelName()
    {
	return name;
    }

    @Override public Set<Features> getFeatures()
    {
	return EnumSet.of(Features.CAN_SYNTH_TO_SPEAKERS,Features.CAN_NOTIFY_WHEN_FINISHED); // 
    }

    @Override public boolean isDefault()
    {
	return defaultChannel;
    }

    @Override public void setDefaultPitch(int value)
    {
    	curPitch=limit100(value);
    	params.setPitch(convPitch(curPitch)); // todo: check it
    }

    @Override public void setDefaultRate(int value)
    {
    	curRate=limit100(value);
	params.setRate(convRate(curRate));
    }

    @Override public long speak(String text,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
	NullCheck.notNull(text, "text");
   	int defPitch=curPitch;
   	int defRate=curRate;
	if(relPitch!=0)
	    setDefaultPitch(curPitch+relPitch);
	if(relRate!=0)
	    setDefaultRate(curRate+relRate);
	// make text string to xml with pitch change for uppercase
	// todo:add support for cancelPrevious=false 
   	params.setSSMLMode(false);
	runThread(text,listener);
	if(relPitch != 0)
	    setDefaultPitch(defPitch);
	if(relRate != 0)
	    setDefaultRate(defRate);
	return -1;
    }

    @Override public long speakLetter(char letter,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
   	int defPitch=curPitch;
   	int defRate=curRate;
	if(relPitch!=0)
	    setDefaultPitch(curPitch+relPitch);
   	if(relRate!=0)
	    setDefaultRate(curRate+relRate);
   	// make text string to xml with pitch change for uppercase
   	// todo:add support for cancelPrevious=false
   	params.setSSMLMode(true);
	runThread(SSML.upperCasePitchControl(""+letter,UPPER_CASE_PITCH_MODIFIER), listener);
   	if(relPitch!=0)
	    setDefaultPitch(defPitch);
   	if(relRate!=0)
	    setDefaultRate(defRate);
   	return -1;
    }

private void runThread(String text, Listener listener)
{
    if (thread != null)
    {
	thread.interrupt = true;
	//while (!thread.finished);
    }
    thread = new SpeakingThread(text, listener, this);
    new Thread(thread).start();
	}

    @Override public boolean synth(String text,int pitch, int rate,
				   AudioFormat format,OutputStream stream)
    {
	return false;
    }

    @Override public void silence()
    {
	if (thread != null)
	{
	    thread.interrupt = true;
	    //	    while(!thread.finished);
	}
	thread = null;
	}

    @Override public AudioFormat[] getSynthSupportedFormats()
    {
	return null;
    }

    @Override public void setCurrentPuncMode(PuncMode mode)
    {
    }

    @Override public PuncMode getCurrentPuncMode()
    {
	return null;
    }

    @Override public int getDefaultRate()
    {
	return 50;
    }

    @Override public int getDefaultPitch()
    {
	return 100;
    }

    @Override public void setCurrentVoice(String name)
    {
    }

    @Override public String getCurrentVoiceName()
    {
	return params.getVoiceProfile();
    }

    @Override public void close()
    {
	silence();
	//FIXME:
    }

    private int limit100(int value)
    {
	if(value<0) value=0;
	if(value>100) value=100;
	return value;
    }

    private double convRate(int value)
    {
	final double range = RATE_MAX - RATE_MIN;
    	return RATE_MIN + range - (double)value * range / 100f;
    }

    private double convPitch(int value)
    { // 0.5 ... 2
	final double range = PITCH_MAX - PITCH_MIN;
    	return PITCH_MIN + (double)value * range / 100f;
    }
}
