IF OBJECT_ID(N'dbo.Audit_Log', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Audit_Log] (
        [audit_id] INT IDENTITY(1,1) NOT NULL,
        [actor_user_id] INT NOT NULL,
        [action_type] NVARCHAR(50) NOT NULL,
        [target_type] NVARCHAR(30) NOT NULL,
        [target_id] INT NOT NULL,
        [summary] NVARCHAR(255) NOT NULL,
        [details] NVARCHAR(MAX) NULL,
        [old_state] NVARCHAR(255) NULL,
        [new_state] NVARCHAR(255) NULL,
        [created_at] DATETIME NOT NULL CONSTRAINT [DF_AuditLog_CreatedAt] DEFAULT (GETDATE()),
        CONSTRAINT [PK_Audit_Log] PRIMARY KEY ([audit_id]),
        CONSTRAINT [FK_AuditLog_ActorUser] FOREIGN KEY ([actor_user_id]) REFERENCES [dbo].[User]([user_id])
    );
END
GO
