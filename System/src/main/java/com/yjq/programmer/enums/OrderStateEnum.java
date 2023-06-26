package com.yjq.programmer.enums;

public enum OrderStateEnum {

    WAIT(1,"待支付"),

    PAY(2,"已支付"),

    SEND(3,"已发货"),

    RECEIVE(4,"已收货"),

    CANCEL(5,"已取消"),

    ;

    Integer code;

    String desc;

    OrderStateEnum(Integer code, String desc) {
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
