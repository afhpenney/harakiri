package me.vaxry.harakiri.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.vaxry.harakiri.Harakiri;
import me.vaxry.harakiri.api.config.Configurable;
import me.vaxry.harakiri.api.util.FileUtil;
import me.vaxry.harakiri.impl.module.render.SearchModule;

import java.io.File;
import java.util.Objects;

/**
 * @author noil
 */
public final class SearchConfig extends Configurable {

    private final SearchModule searchModule;

    public SearchConfig(File dir) {
        super(FileUtil.createJsonFile(dir, "SearchIds"));
        this.searchModule = (SearchModule) Harakiri.INSTANCE.getModuleManager().find("Search");
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (this.searchModule == null)
            return;

        JsonArray searchIdsJsonArray = null;

        final JsonElement blockIds = this.getJsonObject().get("SearchBlockIds");
        if (blockIds != null)
            searchIdsJsonArray = blockIds.getAsJsonArray();

        if (searchIdsJsonArray != null) {
            for (JsonElement jsonElement : searchIdsJsonArray) {
                ((SearchModule) Objects.requireNonNull(Harakiri.INSTANCE.getModuleManager().find("Search"))).add(jsonElement.getAsInt());
            }
        }
    }

    @Override
    public void onSave() {
        if (this.searchModule == null)
            return;

        JsonObject save = new JsonObject();

        JsonArray searchIdsJsonArray = new JsonArray();
        for (Integer i : this.searchModule.getIds())
            searchIdsJsonArray.add(i);

        save.add("SearchBlockIds", searchIdsJsonArray);

        this.saveJsonObjectToFile(save);
    }
}