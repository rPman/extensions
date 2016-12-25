
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
    private Listener listener;
    private Thread thread;
    String text = null;
    boolean interrupt = false;
    private Channel channel;

    public void speak(String text,Listener listener, Channel channel)
    {
	this.listener = listener;
	this.text = text;
	this.channel = channel;
	if(thread == null || !thread.isAlive())
	{
	    thread = new Thread(this);
	    thread.start();
			} else
			{
				// TODO: check it in multithreading
    			interrupt = true;
			}
    	}

    @Override public void run()
    {
	while(text != null)
	{
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
					// we need to set text to null if it is not break (if interrupt=true , text have new message to speak)
					if(!interrupt)
					    text=null;
					interrupt = false;
					// finish speaking buffer
					channel.audioLine.drain();
					//audioLine.close();
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
