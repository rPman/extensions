package org.luwrain.windows.speech;

public class SAPIImplTest
{
	SAPIImpl sapi;
	public static void main(String[] args) throws Exception
	{
		SAPIImpl impl=new SAPIImpl();
		impl.searchVoiceByAttributes(null);
		impl.getNextVoiceIdFromList();
		impl.selectCurrentVoice();

		String id;
		System.out.println(impl.searchVoiceByAttributes(null));
		while((id=impl.getNextVoiceIdFromList())!=null) System.out.println(id+" - "+impl.getLastVoiceDescription());

		impl.speak("Привет, это работает! Hi, it's work!",SAPIImpl_constants.SPF_ASYNC);

		SAPIImpl impl2=new SAPIImpl();
		impl2.searchVoiceByAttributes(null);
		impl2.getNextVoiceIdFromList();
		impl2.selectCurrentVoice();

		impl2.rate(-10);
		impl2.pitch(100);
		impl2.stream("c:\\temp\\rus.wav",SAPIImpl_constants.SPSF_ADPCM_44kHzMono);
		impl2.speak("Привет, это работает!",SAPIImpl_constants.SPF_ASYNC);
		System.out.println(1);
		
		SAPIImpl impl3=new SAPIImpl();
		impl3.searchVoiceByAttributes(null);
		impl3.getNextVoiceIdFromList();
		impl3.selectCurrentVoice();

		impl3.rate(10);
		impl3.stream("c:\\temp\\eng.wav",SAPIImpl_constants.SPSF_ADPCM_44kHzMono);
		impl3.speak("Hi, it's work!",SAPIImpl_constants.SPF_ASYNC);

		System.out.println(2);
		impl2.wait(5000);
		System.out.println(3);
		impl3.wait(5000);
		System.out.println(4);
		impl.wait(5000);
		System.out.println(5);
		//Thread.sleep(3000);
		/*
		// тесты будут работать только если в системе есть голоса RHVoice, проверки на ошибки не проводятся 
		String id;
		System.out.print("Перечислим идентификаторы всех установленных движков, количество:");
		System.out.println(impl.searchVoiceByAttributes(null));
		while((id=impl.getNextVoiceIdFromList())!=null) System.out.println(id);
		// тестируем выбор языка и асинхронную работу
		System.out.println("Ищем Irina+Alan, количество: "+impl.searchVoiceByAttributes("Name=Irina+Alan"));
		System.out.println("get first voice token: "+impl.getNextVoiceIdFromList());
		System.out.println("select voice, result: "+impl.selectCurrentVoice());
		// тестируем асинхронную очередь
		System.out.print("speak Первая фраза - Асинхронно, должна появиться очередь, result: ");
		System.out.println(impl.speak("Это голосовой движок Ирины и Алана, it's right!"));
		System.out.print("sleep 2 sec ");Thread.sleep(2000);System.out.println("ok");
		// тестируем прерывание и очистку очереди
		System.out.println("Ищем Elena, количество: "+impl.searchVoiceByAttributes("Name=Elena"));
		System.out.println("get first voice token: "+impl.getNextVoiceIdFromList());
		System.out.println("select voice, result: "+impl.selectCurrentVoice());

		System.out.println("speak Вторая фраза - Асинхронно, потестируем прерывание и очистку очереди");
		System.out.println("result:"+impl.speak("А это Елена, и нам есть о чем поговорить, долго и нудно"));
		System.out.print("sleep 4 sec ");Thread.sleep(4000);System.out.println("ok");

		System.out.print("выбираем по идентификатору Aleksandr, result:");
		System.out.println(impl.selectVoiceById("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Speech\\Voices\\TokenEnums\\RHVoice\\Aleksandr"));

		System.out.println("speak Третяя фраза. Не асинхронно, с очисткой очереди и остановкой предыдущих текстов");
		System.out.println("result:"+impl.speak("Прервемся",impl.SPF_PURGEBEFORESPEAK));
		System.out.print("sleep 1 sec ");Thread.sleep(1000);System.out.println("ok");

		System.out.println("Ищем Elena, количество: "+impl.searchVoiceByAttributes("Name=Elena"));
		System.out.println("get first voice token: "+impl.getNextVoiceIdFromList());
		System.out.println("select voice, result: "+impl.selectCurrentVoice());
		System.out.print("speak Четвертая фраза - неасинхронно, проверка xml опций, result: ");
		System.out.println(impl.speak("Обычно. <rate absspeed='-5'>Медленно</rate>. <pitch absmiddle='-5'>Ниже тоном</pitch>. <emph>Выделить!</emph>. <spell>По слогам</spell>.",impl.SPF_IS_XML));

		System.out.println("end");
		*/
	}
}
