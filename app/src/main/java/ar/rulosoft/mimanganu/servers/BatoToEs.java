package ar.rulosoft.mimanganu.servers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;
import ar.rulosoft.mimanganu.utils.Util;

/**
 * Created by Raul on 03/02/2017.
 */

public class BatoToEs extends BatoTo {
    BatoToEs(Context context) {
        super(context);
        this.setFlag(R.drawable.flag_es);
        this.setIcon(R.drawable.batoto);
        this.setServerName("BatoTo(ES)");
        setServerID(ServerBase.BATOTOES);
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        try {
            if (manga.getChapters().size() == 0 || forceReload) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String user = prefs.getString("username_" + getServerName(), "");
                String password = prefs.getString("dwp_" + getServerName(), "");
                String data = getNavigatorAndFlushParameters().get(manga.getPath(), new BatotoLoginInterceptor(user, password));
                String synopsis = getFirstMatchDefault("Description:</td>\\s+<td>(.*?)</td>", data, context.getString(R.string.nodisponible));
                manga.setSynopsis(Util.getInstance().fromHtml(synopsis).toString());
                manga.setImages(getFirstMatchDefault("(http://img\\.bato\\.to/forums/uploads.+?)\"", data, ""));
                manga.setAuthor(getFirstMatchDefault("search\\?artist_name=.+?>([^<]+)", data, "n/a"));
                manga.setGenre(getFirstMatchDefault("Genres:</td>\\s+<td>([\\s\\S]+?)<img[^>]+?alt=.edit", data, "").replaceAll("<.*?>", "").replaceAll(",[\\s]*", ",").trim());
                manga.setFinished(!getFirstMatchDefault("Status:<\\/td>\\s+<td>([^<]+)", data, "").contains("Ongoing"));
                ArrayList<Chapter> chapters = new ArrayList<>();
                Pattern pattern = Pattern.compile("<a href=\"([^\"]+)\" title=\"[^\"]+\">.+?>([^<]+).+?title=\"(.+?)\".+?<a[^>]+>([^<]+)", Pattern.DOTALL);
                data = getFirstMatchDefault("ipb_table chapters_list\"([\\s\\S]+?)</table", data, "");
                Matcher matcher = pattern.matcher(data);
                String lang = "Spanish";
                while (matcher.find()) {
                        if (matcher.group(3).contains(lang))
                            chapters.add(0, new Chapter("(" + matcher.group(3) + ") " + matcher.group(2) + " [" + matcher.group(4) + "]", matcher.group(1)));
                }
                manga.setChapters(chapters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
