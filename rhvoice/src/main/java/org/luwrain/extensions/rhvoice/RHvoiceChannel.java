package org.luwrain.extensions.rhvoice;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.luwrain.core.Log;
import org.luwrain.core.NullCheck;
import org.luwrain.core.Registry;
import org.luwrain.core.RegistryProxy;
import org.luwrain.core.events.RunnableEvent;
import org.luwrain.speech.Channel;
import org.luwrain.speech.Voice;
import org.luwrain.speech.Channel.Features;

import com.github.olga_yakovleva.rhvoice.RHVoiceException;
import com.github.olga_yakovleva.rhvoice.SynthesisParameters;
import com.github.olga_yakovleva.rhvoice.TTSClient;
import com.github.olga_yakovleva.rhvoice.TTSEngine;
import com.github.olga_yakovleva.rhvoice.VoiceInfo;

public class RHvoiceChannel implements Channel
{
    //static private final String RHVOICE_ENGINE_PREFIX = "--rhvoice-data-path=";
    static private final String RHVOICE_DATA_PATH = "rhvoice";
    static private final int COPY_WAV_BUF_SIZE=1024;
    static final int AUDIO_LINE_BUFFER_SIZE=3200; // minimal req value is 3200 (1600 samples max give rhvoice and each one 2 byte size
	static final float RHVOICE_FRAME_RATE = 16000f; // 44100 samples/s

    private final RHvoiceChannel impl = new RHvoiceChannel();
    private int curPitch = 100;
    private int curRate = 60;

    private String name = "";
    
    private TTSEngine tts;
    private SynthesisParameters params;
    
    private AudioFormat audioFormat;
    private SourceDataLine audioLine;
    
    @Override public boolean initByRegistry(Registry registry, String path)
    {
    	String voiceName;
    	try {
    	    final Settings options = RegistryProxy.create(registry, path, Settings.class);
    	    name = options.getName(name);
    	    voiceName=options.getVoiceName("");
    	}
    	catch (Exception e)
    	{
    	    Log.error("rhvoice", "unexpected error while initializing the speech channel:" + e.getMessage());
    	    e.printStackTrace();
    	    return false;
    	}
    	return initByArgs(new String[]{voiceName});
    }

    // currently args must contains single string - voice name
    @Override public boolean initByArgs(String[] args)
    {
    	NullCheck.notEmptyArray(args,"rhvoice argument");
    	// init tts	
		try
		{
			tts=new TTSEngine();
		} catch(RHVoiceException e)
		{
			Log.error("rhvoice", "unable to create rhvoice tts speach engine");
			e.printStackTrace();
			return false;
		}
		// select voice
		String voiceName=args[0];
		params=new SynthesisParameters();
		setDefaultPitch(curPitch);
		setDefaultRate(curRate);
		//
		if(voiceName.isEmpty())
		{
			// select first russian/english language from list
			String ruVoice=null;
			String enVoice=null;
			List<VoiceInfo> voices=tts.getVoices();
			for(VoiceInfo voice:voices)
			{
				Log.debug("rhvoice", "found voice: "+voice.getName()+", lang: "+voice.getLanguage().getName()+" (a2s:"+voice.getLanguage().getAlpha2Code()+",a2c:"+voice.getLanguage().getAlpha2CountryCode()+",a3s:"+voice.getLanguage().getAlpha3Code()+",a2c:"+voice.getLanguage().getAlpha3CountryCode()+")");
				if(ruVoice==null&&voice.getLanguage().getName().equals("Russian")) ruVoice=voice.getName();
				if(enVoice==null&&voice.getLanguage().getName().equals("English")) enVoice=voice.getName();
			}
			if(ruVoice==null&&enVoice==null)
			{
				Log.error("rhvoice", "can't find default voice with language Russian or English");
				return false;
			}
			if(ruVoice==null)
				voiceName=enVoice;
			else if(enVoice==null)
				voiceName=ruVoice;
			else
				voiceName=ruVoice+"+"+enVoice;
		}
		params.setVoiceProfile(voiceName);
		Log.debug("rhvoice", "selected voice: "+voiceName);
    	// audio device and player
		try
		{
			audioFormat = new AudioFormat(Encoding.PCM_SIGNED,RHVOICE_FRAME_RATE,Short.SIZE,1,(1*Short.SIZE/8),RHVOICE_FRAME_RATE,false);
	        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
			audioLine=(SourceDataLine) AudioSystem.getLine(info);
	        audioLine.open(audioFormat,AUDIO_LINE_BUFFER_SIZE);
	        audioLine.start();
		} catch(Exception e)
		{
			Log.error("rhvoice", "unable to init audio device");
			e.printStackTrace();
		}
		TTSClient player=new TTSClient()
		{
			@Override public boolean playSpeech(short[] samples)
			{
				try
				{
			        final ByteBuffer buffer=ByteBuffer.allocate(samples.length*audioFormat.getFrameSize());
			        buffer.order(ByteOrder.LITTLE_ENDIAN);
			        buffer.asShortBuffer().put(samples);
			        final byte[] bytes=buffer.array();
			        // here execution would paused, if audioLine buffer is full
			        audioLine.write(bytes,0,bytes.length);
			        
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				return true;
			}
		};

		return true;
    }

    @Override public Voice[] getVoices()
    {
    	Voice[] voices=new Voice[tts.getVoices().size()];
    	int i=0;
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
	return EnumSet.of(Features.CAN_SYNTH_TO_SPEAKERS); // Features.CAN_NOTIFY_WHEN_FINISHED
    }

    @Override public boolean isDefault()
    {
	return false;
    }

    @Override public void setDefaultPitch(int value)
    {
    	curPitch=limit100(value);
    	params.setPitch(curPitch); // todo: check it
    }

    /** convert rate from range 0..100 where 0 slowest, 100 fastest to sapi -10..+10 where -10 is fastest and +10 slowest */
    private int convRate(int rate100)
    {
    	return Math.round((10-rate100/5));
    }
    
    @Override public void setDefaultRate(int value)
    {
    	curRate=limit100(value);
		params.setRate(convRate(curRate));
    }

    /** thread to speak can be restarted or looped (if last speak was stopped by new speak or silence)
     * 
     **/
    class SpeakingThread implements Runnable
    {
    	Thread thread;
    	void execute()
    	{
    		if(threadRun==null)
    		{
    			threadRun=new SpeakingThread();
    			thread=new Thread(threadRun);
    		} else
    		{
    		}
    		thread.start();
    	}
		@Override public void run()
		{
			
		}
    }
    SpeakingThread threadRun=null;
    
    @Override public long speak(String text,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
    	int defPitch=curPitch;
    	int defRate=curRate;
    if(relPitch!=0)
    	setDefaultPitch(curPitch+relPitch);
	if(relRate!=0)
    	setDefaultRate(curRate+relRate);
	//
	

	//
	if(relPitch!=0)
    	setDefaultPitch(defPitch);
	if(relRate!=0)
		setDefaultRate(defRate);
	return -1;
    }

    @Override public long speakLetter(char letter,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
	return speak(""+letter,listener,relPitch,relRate,cancelPrevious);
    }

    @Override public boolean synth(String text,int pitch, int rate,
				   AudioFormat format,OutputStream stream)
    {
	return false;
    }

    @Override public void silence()
    {
    	// FIXME:
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
	// fixme, voice profile not voice, it can have multiple voice for different lang
    }

    @Override public void close()
    {
    	// FIXME:
    }

    private int limit100(int value)
    {
	if(value<0) value=0;
	if(value>100) value=100;
	return value;
    }
}
