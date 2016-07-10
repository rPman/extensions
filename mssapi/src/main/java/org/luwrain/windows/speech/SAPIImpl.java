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

/**
 * @author Volovodov Roman
 * Класс, определяющий доступ к библиотеке-прослойке SAPI, для синтеза речи
 * 
 * Для получения списка голосовых движков необходимо последовательно вызывать SAPIImpl.getNextVoiceIdFromList(); до тех пор, пока не будет возвращен null
 *  SAPIImpl.searchVoiceByAttributes(null);
 *  while(string id=SAPIImpl.getNextVoiceIdFromList()) System.out.println(id);
 *
 * Простейший сценарий использования
 * 	SAPIImpl.searchVoiceByAttributes("Name=Irina+Alan");	// выбираем голосовой движок по атрибуту name
 *  SAPIImpl.getNextVoiceIdFromList();						// выбираем первый результат
 *  SAPIImpl.selectCurrentVoice();							// выбираем этот голосовой токен
 *  SAPIImpl.speak("Это голосовой движок Ирины и Алана, it's right!",SAPIImpl.SPF_IS_NOT_XML);
 * Данный код запустит поиск голоса Irina+Alan (русский - Irina, английский - Alan), из полученного списка выберет первый голос
 * и произнесет фразу не асинхронно (по умолчанию), не анализируя полученнцю строку как xml
 *
 * Синтаксис строки а формате xml, позволяющий управлять параметрами произношения можно посмотреть в документации msdn по ссылке
 * https://msdn.microsoft.com/en-us/library/ee431815%28v=vs.85%29.aspx
 * Пример замедления - "<rate absspeed='-5'>Медленно</rate>"
 * Пример понижения тона - "<pitch absmiddle='-5'>Ниже тоном</pitch>"
 * Пример выделения голосом - "<emph>Выделить!</emph>"
 *
 * todo: добавить возможность создания нескольких экземпляров объекта (поддержка одновременно нескольких голосовых интерфейсов, в т.ч. одновременно говорящих)
 * todo: добавить поддержку многопоточности или контроль за синхронностью вызовов 
 */
class SAPIImpl extends SAPIImpl_constants
{
    /**
     * Во время загрузки библиотеки однократно происходит иннициализация SAPI
     * todo: корректная обработка ошибок иннициализации SAPI  
     */
    static
    {
	// загружаем динамическу библиотеку под текущую архитектуру (32 или 64 бита)
    	System.loadLibrary("SAPIImpl."+System.getProperty("sun.arch.data.model"));
    }

    static int idCounter = 0;
    int id = 0;

    SAPIImpl()
    {
	id = idCounter++;
    }

    /**
     * Переходит к следующему голосовому токену (при первом вызове к первому) и возвращает его идентификатор
     * Должен быть использован после вызова searchVoiceByAttributes или предыдущего успешного getNextVoiceIdFromList
     * Метод необходимо использовать перед вызовом selectCurrentVoice() 
     * @return строку с текущим идентификатором голосового токена либо nul, если больше голосовых токенов в списке нет
     */
    native public String getNextVoiceIdFromList();

    /**
     * Возвращает строку описанием голосового токена, идентификатор которого в последний раз был получен методом getNextVoiceIdFromList()  
     * @return строка с описанием голосового токена (обычно это наименование)
     */
    native public String getLastVoiceDescription();

    /**
     * Переходит к следующему голосовому движку и возвращает его идентификатор
     * Метод необходимо вызвать как минимум для получения первого движка из списка результатов поиска searchVoiceByAttributes  
     * @return null - если больше нет голосовых токенов или строковый идентификатор движка	
     */
    native public int selectCurrentVoice();

    /**
     * Выбор голосового токена (комбинация голосовых движков для используемых в операционной системе языков,
     * например комбинация двух - английский и русский)
     * todo: проверить, как голосовые движки работают в мультиязычной среде с поддержкой трех и более языков
     * @param   id	строковый идентификатор токена (в SAPI это результат вызова GetId(&id) для объекта CComPtr<ISpObjectToken>)
     * @return	0 если успех и 1 - если не выбран голос
     */
    native public int selectVoiceById(String id);

    /**
     * Выбор голосового токена по критерию с помощью атрибутов. Наименование атрибутов и их значения определяются голосовыми движками.
     * В результате метода сбрасывается текущее перечисление списка голосовых движков и устанавливается новое, в соответствии с заданным критерием
     * @param cond	Список значений атрибутов, например Name=Alena, Language=409, Gender=male и т.п., разделенные ';', возможно пустое значение null - отсутствие фильтров
     * подробности можно узнать тут https://msdn.microsoft.com/en-us/library/ms717036%28v=vs.85%29.aspx
     * @return	@return	количество голосов, удовлетворяющих условию или -1 - если ошибка
     */
    native public int searchVoiceByAttributes(String cond);

    /**
     * Отсылает текстовую строку для синтеза речи с помощью SAPI, текущий голосовой токен уже должен быть выбран
     * Сообщение будет произнесено с использованием текущего звукового устройства 'по умолчанию',
     * если предыдущее сообщение было отослано в асинхронном режиме 
     * @param 	text	Содержит сообщение, для которого необходимо синтезировать голос
     * @param	flags	Битовая маска, определяет параметры, такие как асинхронный вызов или наличие xml marckup 
     * @return	0 если успех и 1 если произошла ошибка
     */
    native public int speak(String text, int flags);

    /**
     * Отсылает текстовую строку для синтеза речи с помощью SAPI, текущий голосовой токен уже должен быть выбран
     * Сообщение будет произнесено с использованием текущего звукового устройства 'по умолчанию',
     * если предыдущее сообщение было отослано в асинхронном режиме
     * Используются параметры по умолчанию - Асинхронный режим и XML Marckup 
     * @return	0 если успех и 1 если произошла ошибка
     */
    public int speak(String text) {return speak(text,SPF_ASYNC|SPF_IS_XML);}

    /**
     * Выбирает текущее устройство для вывода синтезированного звука
     * @param 	stream	null - для выбора звуковой карты по умолчанию, иначе путь до wav файла
     * @param	flags	формат звука в файле 
     * @return	0 если успех и 1 если произошла ошибка
     */
    native public int stream(String stream, int flags);

    /**
     * устанавливает скорость речи
     * @param	rate	значение от -10 до +10, медленное и быстрое соответственно
     * @return	0	если успех и 1 если произошла ошибка
     */
    native public int rate(int rate);

    /**
     * устанавливает громкость речи
     * @param	pitch	значение от 0 до 100
     * @return	0	если успех и 1 если произошла ошибка
     */
    native public int pitch(int pitch);

    /**
     * ожидает окончания синтеза речи, если был выбран асинхронный режим
     * @param	timeout	таймаут ожидания в миллисекундах
     * @return	0 если успех и 1 если произошла ошибка
     */
    native public int wait(int timeout);
}
