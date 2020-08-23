package pers.htc.customredis.model.common;

import lombok.Data;

@Data
public class Result {
    int code;
    Object data;

    public static Result ok(Object data) {
        Result r = new Result();
        r.setCode(200);
        r.setData(data);
        return r;
    }
}
