package com.zjtec.admin.common;

import java.io.Serializable;

/**
 * 统一响应体（拓展 · 不计入考核，但实验六的 AJAX 强烈建议用它）。
 *
 * <p><b>解决什么问题：</b>不用它的时候，Controller 直接返回一个
 * {@code Map<String, Object>}，前端只能靠"猜字段名"取值 ——
 * 这个接口叫 {@code msg}、那个接口叫 {@code message}、另一个叫 {@code error}，
 * 前端每接一个接口就要读一遍后端代码。</p>
 *
 * <p><b>用它之后：</b>所有接口的 JSON 结构固定为三段</p>
 * <pre>
 * {
 *   "code": 200,          // 业务状态码：200 成功，其他失败
 *   "msg":  "注册成功",    // 给人看的提示，前端可直接显示
 *   "data": { ... }       // 真正的业务数据，没有就为 null
 * }
 * </pre>
 *
 * <p><b>对照 ASP.NET：</b>相当于统一的 {@code ApiResponse<T>} 包装，
 * 或在 ASP.NET Core 里对 {@code IActionResult} 做统一返回封装。</p>
 *
 * <p><b>注意：</b>HTTP 状态码与这里的 {@code code} 是两回事。
 * HTTP 200 表示"请求送到了"，业务是否成功看 {@code code}。
 * 这样前端只需处理一种成功分支：{@code if (json.code === 200)}。</p>
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码：成功 */
    public static final int CODE_OK = 200;
    /** 业务状态码：参数错误 */
    public static final int CODE_BAD_REQUEST = 400;
    /** 业务状态码：业务失败（如用户名已存在） */
    public static final int CODE_FAIL = 500;

    private int code;
    private String msg;
    private T data;

    public Result() {
        // Jackson 反序列化需要无参构造
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // ==================== 静态工厂方法：让调用处读起来像句子 ====================

    /** 成功，无数据。用法：{@code return Result.ok("注册成功");} */
    public static <T> Result<T> ok(String msg) {
        return new Result<>(CODE_OK, msg, null);
    }

    /** 成功，带数据。用法：{@code return Result.ok("查询成功", list);} */
    public static <T> Result<T> ok(String msg, T data) {
        return new Result<>(CODE_OK, msg, data);
    }

    /** 失败。用法：{@code return Result.fail("用户名或密码错误");} */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(CODE_FAIL, msg, null);
    }

    /** 失败，自定义状态码。 */
    public static <T> Result<T> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    /** 是否成功（前端也可直接读 JSON 里的 success 字段） */
    public boolean isSuccess() {
        return this.code == CODE_OK;
    }

    // ==================== getter / setter（Jackson 序列化靠它们） ====================

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Result{code=" + code + ", msg='" + msg + "', data=" + data + '}';
    }
}
