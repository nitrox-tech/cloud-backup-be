package com.nitro.tech.cloud.service;

/**
 * Shareable-archive invite payload (deep link / JSON): backend root folder id + Telegram supergroup chat id.
 */
public record ArchiveInviteLink(String inviteUrl, String folderId, String telegramChatId) {}
