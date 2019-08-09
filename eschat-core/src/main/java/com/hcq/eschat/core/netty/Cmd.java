package com.hcq.eschat.core.netty;

import lombok.Data;

@Data
public class Cmd {
    private int id;
    private CmdType cmdType;
    private OneWay oneWay;
    private Object object;
}
