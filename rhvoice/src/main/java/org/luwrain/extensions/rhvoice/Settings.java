package org.luwrain.extensions.rhvoice;

import org.luwrain.core.*;

interface Settings
{
    String getVoiceName(String defValue);
    void setVoiceName(String value);
    String getName(String defValue);
    void setName(String value);
    boolean getDefault(boolean defValue);
    void setDefault(boolean value);

    static Settings create(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	return RegistryProxy.create(registry, path, Settings.class);
    }
}
