package jp.ac.dendai.im.cps.citywalkersmeter;

import android.os.Handler;
import android.os.Message;

/**
 * Created by naoya on 2015/08/23.
 */
public abstract class HttpPostHandler extends Handler{

    private static String HTTP_POST_SUCCESS = "http_post_success";
    private static String HTTP_RESPONSE = "http_response";

    public void handleMessage(Message msg) {
        boolean isPostSuccess = msg.getData().getBoolean(HTTP_POST_SUCCESS);
        String http_response = msg.getData().getString(HTTP_RESPONSE);

        if (isPostSuccess) {
            onPostCompleted(http_response);
        } else {
            onPostFailed(http_response);
        }
    }

    /*
     * 通信成功時の処理
     */
    public abstract void onPostCompleted(String response);

    /*
     * 通信失敗時の処理を記述
     */
    public abstract void onPostFailed(String response);
}
