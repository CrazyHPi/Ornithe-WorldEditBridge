package webridge;

import com.sk89q.worldedit.util.PropertiesConfiguration;

import java.io.File;

class VanillaConfiguration extends PropertiesConfiguration {

    public VanillaConfiguration(File path) {
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
