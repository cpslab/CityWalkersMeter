package jp.ac.dendai.im.cps.citywalkersmeter.networks;

import jp.ac.dendai.im.cps.citywalkersmeter.BuildConfig;
import okhttp3.HttpUrl;

public class UrlBuilder {
    private static final String TAG = UrlBuilder.class.getSimpleName();

    public static final String SCHEME = BuildConfig.CITYLOG_SCHEME;
    public static final String HOST = BuildConfig.CITYLOG_HOST;
    public static final String ROOT = BuildConfig.CITYLOG_ROOT;
    public static final String USERS = BuildConfig.CITYLOG_USERS;
    public static final String REGIST = BuildConfig.CITYLOG_REGIST;
    public static final String LOGS = BuildConfig.CITYLOG_LOGS;
    public static final String UPDATE = BuildConfig.CITYLOG_UPDATE;

    /**
     * /api/
     * @return HttpUrl.Builder
     */
    private static HttpUrl.Builder buildRootUrl() {
        return new HttpUrl.Builder()
                .scheme(SCHEME)
                .host(HOST)
                .addPathSegment(ROOT);
    }

    /**
     * /api/users/regist/
     * @return HttpUrl.Builder
     */
    public static HttpUrl.Builder buildUsersRegistUrl() {
        return buildRootUrl().addPathSegment(USERS).addPathSegment(REGIST);
    }

    /**
     * /api/log/update/
     * @return HttpUrl.Builder
     */
    public static HttpUrl.Builder buildLogsUpdate() {
        return buildRootUrl().addPathSegment(LOGS).addPathSegment(UPDATE);
    }
}
