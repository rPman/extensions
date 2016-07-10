// SAPIImpl.cpp : Defines the exported functions for the DLL application.

#include "stdafx.h"

#include <sapi.h>
#pragma warning (disable:4996) 
#include <sphelper.h>

#include <map>

#include "org_luwrain_windows_speech_SAPIImpl.h"

// классы помошники
// автоматически очищяет память указанного объекта, созданного с помощью new
class Autocleaner{ public: void* resource; Autocleaner(void* to_clean){ resource = to_clean; }; ~Autocleaner(){ delete resource; } };
class CoAutocleaner{ public: void* resource; CoAutocleaner(void* to_clean){ resource = to_clean; }; ~CoAutocleaner(){ CoTaskMemFree(resource); } };

// заворачиваем весь функционал в класс, только для синтаксического удобства организации кода
class SAPIImpl
{
public:
	bool skipInit = false;
	CComPtr<IEnumSpObjectTokens> cpIEnum;
	CComPtr<ISpObjectToken> cpToken = NULL;
	CComPtr<ISpVoice> cpVoice;
	HRESULT hr;
	
	CSpStreamFormat cAudioFmt;
	CComPtr<ISpStream> cpStream = NULL;

	LPWSTR lastVoiceToketDescription = NULL;

	void init()
	{
		if (skipInit) return;
		skipInit = true;
		// todo: корректная обработка ошибок иннициализации
		hr = ::CoInitialize(NULL);
		//
		hr = CoCreateInstance(CLSID_SpVoice, NULL, CLSCTX_ALL, IID_ISpVoice, (void **)&cpVoice);
		//hr = SpCreateBestObject(SPCAT_VOICES, NULL, NULL, &cpVoice);
	}
	~SAPIImpl()
	{
		if (cpStream != NULL)
		{
			hr = cpStream->Close();
			cpStream.Release();
			cpStream = NULL;
		}
		if (cpToken != NULL)
		{
			cpToken.Release(); cpToken = NULL;
			cpIEnum.Release();
		}
		cpVoice.Release();
	}
	jint searchVoiceToken(jchar* cond)
	{
		cpIEnum.Release();
//		MessageBox(NULL,(LPWSTR)cond,L"debug",MB_OK);
		hr = SpEnumTokens(SPCAT_VOICES, (WCHAR*)cond, NULL, &cpIEnum);
		if (hr != S_OK) return -1; // если ошибка, вернем количество - 0 todo: добавить обработку ошибок
		// определяем количество
		jint cnt = 0;
		hr = cpIEnum->GetCount((ULONG*)&cnt);
		if (hr != S_OK) return -1; // если ошибка, вернем количество - 0 todo: добавить обработку ошибок
		return cnt;
	}
	jint selectVoiceById(jchar* id)
	{
		// чистим предыдущий токен, если был
		if (cpToken != NULL) { cpToken.Release(); cpToken = NULL; }
		// выбираем токен по id
		hr = SpGetTokenFromId((WCHAR*)id,&cpToken);
		if (hr != S_OK) return 1;
		// устанавливаем его как текущий голос
		hr = cpVoice->SetVoice(cpToken);
		if (hr != S_OK) return 2;
		return 0;
	}
	jint selectCurrentVoice()
	{
		// устанавливаем текущий голос
		hr = cpVoice->SetVoice(cpToken);
		if (hr != S_OK) return 1;
		return 0;
	}
	void setIdStringFromVoiceListNext(LPWSTR* id)
	{
		// чистим предыдущий токен, если был
		if (cpToken != NULL) { cpToken.Release(); cpToken = NULL; }
		// получаем следующий
		hr = cpIEnum->Next(1, &cpToken, NULL);
		if (hr != S_OK) return;
		// получаем идентификатор токена (тут выделяется память под струку)
		hr = cpToken->GetId(id);
		// получаем описание токена (наименование голоса)
		SpGetDescription(cpToken, &lastVoiceToketDescription, NULL);

	}
	jint speak(jchar* text, jint flags)
	{
		hr = cpVoice->Speak((LPWSTR)text, flags, NULL);
		if (hr != S_OK) return 1;
		return 0;
	}

	jint stream(jchar* stream, jint flags)
	{
		if (stream == NULL)
		{
			if (cpStream != NULL) { hr = cpStream->Close(); cpStream.Release(); cpStream = NULL; }
			hr = cpVoice->SetOutput(NULL, TRUE);
			if (hr != S_OK) return 3;
		}
		else
		{
			hr = cAudioFmt.AssignFormat((SPSTREAMFORMAT)flags);
			if (hr != S_OK) return 1;
			if (cpStream != NULL) { hr = cpStream->Close(); cpStream.Release(); cpStream = NULL; }
			hr = SPBindToFile((LPWSTR)stream, SPFM_CREATE_ALWAYS, &cpStream, &cAudioFmt.FormatId(), cAudioFmt.WaveFormatExPtr());
			if (hr != S_OK) return 2;
			hr = cpVoice->SetOutput(cpStream, TRUE);
			if (hr != S_OK) return 3;
		}
		return 0;
	}

	jint set_rate(jint rate)
	{ // -10 .. 10
		hr = cpVoice->SetRate(rate);
		if (hr != S_OK) return 1;
		return 0;
	}
	jint set_pitch(jint pitch)
	{ // 0..100
		hr = cpVoice->SetVolume((USHORT)pitch);
		if (hr != S_OK) return 1;
		return 0;
	}
	jint wait_done(jint timeout)
	{
		hr = cpVoice->WaitUntilDone(timeout);
		if (hr != S_OK) return 1;
		return 0;
	}
};

bool doStaticInit = true;
jfieldID idField;
std::map<int, SAPIImpl*> sList;

SAPIImpl& make_impl(JNIEnv * jni,jobject &jthis)
{
	if (doStaticInit)
	{
		jclass c = jni->GetObjectClass(jthis);
		idField = jni->GetFieldID(c, "id", "I");
	}
	int id = jni->GetIntField(jthis, idField);

//	printf("make_impl(%d)\n", id);
	if (sList.find(id) == sList.end()) sList[id] = new SAPIImpl();
	SAPIImpl& impl = *sList[id];
	impl.init();
	return impl;
}
void release_impl(JNIEnv * jni,jobject &jthis)
{
	int id = jni->GetIntField(jthis, idField);
	std::map<int, SAPIImpl*>::iterator it = sList.find(id);

	if (it == sList.end()) return;
	delete it->second;
	sList.erase(it);
}

JNIEXPORT jstring JNICALL Java_org_luwrain_windows_speech_SAPIImpl_getLastVoiceDescription(JNIEnv * jni, jobject jthis)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	if (impl.lastVoiceToketDescription == NULL) return NULL;
	// формируем строку для возврата в java
	return jni->NewString((jchar*)impl.lastVoiceToketDescription, (jsize)wcslen(impl.lastVoiceToketDescription));
}
JNIEXPORT jstring JNICALL Java_org_luwrain_windows_speech_SAPIImpl_getNextVoiceIdFromList(JNIEnv * jni, jobject jthis)
{
	SAPIImpl& impl = make_impl(jni,jthis);
	//
	LPWSTR id = NULL;
	new CoAutocleaner(id); // чуть раньше времени задаем метод автоочистки памяти
	impl.setIdStringFromVoiceListNext(&id);
	if (id == NULL) return NULL;
	// формируем строку для возврата в java
	return jni->NewString((jchar*)id, (jsize)wcslen(id));
}
JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_selectCurrentVoice(JNIEnv * jni, jobject jthis)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	//
	// выбираем текущий токен
	return impl.selectCurrentVoice();
}
JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_selectVoiceById(JNIEnv * jni, jobject jthis, jstring jtext)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	//
	// выгружаем строку с условием
	jsize length = jni->GetStringLength(jtext);
	jchar* text = new jchar[length + 1];
	new Autocleaner(text);
	jni->GetStringRegion(jtext, 0, length, text);
	text[length] = L'\x0';
	// выбираем токен по идентификатору
	return impl.selectVoiceById(text);
}
JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_searchVoiceByAttributes(JNIEnv * jni, jobject jthis, jstring jtext)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	//
	// если критерии не указаны (выдан null) то делаем запрос на все токены
	if (jtext == NULL) return impl.searchVoiceToken(NULL);
	// выгружаем строку с условием
	jsize length = jni->GetStringLength(jtext);
	jchar* text = new jchar[length + 1];
	new Autocleaner(text);
	jni->GetStringRegion(jtext, 0, length, text);
	text[length] = L'\x0';
	// запрос на токены по критериям
	return impl.searchVoiceToken(text);
}
JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_speak(JNIEnv * jni, jobject jthis, jstring jtext, jint flags)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	// выгружаем строку с условием
	jsize length = jni->GetStringLength(jtext);
	jchar* text = new jchar[length + 1];
	new Autocleaner(text);
	jni->GetStringRegion(jtext, 0, length, text);
	text[length] = L'\x0';
	// говорим
	return impl.speak(text, flags);
}

JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_stream(JNIEnv * jni, jobject jthis, jstring jstream, jint flags)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	// выгружаем строку с условием
	jchar* stream = NULL;
	if (jstream != NULL)
	{
		jsize length = jni->GetStringLength(jstream);
		stream = new jchar[length + 1];
		new Autocleaner(stream);
		jni->GetStringRegion(jstream, 0, length, stream);
		stream[length] = L'\x0';
	}
	// устанавливаем, куда выводим звук (null - звуковая карта или путь к wav файлу)
	return impl.stream(stream, flags);
}

JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_rate(JNIEnv * jni, jobject jthis, jint rate)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	return impl.set_rate(rate);
}

JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_pitch(JNIEnv * jni, jobject jthis, jint pitch)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	return impl.set_pitch(pitch);
}

JNIEXPORT jint JNICALL Java_org_luwrain_windows_speech_SAPIImpl_wait(JNIEnv * jni, jobject jthis, jint timeout)
{
	SAPIImpl& impl = make_impl(jni, jthis);
	return impl.wait_done(timeout);
}
