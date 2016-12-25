
package org.luwrain.extensions.rhvoice;

import java.nio.*;
import javax.sound.sampled.*;

import com.github.olga_yakovleva.rhvoice.*;

import org.luwrain.core.*;
import org.luwrain.speech.Channel.Listener;

/** thread to speak can be restarted or looped (if last speak was stopped by new speak or silence)
 * 
 **/
class SpeakingThread implements Runnable
{
    private final Listener listener;
    private final String text;
        private final Channel channel;
    boolean interrupt = false;
    boolean finished = false;

    SpeakingThread(String text,Listener listener, Channel channel)
    {
	this.listener = listener;
	this.text = text;
	this.channel = channel;
	}

    @Override public void run()
    {
	synchronized(channel){
	try {
	    channel.tts.speak(text, channel.params, (samples)->{
		    try {
			final ByteBuffer buffer=ByteBuffer.allocate(samples.length * channel.audioFormat.getFrameSize());
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.asShortBuffer().put(samples);
			final byte[] bytes = buffer.array();
			//We can freeze there, if the audio line doesn't have necessary room for new data
			channel.audioLine.write(bytes, 0, bytes.length);
			if(interrupt)
			{
			    channel.audioLine.flush();
			    finished = true;
			    return false;
			}
		    }
		    catch(Exception e)
						    {
							Log.error("rhvoice", "unable to speak");
							e.printStackTrace();
							finished = true;
							return false;
						    }
		    finished = true;
						    return true;
					    });
	    if (!interrupt)
		channel.audioLine.drain();
	    if(listener != null) 
		listener.onFinished(-1);
	    finished = true;
	} 
	catch(RHVoiceException e)
	{
	    if(listener != null) 
		listener.onFinished(-1);
	    finished = true;
	    Log.error("rhvoice", "rhvoice error:" + e.getClass().getName() + ":" + e.getMessage());
	    return;
	}
	}
    }
}
