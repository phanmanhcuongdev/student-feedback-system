USE [SURVEY_SYSTEM_DEV];
GO

IF COL_LENGTH('dbo.Student', 'review_reason') IS NULL
BEGIN
    ALTER TABLE [dbo].[Student] ADD [review_reason] NVARCHAR(255) NULL;
END
GO

IF COL_LENGTH('dbo.Student', 'review_notes') IS NULL
BEGIN
    ALTER TABLE [dbo].[Student] ADD [review_notes] NVARCHAR(MAX) NULL;
END
GO

IF COL_LENGTH('dbo.Student', 'reviewed_by_user_id') IS NULL
BEGIN
    ALTER TABLE [dbo].[Student] ADD [reviewed_by_user_id] INT NULL;
END
GO

IF COL_LENGTH('dbo.Student', 'reviewed_at') IS NULL
BEGIN
    ALTER TABLE [dbo].[Student] ADD [reviewed_at] DATETIME NULL;
END
GO

IF COL_LENGTH('dbo.Student', 'resubmission_count') IS NULL
BEGIN
    ALTER TABLE [dbo].[Student]
        ADD [resubmission_count] INT NOT NULL
        CONSTRAINT [DF_Student_ResubmissionCount] DEFAULT ((0)) WITH VALUES;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_Student_ReviewedByUser'
)
BEGIN
    ALTER TABLE [dbo].[Student] WITH CHECK
    ADD CONSTRAINT [FK_Student_ReviewedByUser]
    FOREIGN KEY ([reviewed_by_user_id]) REFERENCES [dbo].[User]([user_id]);
END
GO
