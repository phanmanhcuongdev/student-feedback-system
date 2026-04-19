ALTER TABLE [dbo].[Notification]
ADD
    [type] VARCHAR(50) NULL,
    [title] NVARCHAR(150) NULL,
    [survey_id] INT NULL,
    [action_label] NVARCHAR(80) NULL,
    [created_by_user_id] INT NULL,
    [metadata] NVARCHAR(MAX) NULL,
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_Notification_CreatedAt] DEFAULT (GETDATE());
GO

UPDATE [dbo].[Notification]
SET
    [type] = COALESCE([type], 'GENERAL'),
    [title] = COALESCE([title], 'Notification');
GO

ALTER TABLE [dbo].[Notification]
ALTER COLUMN [type] VARCHAR(50) NOT NULL;
GO

ALTER TABLE [dbo].[Notification]
ALTER COLUMN [title] NVARCHAR(150) NOT NULL;
GO

ALTER TABLE [dbo].[Notification]
ADD CONSTRAINT [FK_Notification_Survey]
    FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]);
GO

ALTER TABLE [dbo].[Notification]
ADD CONSTRAINT [FK_Notification_CreatedByUser]
    FOREIGN KEY ([created_by_user_id]) REFERENCES [dbo].[User]([user_id]);
GO

ALTER TABLE [dbo].[Notification_User]
ADD
    [delivered_at] DATETIME NOT NULL CONSTRAINT [DF_NotificationUser_DeliveredAt] DEFAULT (GETDATE()),
    [read_at] DATETIME NULL;
GO

ALTER TABLE [dbo].[Notification_User]
ADD CONSTRAINT [UQ_NotificationUser_NotificationUser]
    UNIQUE ([noti_id], [user_id]);
GO

CREATE INDEX [IX_NotificationUser_UserRead]
ON [dbo].[Notification_User] ([user_id], [read_at], [delivered_at]);
GO

CREATE INDEX [IX_Notification_TypeSurvey]
ON [dbo].[Notification] ([type], [survey_id], [created_at]);
GO
