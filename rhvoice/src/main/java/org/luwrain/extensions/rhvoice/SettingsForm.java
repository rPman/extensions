package org.luwrain.extensions.rhvoice;

import org.luwrain.core.*;
import org.luwrain.core.events.*;
import org.luwrain.controls.*;
import org.luwrain.cpanel.*;

class SettingsForm extends FormArea implements SectionArea
{
    private ControlPanel controlPanel;
    private Luwrain luwrain;
    private Settings settings;
    private Strings strings;

    SettingsForm(ControlPanel controlPanel, Strings strings, String path)
    {
	super(new DefaultControlEnvironment(controlPanel.getCoreInterface()), strings.formName());
	this.controlPanel = controlPanel;
	this.luwrain = controlPanel.getCoreInterface();
	this.settings = Settings.create (luwrain.getRegistry(), path);
	this.strings = strings;
	fillForm();
    }

    private void fillForm()
    {
	addEdit("name", strings.formChannelName(), settings.getName(""));
	addEdit("datapath", strings.formHost(), settings.getVoiceName(""));
	addCheckbox("default", strings.formDefault(), settings.getDefault(false));
    }

    @Override public boolean onKeyboardEvent(KeyboardEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onKeyboardEvent(event))
	    return true;
	return super.onKeyboardEvent(event);
    }

    @Override public boolean onEnvironmentEvent(EnvironmentEvent event)
    {
	NullCheck.notNull(event, "event");
	if (controlPanel.onEnvironmentEvent(event))
	    return true;
	return super.onEnvironmentEvent(event);
    }

    @Override public boolean saveSectionData()
    {
	return true;
    }

    static SettingsForm create(ControlPanel controlPanel, String path)
    {
	NullCheck.notNull(controlPanel , "controlPanel");
	NullCheck.notNull(path, "path");
	final Strings strings = (Strings)controlPanel.getCoreInterface().i18n().getStrings(Strings.NAME);
	return new SettingsForm(controlPanel, strings, path);
    }
}
