package jp.ac.dendai.im.cps.citywalkersmeter.networks;

import okhttp3.HttpUrl;

public class UrlBuilder {
    private static final String TAG = UrlBuilder.class.getSimpleName();

    public static final String DON_SCHEME = "http";
    public static final String DON_HOST = "citylog.cps.im.dendai.ac.jp";
    public static final String ROOT = "api";
    public static final String USERS = "users";
    public static final String REGIST = "regist";
    public static final String LOGS = "logs";
    public static final String UPDATE = "update";

    /**
     * /api/
     * @return HttpUrl.Builder
     */
    private static HttpUrl.Builder buildRootUrl() {
        return new HttpUrl.Builder()
                .scheme(DON_SCHEME)
                .host(DON_HOST)
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
