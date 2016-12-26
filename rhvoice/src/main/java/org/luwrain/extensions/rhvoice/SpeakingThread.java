
package org.luwrain.extensions.rhvoice;

import java.nio.*;
import javax.sound.sampled.*;

import com.github.olga_yakovleva.rhvoice.*;

import org.luwrain.core.*;
import org.luwrain.speech.Channel.Listener;

class SpeakingThread implements Runnable
{
    private final Listener listener;
    private final String text;
    private final Channel channel;
    boolean interrupt = false;

    SpeakingThread(String text,Listener listener, Channel channel)
    {
	NullCheck.notNull(text, "text");
	this.listener = listener;
	this.text = text;
	this.channel = channel;
    }

    @Override public void run()
    {
	Log.debug("problem", "text " + text);
	synchronized(channel){
	    try {
		channel.tts.speak(text, channel.params, (samples)->{
			try {
			    final ByteBuffer buffer=ByteBuffer.allocate(samples.length * channel.audioFormat.getFrameSize());
			    buffer.order(ByteOrder.LITTLE_ENDIAN);
			    buffer.asShortBuffer().put(samples);
			    final byte[] bytes = buffer.array();
			    //We can freeze there, if the audio line doesn't have necessary room for new data
			    Log.debug("problem", "write");
			    channel.audioLine.write(bytes, 0, bytes.length);
			    if(interrupt)
			    {
				channel.audioLine.flush();
				return false;
			    }
			}
			catch(Exception e)
			{
			    Log.error("rhvoice", "unable to speak");
			    e.printStackTrace();
			    return false;
			}
			return true;
		    });
		if (!interrupt)
		    channel.audioLine.drain();
		if(listener != null) 
		    listener.onFinished(-1);
	    } 
	    catch(RHVoiceException e)
	    {
		if(listener != null) 
		    listener.onFinished(-1);
		Log.error("rhvoice", "rhvoice error:" + e.getClass().getName() + ":" + e.getMessage());
		return;
	    }
	}
    }
}
