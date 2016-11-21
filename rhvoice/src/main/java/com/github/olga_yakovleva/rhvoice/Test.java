package com.github.olga_yakovleva.rhvoice;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class Test {

	public static void main(String[] args) {
		
		try
		{
			new Test().go();
		} catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	private void go() throws Exception
	{
		TTSEngine tts=new TTSEngine();
		List<VoiceInfo> voices=tts.getVoices();
		for(VoiceInfo voice:voices)
		{
			System.out.println(voice.getName()+" lang:"+voice.getLanguage().getName());
		}
		SynthesisParameters params=new SynthesisParameters();
		params.setVoiceProfile("SLT+Irina+Natia");
		//params.setRate();
		params.setSSMLMode(false);

		float frameRate = 16000; // 44100 samples/s
		AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED,frameRate,Short.SIZE,1,(1*Short.SIZE/8),frameRate,false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format,1024*1024);
        line.start();
        
		TTSClient player=new TTSClient()
		{
			
			@Override public boolean playSpeech(short[] samples)
			{
				System.out.print("["+samples.length);
				try
				{
			        final ByteBuffer buffer=ByteBuffer.allocate(samples.length*format.getFrameSize());
			        buffer.order(ByteOrder.LITTLE_ENDIAN);
			        buffer.asShortBuffer().put(samples);
			        final byte[] bytes=buffer.array();

			        line.write(bytes,0,bytes.length);
			        
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				System.out.print("] ");
				return true;
			}
		};
		tts.speak("ქართული ენის განმარტებითი ლექსიკონი. Hi, i am Elena from RHVoice. Привет я Елена из проекта RHVoice",params,player);
        line.drain();
        line.close();

        System.out.println("sleep1");
		
        Thread.sleep(3000);
		
	}
	
	private void audioInit(short[] samples) throws Exception
	{
		float frameRate = 16000; // 44100 samples/s
		int channels = 1;
		int sampleBytes = Short.SIZE/8;
		int frameBytes = sampleBytes * channels;
		AudioFormat format =
		    new AudioFormat(Encoding.PCM_SIGNED,
		                    frameRate,
		                    Short.SIZE,
		                    channels,
		                    frameBytes,
		                    frameRate,
		                    false);

        final ByteBuffer buffer=ByteBuffer.allocate(samples.length*sampleBytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(samples);
        final byte[] bytes=buffer.array();

		AudioInputStream stream =
		    new AudioInputStream(new ByteArrayInputStream(bytes), format, bytes.length);
		Clip clip = AudioSystem.getClip();
		clip.open(stream);
		clip.start();
		Thread.sleep((long)(1000*samples.length/frameRate));
		//clip.drain();
		clip.flush();
	}

}
