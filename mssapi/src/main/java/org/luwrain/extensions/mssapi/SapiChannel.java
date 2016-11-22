
package org.luwrain.extensions.mssapi;

import java.io.*;
import java.util.*;
import javax.sound.sampled.AudioFormat;

import org.luwrain.core.*;
import org.luwrain.speech.*;

class SapiChannel implements Channel
{
    static private final String LOG_COMPONENT = "mssapi";

    static private final String SAPI_ENGINE_PREFIX = "--sapi-engine=";
    static private final int COPY_WAV_BUF_SIZE=1024;

    private final SAPIImpl impl = new SAPIImpl();
    private int curPitch = 100;
    private int curRate = 60;

    private boolean def = false;
    private String name = "";
    private File tempFile;

    private String getAttrs(String[] cmdLine)
    {
	if (cmdLine == null)
	    return null;
	for(String s: cmdLine)
	{
	    if (s == null)
		continue;
	    if (s.startsWith(SAPI_ENGINE_PREFIX))
		return s.substring(SAPI_ENGINE_PREFIX.length());
	}
	return null;
    }

    @Override public boolean initByRegistry(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notEmpty(path, "path");
    	    final Settings sett = Settings.create(registry, path);
	    def = sett.getDefault(false);
    	    name = sett.getName("");
    	    final String cond = sett.getCond("");
	return initByArgs(new String[]{cond});
    }

    @Override public boolean initByArgs(String[] args)
    {
	NullCheck.notNullItems(args, "args");
    	int cnt=impl.searchVoiceByAttributes(args==null?null:String.join(";",args));
	if(cnt == 0)
	{
	    Log.warning("windows", "no voice with specified attributes, use default");
	} else
	    if(cnt == -1)
	    {
		Log.error("windows", "unable to find a suitable voice due to unexpected error");
		return false;
	    }
    	String voiceId = impl.getNextVoiceIdFromList();
	int res = impl.selectCurrentVoice();
		if(res != 0)
		{
		    Log.error("windows", "unable to select the voice which has been found");
		    return false;
		}
		// FIXME: only for wav gen 
		try {
		    tempFile = File.createTempFile(name,"tmpwav");
		} 
		catch(IOException e)
		{
		    Log.error("windows", "unable to create a temporary file");
		    e.printStackTrace();
		    return false;
		}
		return true;
    }

    @Override public Voice[] getVoices()
    {
	impl.searchVoiceByAttributes(null);
	final LinkedList<Voice> voices = new LinkedList<Voice>(); 
	String id;
	while((id = impl.getNextVoiceIdFromList()) != null)
	    voices.add(new SAPIVoice(id,impl.getLastVoiceDescription(),false)); // FIXME: get male flag from SAPI if it possible
	return voices.toArray(new Voice[voices.size()]);
    }

    @Override public String getChannelName()
    {
	return name;
    }

    @Override public Set<Features> getFeatures()
    {
	return EnumSet.of(Features.CAN_SYNTH_TO_STREAM, Features.CAN_SYNTH_TO_SPEAKERS); // Features.CAN_NOTIFY_WHEN_FINISHED
    }

    @Override public boolean isDefault()
    {
	return def;
    }

    @Override public void setDefaultPitch(int value)
    {
    	curPitch=limit100(value);
    	impl.pitch(curPitch);
    }

    /** convert rate from range 0..100 where 0 slowest, 100 fastest to sapi -10..+10 where -10 is fastest and +10 slowest */
    private int convRate(int rate100)
    {
    	return Math.round((10-rate100/5));
    }

    @Override public void setDefaultRate(int value)
    {
    	curRate=limit100(value);
		impl.rate(convRate(curRate));
    }

    @Override public long speak(String text,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
	NullCheck.notNull(text, "text");
    if(relPitch!=0)
    	impl.pitch(limit100(curPitch+relPitch));
	if(relRate!=0)
		impl.rate(convRate(limit100(curRate+relRate)));
	impl.speak(text,SAPIImpl_constants.SPF_ASYNC|SAPIImpl_constants.SPF_IS_NOT_XML|(cancelPrevious?SAPIImpl_constants.SPF_PURGEBEFORESPEAK:0));
	if(relPitch!=0)
		impl.pitch(curPitch);
	if(relRate!=0)
		impl.rate(convRate(curRate));
	return -1;
    }

    @Override public long speakLetter(char letter,Listener listener,int relPitch,int relRate, boolean cancelPrevious)
    {
	return speak(""+letter,listener,relPitch,relRate,cancelPrevious);
    }

    @Override public boolean synth(String text,int pitch, int rate,
				   AudioFormat format,OutputStream stream)
    {
	impl.stream(tempFile.getPath(),chooseSAPIAudioFormatFlag(format));
	if(pitch!=0)
		impl.pitch(limit100(curPitch+pitch));
	if(rate!=0)
		impl.rate(convRate(limit100(curRate+rate)));
	impl.speak(text,SAPIImpl_constants.SPF_IS_NOT_XML);
	if(pitch!=0)
		impl.pitch(curPitch);
	if(rate!=0)
		impl.rate(convRate(curRate));
	impl.stream(null,SAPIImpl_constants.SPSF_Default);
	//Copying the whole file to the stream, except of 44 wave header
	try {
	    final FileInputStream is=new FileInputStream(tempFile.getPath());
	    final byte[] buf=new byte[COPY_WAV_BUF_SIZE];
	    while(true)
	    {
		final int len=is.read(buf);
		if(len == -1)
		    break;
		stream.write(buf,0,len);
	    }
	} catch(Exception e)
	{
	    Log.warning("windows", "unable to copy synthesized data:" + e.getMessage());
	    return false;
	}
	return true;
    }

    @Override public void silence()
    {
    	impl.speak("", SAPIImpl.SPF_PURGEBEFORESPEAK);
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
	return 0;
    }

    @Override public int getDefaultPitch()
    {
	return 0;
    }

    @Override public void setCurrentVoice(String name)
    {
    }

    @Override public String getCurrentVoiceName()
    {
	return "default";
    }

    @Override public void close()
    {
    }

    private int limit100(int value)
    {
	if(value<0) value=0;
	if(value>100) value=100;
	return value;
    }

    private int chooseSAPIAudioFormatFlag(AudioFormat format)
    {
	int sapiaudio=SAPIImpl_constants.SPSF_Default;
	if(format.getChannels()==1)
	{ // mono
	    if(format.getSampleSizeInBits()==8)
	    {
		if(format.getFrameRate()<= 8000000) sapiaudio=SAPIImpl_constants.SPSF_8kHz8BitMono;else
		    if(format.getFrameRate()<=11000000) sapiaudio=SAPIImpl_constants.SPSF_11kHz8BitMono;else
			if(format.getFrameRate()<=12000000) sapiaudio=SAPIImpl_constants.SPSF_12kHz8BitMono;else
			    if(format.getFrameRate()<=16000000) sapiaudio=SAPIImpl_constants.SPSF_16kHz8BitMono;else
				if(format.getFrameRate()<=22000000) sapiaudio=SAPIImpl_constants.SPSF_22kHz8BitMono;else
				    if(format.getFrameRate()<=24000000) sapiaudio=SAPIImpl_constants.SPSF_24kHz8BitMono;else
					if(format.getFrameRate()<=32000000) sapiaudio=SAPIImpl_constants.SPSF_32kHz8BitMono;else
					    if(format.getFrameRate()<=44000000) sapiaudio=SAPIImpl_constants.SPSF_44kHz8BitMono;else
						if(format.getFrameRate()<=48000000) sapiaudio=SAPIImpl_constants.SPSF_48kHz8BitMono;else
						    Log.warning("sapi","Audioformat sample frame too big "+format.getFrameRate());
	    } else
		if(format.getSampleSizeInBits()==16)
		{
		    if(format.getFrameRate()<= 8000000) sapiaudio=SAPIImpl_constants.SPSF_8kHz16BitMono;else
			if(format.getFrameRate()<=11000000) sapiaudio=SAPIImpl_constants.SPSF_11kHz16BitMono;else
			    if(format.getFrameRate()<=12000000) sapiaudio=SAPIImpl_constants.SPSF_12kHz16BitMono;else
				if(format.getFrameRate()<=16000000) sapiaudio=SAPIImpl_constants.SPSF_16kHz16BitMono;else
				    if(format.getFrameRate()<=22000000) sapiaudio=SAPIImpl_constants.SPSF_22kHz16BitMono;else
					if(format.getFrameRate()<=24000000) sapiaudio=SAPIImpl_constants.SPSF_24kHz16BitMono;else
					    if(format.getFrameRate()<=32000000) sapiaudio=SAPIImpl_constants.SPSF_32kHz16BitMono;else
						if(format.getFrameRate()<=44000000) sapiaudio=SAPIImpl_constants.SPSF_44kHz16BitMono;else
						    if(format.getFrameRate()<=48000000) sapiaudio=SAPIImpl_constants.SPSF_48kHz16BitMono;else
							Log.warning("sapi","Audioformat sample frame too big "+format.getFrameRate());
		} else
		{
		    Log.warning("sapi","Audioformat sample size can be 8 or 16 bit, but specified "+format.getSampleSizeInBits());
		}
	} else
	{ // stereo
	    if(format.getSampleSizeInBits()==8)
	    {
		if(format.getFrameRate()<= 8000000) sapiaudio=SAPIImpl_constants.SPSF_8kHz8BitStereo;else
		    if(format.getFrameRate()<=11000000) sapiaudio=SAPIImpl_constants.SPSF_11kHz8BitStereo;else
			if(format.getFrameRate()<=12000000) sapiaudio=SAPIImpl_constants.SPSF_12kHz8BitStereo;else
			    if(format.getFrameRate()<=16000000) sapiaudio=SAPIImpl_constants.SPSF_16kHz8BitStereo;else
				if(format.getFrameRate()<=22000000) sapiaudio=SAPIImpl_constants.SPSF_22kHz8BitStereo;else
				    if(format.getFrameRate()<=24000000) sapiaudio=SAPIImpl_constants.SPSF_24kHz8BitStereo;else
					if(format.getFrameRate()<=32000000) sapiaudio=SAPIImpl_constants.SPSF_32kHz8BitStereo;else
					    if(format.getFrameRate()<=44000000) sapiaudio=SAPIImpl_constants.SPSF_44kHz8BitStereo;else
						if(format.getFrameRate()<=48000000) sapiaudio=SAPIImpl_constants.SPSF_48kHz8BitStereo;else
						    Log.warning("sapi","Audioformat sample frame too big "+format.getFrameRate());
	    } else
		if(format.getSampleSizeInBits()==16)
		{
		    if(format.getFrameRate()<= 8000000) sapiaudio=SAPIImpl_constants.SPSF_8kHz16BitStereo;else
			if(format.getFrameRate()<=11000000) sapiaudio=SAPIImpl_constants.SPSF_11kHz16BitStereo;else
			    if(format.getFrameRate()<=12000000) sapiaudio=SAPIImpl_constants.SPSF_12kHz16BitStereo;else
				if(format.getFrameRate()<=16000000) sapiaudio=SAPIImpl_constants.SPSF_16kHz16BitStereo;else
				    if(format.getFrameRate()<=22000000) sapiaudio=SAPIImpl_constants.SPSF_22kHz16BitStereo;else
					if(format.getFrameRate()<=24000000) sapiaudio=SAPIImpl_constants.SPSF_24kHz16BitStereo;else
					    if(format.getFrameRate()<=32000000) sapiaudio=SAPIImpl_constants.SPSF_32kHz16BitStereo;else
						if(format.getFrameRate()<=44000000) sapiaudio=SAPIImpl_constants.SPSF_44kHz16BitStereo;else
						    if(format.getFrameRate()<=48000000) sapiaudio=SAPIImpl_constants.SPSF_48kHz16BitStereo;else
							Log.warning("sapi","Audioformat sample frame too big "+format.getFrameRate());
		} else
		{
		    Log.warning("sapi","Audioformat sample size can be 8 or 16 bit, but specified "+format.getSampleSizeInBits());
		}
	}
	return sapiaudio;
    }
}
