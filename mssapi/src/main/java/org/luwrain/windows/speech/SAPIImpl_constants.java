/*
   Copyright 2015-2016 Roman Volovodov <gr.rPman@gmail.com>
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

package org.luwrain.windows.speech;

class  SAPIImpl_constants
{
	// link to documentation https://msdn.microsoft.com/en-us/library/ee431843%28v=vs.85%29.aspx
	public static final int SPF_DEFAULT = 0;
	public static final int SPF_IS_XML = 8;
	public static final int SPF_IS_NOT_XML = 16;
	public static final int SPF_ASYNC = 1;
	public static final int SPF_PURGEBEFORESPEAK = 2;
	public static final int SPF_IS_FILENAME = 4;
	public static final int SPF_NLP_SPEAK_PUNC = 64;
	public static final int SPF_PARSE_SAPI = 128;
	public static final int SPF_PARSE_SSML = 256;
	public static final int SPSF_Default = -1;
	public static final int SPSF_NoAssignedFormat = 0;
	public static final int SPSF_Text = 1;
	public static final int SPSF_NonStandardFormat = 2;
	public static final int SPSF_ExtendedAudioFormat = 3;
	public static final int SPSF_8kHz8BitMono = 4;
	public static final int SPSF_8kHz8BitStereo = 5;
	public static final int SPSF_8kHz16BitMono = 6;
	public static final int SPSF_8kHz16BitStereo = 7;
	public static final int SPSF_11kHz8BitMono = 8;
	public static final int SPSF_11kHz8BitStereo = 9;
	public static final int SPSF_11kHz16BitMono = 10;
	public static final int SPSF_11kHz16BitStereo = 11;
	public static final int SPSF_12kHz8BitMono = 12;
	public static final int SPSF_12kHz8BitStereo = 13;
	public static final int SPSF_12kHz16BitMono = 14;
	public static final int SPSF_12kHz16BitStereo = 15;
	public static final int SPSF_16kHz8BitMono = 16;
	public static final int SPSF_16kHz8BitStereo = 17;
	public static final int SPSF_16kHz16BitMono = 18;
	public static final int SPSF_16kHz16BitStereo = 19;
	public static final int SPSF_22kHz8BitMono = 20;
	public static final int SPSF_22kHz8BitStereo = 21;
	public static final int SPSF_22kHz16BitMono = 22;
	public static final int SPSF_22kHz16BitStereo = 23;
	public static final int SPSF_24kHz8BitMono = 24;
	public static final int SPSF_24kHz8BitStereo = 25;
	public static final int SPSF_24kHz16BitMono = 26;
	public static final int SPSF_24kHz16BitStereo = 27;
	public static final int SPSF_32kHz8BitMono = 28;
	public static final int SPSF_32kHz8BitStereo = 29;
	public static final int SPSF_32kHz16BitMono = 30;
	public static final int SPSF_32kHz16BitStereo = 31;
	public static final int SPSF_44kHz8BitMono = 32;
	public static final int SPSF_44kHz8BitStereo = 33;
	public static final int SPSF_44kHz16BitMono = 34;
	public static final int SPSF_44kHz16BitStereo = 35;
	public static final int SPSF_48kHz8BitMono = 36;
	public static final int SPSF_48kHz8BitStereo = 37;
	public static final int SPSF_48kHz16BitMono = 38;
	public static final int SPSF_48kHz16BitStereo = 39;
	public static final int SPSF_TrueSpeech_8kHz1BitMono = 40;
	public static final int SPSF_CCITT_ALaw_8kHzMono = 41;
	public static final int SPSF_CCITT_ALaw_8kHzStereo = 42;
	public static final int SPSF_CCITT_ALaw_11kHzMono = 43;
	public static final int SPSF_CCITT_ALaw_11kHzStereo = 44;
	public static final int SPSF_CCITT_ALaw_22kHzMono = 45;
	public static final int SPSF_CCITT_ALaw_22kHzStereo = 46;
	public static final int SPSF_CCITT_ALaw_44kHzMono = 47;
	public static final int SPSF_CCITT_ALaw_44kHzStereo = 48;
	public static final int SPSF_CCITT_uLaw_8kHzMono = 49;
	public static final int SPSF_CCITT_uLaw_8kHzStereo = 50;
	public static final int SPSF_CCITT_uLaw_11kHzMono = 51;
	public static final int SPSF_CCITT_uLaw_11kHzStereo = 52;
	public static final int SPSF_CCITT_uLaw_22kHzMono = 53;
	public static final int SPSF_CCITT_uLaw_22kHzStereo = 54;
	public static final int SPSF_CCITT_uLaw_44kHzMono = 55;
	public static final int SPSF_CCITT_uLaw_44kHzStereo = 56;
	public static final int SPSF_ADPCM_8kHzMono = 57;
	public static final int SPSF_ADPCM_8kHzStereo = 58;
	public static final int SPSF_ADPCM_11kHzMono = 59;
	public static final int SPSF_ADPCM_11kHzStereo = 60;
	public static final int SPSF_ADPCM_22kHzMono = 61;
	public static final int SPSF_ADPCM_22kHzStereo = 62;
	public static final int SPSF_ADPCM_44kHzMono = 63;
	public static final int SPSF_ADPCM_44kHzStereo = 64;
	public static final int SPSF_GSM610_8kHzMono = 65;
	public static final int SPSF_GSM610_11kHzMono = 66;
	public static final int SPSF_GSM610_22kHzMono = 67;
	public static final int SPSF_GSM610_44kHzMono = 68;
	public static final int SPSF_NUM_FORMATS = 69;
}
