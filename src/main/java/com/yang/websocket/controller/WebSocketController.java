package com.yang.websocket.controller;

import com.alibaba.fastjson.JSONObject;
import com.yang.websocket.config.WebSocketServer;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ：yangxinan
 * @description :
 * @date ：2022/10/12 10:23
 **/
@RestController
@RequestMapping("/ws")
public class WebSocketController {
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 消息发送
     *
     * @param msg
     * @param userId
     */
    @GetMapping("/send/{userId}/{msg}")
    public void send(@PathVariable String msg, @PathVariable String userId) {
        webSocketServer.sendMessage(msg, Long.valueOf(userId));
    }

    /**
     * 群发消息
     *
     * @param msg
     */
    @GetMapping("/sendMassMessage/{msg}")
    public void sendMassMessage(@PathVariable String msg) {
        WebsocketResponse response = new WebsocketResponse();
        response.setTitle("群发主题");
        response.setMessage(msg);
        webSocketServer.sendMassMessage(JSONObject.toJSONString(response));
    }

    @Data
    @Accessors(chain = true)
    public static class WebsocketResponse {
        private String title;
        private String message;
        private String userId;
        private String userName;
        private int age;
    }

}
