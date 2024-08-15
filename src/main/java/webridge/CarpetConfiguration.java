package webridge;

import com.sk89q.worldedit.util.PropertiesConfiguration;

import java.io.File;

class CarpetConfiguration extends PropertiesConfiguration {

    public CarpetConfiguration(File path) {
        super(path);
    }

    public int getPermissionLevel(String perm) {
        String val = properties.getProperty("permission." + perm, "2");
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 2;
        }
    }

}
