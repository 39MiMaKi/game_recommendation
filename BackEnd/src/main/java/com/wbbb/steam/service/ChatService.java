package com.wbbb.steam.service;

import com.wbbb.steam.dto.response.data.ChatFriendDto;
import com.wbbb.steam.entity.ChatMessage;
import com.wbbb.steam.repository.ChatMessageRepository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 获取聊天好友列表
     * @param userId 用户ID
     * @return 聊天好友列表
     */
    public List<ChatFriendDto> getChatFriends(Long userId) {
        return chatMessageRepository.getChatFriends(userId);
    }

    /**
     * 发送聊天消息
     * @param fromId 发送者ID
     * @param toId 接收者ID
     * @param content 消息内容
     * @return 保存的聊天消息
     */
    public ChatMessage sendChatMessage(Long fromId, Long toId, String content) {
        ChatMessage message = new ChatMessage(fromId, toId, content);
        return chatMessageRepository.save(message);
    }

    /**
     * 获取聊天记录
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param id 消息ID（可选）
     * @param num 消息数量
     * @return 聊天记录列表
     */
    public List<ChatMessage> getChatMessages(Long userId, Long friendId, Long id, Integer num) {
        Pageable pageable = PageRequest.of(0, num);
        return id == null ? chatMessageRepository.getChatMessages(userId, friendId, pageable) : chatMessageRepository.getChatMessages(userId, friendId, id, pageable);
    }

    /**
     * 设置聊天消息为已读
     * @param userId 用户ID
     * @param friendId 好友ID
     * @param id 消息ID
     */
    @Transactional
    public void setChatMessagesRead(Long userId, Long friendId, Long id) {
        chatMessageRepository.setChatMessagesRead(userId, friendId, id);
    }
}
