package com.yjq.programmer.enums;

public enum ProductStateEnum {

    WAIT(1,"待审核"),

    SUCCESS(2,"审核通过"),

    FAIL(3,"审核不通过"),

    DOWN(4,"已下架")

    ;

    Integer code;

    String desc;

    ProductStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
