IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_NotificationUser_UserUnread'
      AND object_id = OBJECT_ID(N'[dbo].[Notification_User]')
)
BEGIN
    CREATE INDEX [IX_NotificationUser_UserUnread]
    ON [dbo].[Notification_User] ([user_id])
    INCLUDE ([noti_id], [delivered_at])
    WHERE [read_at] IS NULL;
END;
GO
