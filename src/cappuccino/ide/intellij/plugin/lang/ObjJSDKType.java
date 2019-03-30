package cappuccino.ide.intellij.plugin.lang;
import com.intellij.openapi.projectRoots.*;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class ObjJSDKType extends SdkType {

    private static final String SDK_TYPE_ID = "ObjJ Framework";
    private static final String SDK_NAME = "Objective-J Framework";
    private static final String PLIST_INFO_NAME = "info.plist";

    public ObjJSDKType() {
        super(SDK_TYPE_ID);
    }

    @Nullable
    @Override
    public String suggestHomePath() {
        return null;
    }

    @Override
    public boolean isValidSdkHome(String sdkPath) {
        File file = new File(sdkPath, PLIST_INFO_NAME);
        return file.exists();
    }

    @NotNull
    @Override
    public String suggestSdkName(String currentSdkName, String sdkHome) {
        File plist = new File(sdkHome, PLIST_INFO_NAME);
        try {
            FileReader fileReader = new FileReader(plist);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null && !line.contains("<key>CPBundleName</key>")) {
                line = bufferedReader.readLine();
            }
            line = line != null ? bufferedReader.readLine() : null;
            if (line == null) {
                return "";
            }
            return line.replace("<string>", "").replace("</string>", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Nullable
    @Override
    public AdditionalDataConfigurable createAdditionalDataConfigurable(
            @NotNull
                    SdkModel sdkModel,
            @NotNull
                    SdkModificator sdkModificator) {
        return null;
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return SDK_NAME;
    }

    @Override
    public void saveAdditionalData(
            @NotNull
                    SdkAdditionalData sdkAdditionalData,
            @NotNull
                    Element element) {

    }
}
