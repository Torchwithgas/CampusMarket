package com.yjq.programmer.enums;

public enum AddressSelectEnum {

    YES(1,"是"),

    NO(2,"否"),

    ;

    Integer code;

    String desc;

    AddressSelectEnum(Integer code, String desc) {
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
