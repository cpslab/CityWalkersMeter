package jp.ac.dendai.im.cps.citywalkersmeter.networks;

import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

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
        Log.d(TAG, "updateLog: " + data);
        HttpUrl.Builder builder = UrlBuilder.buildLogsUpdate();
//        RequestBody requestBody = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("userId", userId)
//                .addFormDataPart("logs", "[" + data + "]")
//                .build();
        RequestBody requestBody = new FormBody.Builder()
                .addEncoded("userId", userId)
                .addEncoded("logs", "[" + data + "]")
                .build();
        post(builder, requestBody);
    }

    private void post(HttpUrl.Builder builder, RequestBody body) {
        final Request request = new Request.Builder().url(builder.build()).addHeader("Content-type", "application/json").post(body).build();

        Log.d(TAG, "post: " + bodyToString(request));

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

    private static String bodyToString(final Request request){

        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    public abstract void onFailure(Request request, IOException e);
    public abstract void onResponse(Response response) throws IOException;
}
