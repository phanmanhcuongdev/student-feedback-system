USE [SURVEY_SYSTEM_DEV];
GO

ALTER TABLE [dbo].[Survey]
ADD [lifecycle_state] NVARCHAR(20) NOT NULL
    CONSTRAINT [DF_Survey_LifecycleState] DEFAULT ('DRAFT');
GO

UPDATE [dbo].[Survey]
SET [lifecycle_state] = CASE
    WHEN [end_date] IS NOT NULL AND [end_date] < GETDATE() THEN 'CLOSED'
    ELSE 'PUBLISHED'
END;
GO

ALTER TABLE [dbo].[Survey]
ADD CONSTRAINT [CHK_Survey_LifecycleState]
CHECK ([lifecycle_state] IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'ARCHIVED'));
GO
