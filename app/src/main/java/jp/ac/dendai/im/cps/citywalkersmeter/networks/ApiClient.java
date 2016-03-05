package jp.ac.dendai.im.cps.citywalkersmeter.networks;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class ApiClient {
    private static final String TAG = ApiClient.class.getSimpleName();
    private ApiClient own;

    public ApiClient() {
        this.own = this;
    }

    public void postUserId(String projectId) {
        HttpUrl.Builder builder = UrlBuilder.buildUsersRegistUrl();
        RequestBody formBody = new FormBody.Builder()
                .add("projectId", projectId)
                .build();
        post(builder, formBody);
    }

    public void updateLog(String userId, String data) {
        HttpUrl.Builder builder = UrlBuilder.buildLogsUpdate();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", userId)
                .add("logs", data)
                .build();
        post(builder, formBody);
    }

    private void post(HttpUrl.Builder builder, RequestBody body) {
        final Request request = new Request.Builder().url(builder.build()).post(body).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                own.onFailure(request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                own.onResponse(response);
            }
        });
    }

    public abstract void onFailure(Request request, IOException e);
    public abstract void onResponse(Response response) throws IOException;
}
