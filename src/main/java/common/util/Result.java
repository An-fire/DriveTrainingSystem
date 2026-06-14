package common.util;

/**
 * 统一返回结果封装
 */
public class Result {
    private boolean success;
    private String msg;
    private Object data;

    public static Result success() {
        Result r = new Result();
        r.success = true;
        r.msg = "操作成功";
        return r;
    }

    public static Result success(Object data) {
        Result r = success();
        r.data = data;
        return r;
    }

    public static Result error(String msg) {
        Result r = new Result();
        r.success = false;
        r.msg = msg;
        return r;
    }

    // getter & setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}