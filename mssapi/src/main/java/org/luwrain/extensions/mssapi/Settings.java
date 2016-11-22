
package org.luwrain.extensions.mssapi;

import org.luwrain.core.*;

interface Settings
{
    String getName(String defValue);
    void setName(String value);
    String getCond(String defValue);
    void setCond(String defValue);
    boolean getDefault(boolean defValue);
    void setDefault(boolean value);

    static Settings create(Registry registry, String path)
    {
	NullCheck.notNull(registry, "registry");
	NullCheck.notNull(path, "path");
	return RegistryProxy.create(registry, path, Settings.class);
    }
}
