
package org.luwrain.extensions.mssapi;

import org.luwrain.core.*;

interface Settings
{
    String getHost(String defValue);
    void setHost(String value);
    int getPort(int defValue);
    void setPort(int value);
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
