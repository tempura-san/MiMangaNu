package ar.rulosoft.mimanganu.servers;

import android.content.Context;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.rulosoft.mimanganu.R;
import ar.rulosoft.mimanganu.componentes.Chapter;
import ar.rulosoft.mimanganu.componentes.Manga;

class SubManga extends ServerBase {
    private static final String HOST = "http://submanga.com";

    SubManga(Context context) {
        super(context);
        setFlag(R.drawable.flag_es);
        setIcon(R.drawable.submanga_icon);
        setServerName("SubManga");
        setServerID(SUBMANGA);
    }

    @Override
    public boolean hasList() {
        return true;
    }

    @Override
    public ArrayList<Manga> getMangas() throws Exception {
        ArrayList<Manga> mangas = new ArrayList<>();
        String source = getNavigatorAndFlushParameters().get(HOST + "/series");
        Pattern p = Pattern.compile("<td><a href=\"(http://submanga.com/.+?)\".+?</b>(.+?)<", Pattern.DOTALL);
        Matcher m = p.matcher(source);
        while (m.find()) {
            mangas.add(new Manga(getServerID(), m.group(2), m.group(1), false));
        }
        return mangas;
    }

    @Override
    public boolean hasFilteredNavigation() {
        return false;
    }

    @Override
    public ArrayList<Manga> search(String term) throws Exception {
        return null;
    }

    @Override
    public void loadChapters(Manga manga, boolean forceReload) throws Exception {
        if (manga.getChapters().isEmpty() || forceReload) {
            String data = getNavigatorAndFlushParameters().get((manga.getPath() + "/completa"));
            Pattern p = Pattern.compile("<tr[^>]*><td[^>]*><a href=\"http://submanga.com/([^\"|#]+)\">(.+?)</a>", Pattern.DOTALL);
            Matcher m = p.matcher(data);

            while (m.find()) {
                String web = HOST + "/c" + m.group(1).substring(m.group(1).lastIndexOf("/"));
                Chapter chapter = new Chapter(m.group(2), web);
                chapter.addChapterFirst(manga);
            }
        }
    }

    @Override
    public void loadMangaInformation(Manga manga, boolean forceReload) throws Exception {
        String data = getNavigatorAndFlushParameters().get((manga.getPath()));

        Pattern p = Pattern.compile("<img src=\"(http://.+?)\"/><p>(.+?)</p>", Pattern.DOTALL);
        Matcher m = p.matcher(data);

        // Cover and Summary
        if (m.find()) {
            manga.setImages(m.group(1));
            manga.setSynopsis(m.group(2));
        } else {
            manga.setSynopsis(context.getString(R.string.nodisponible));
        }
        // Author
        manga.setAuthor(getFirstMatchDefault("<p>Creado por (.+?)</p>", data, context.getString(R.string.nodisponible)));
        // Genre
        manga.setGenre(getFirstMatchDefault("(<a class=\"b\" href=\"http://submanga.com/ge.+?</p>)", data, context.getString(R.string.nodisponible)));
        // Chapters
        loadChapters(manga, forceReload);
    }

    @Override
    public String getPagesNumber(Chapter chapter, int page) {
        return chapter.getPath() + "/" + page;
    }

    @Override
    public String getImageFrom(Chapter chapter, int page) throws Exception {
        String data;
        data = getNavigatorAndFlushParameters().get(getPagesNumber(chapter, page));
        data = getFirstMatchDefault("<img[^>]+src=\"(http:\\/\\/.+?)\"", data, "");
        return data;
    }

    @Override
    public void chapterInit(Chapter chapter) throws Exception {
        String data = getNavigatorAndFlushParameters().get(chapter.getPath());
        chapter.setPages(Integer.parseInt(getFirstMatch("(\\d+)<\\/option><\\/select>", data, "Error: failed to get number of pages")));
        if (chapter.getExtra() == null || chapter.getExtra().length() < 2) {
            data = getFirstMatchDefault("<img src=\"(http://.+?)\"", data, null);
            chapter.setExtra(data.substring(0, data.length() - 4));
        }
    }
}
