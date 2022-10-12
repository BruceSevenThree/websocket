package com.yang.websocket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author ：yangxinan
 * @Description : webSocket 服务
 * @date ：2022/10/12 9:45
 **/
@Slf4j
@Component
@ServerEndpoint("/websocket/{userId}")
public class WebSocketServer {
    /**
     * 在线人数
     */
    private static int onlineCount;
    /**
     * 当前会话
     */
    private Session session;
    /**
     * 用户唯一标识
     */
    private String userId;

    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    /**
     * concurrent包的线程安全set，用来存放每个客户端对应的MyWebSocket对象
     */
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    /**
     * 为了保存在线用户信息，在方法中新建一个list存储一下【实际项目依据复杂度，可以存储到数据库或者缓存】
     */
    private final static List<Session> SESSIONS = Collections.synchronizedList(new ArrayList<>());

    /**
     * 建立连接
     *
     * @param session
     * @param userId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        webSocketSet.add(this);
        SESSIONS.add(session);

        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
        } else {
            webSocketMap.put(userId, this);
            addOnlineCount();
        }
        log.info("[连接ID：{}] 建立连接，当前连接数：{}", userId, getOnlineCount());
    }

    /**
     * 断开连接
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            subOnlineCount();
        }
        log.info("[连接ID：{}] 断开连接，当前连接数：{}", userId, getOnlineCount());
    }

    /**
     * 发送错误
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.info("[连接ID：{}] 错误原因：{}", this.userId, error.getMessage());
        error.printStackTrace();
    }

    /**
     * 收到消息
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("[连接ID：{}] 收到消息：{}", this.userId, message);
    }

    /**
     * 发送消息
     *
     * @param message
     * @param userId
     */
    public void sendMessage(String message, Long userId) {
        WebSocketServer webSocketServer = webSocketMap.get(String.valueOf(userId));
        if (null != webSocketServer) {
            log.info("[websocket消息] 推送消息,[toUser]userId={},message={}", userId, message);
            try {
                webSocketServer.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("[连接ID:{}] 发送消息失败, 消息:{}", this.userId, message, e);
            }
        }
    }

    /**
     * 群发消息
     *
     * @param message
     */
    public void sendMassMessage(String message) {
        try {
            for (Session session1 : SESSIONS) {
                if (session1.isOpen()) {
                    session1.getBasicRemote().sendText(message);
                    log.info("[连接ID:{}] 发送消息:{}", session.getRequestParameterMap().get("userId"), message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前连接数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 当前连接数+1
     */
    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    /**
     * 当前连接数-1
     */
    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }
}
