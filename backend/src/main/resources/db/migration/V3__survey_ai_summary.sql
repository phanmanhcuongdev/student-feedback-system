CREATE TABLE [dbo].[Survey_AI_Summary] (
    [summary_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [source_hash] NVARCHAR(64) NOT NULL,
    [model_name] NVARCHAR(100) NOT NULL,
    [comment_count] INT NOT NULL,
    [summary_text] NVARCHAR(MAX) NOT NULL,
    [highlights_json] NVARCHAR(MAX) NULL,
    [concerns_json] NVARCHAR(MAX) NULL,
    [actions_json] NVARCHAR(MAX) NULL,
    [created_by_user_id] INT NOT NULL,
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyAISummary_CreatedAt] DEFAULT (GETDATE()),
    CONSTRAINT [PK_Survey_AI_Summary] PRIMARY KEY ([summary_id]),
    CONSTRAINT [FK_SurveyAISummary_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id])
);
GO

CREATE TABLE [dbo].[Survey_AI_Summary_Job] (
    [job_id] INT IDENTITY(1,1) NOT NULL,
    [survey_id] INT NOT NULL,
    [source_hash] NVARCHAR(64) NOT NULL,
    [comment_count] INT NOT NULL,
    [status] NVARCHAR(20) NOT NULL,
    [requested_by_user_id] INT NOT NULL,
    [summary_id] INT NULL,
    [created_at] DATETIME NOT NULL CONSTRAINT [DF_SurveyAISummaryJob_CreatedAt] DEFAULT (GETDATE()),
    [started_at] DATETIME NULL,
    [finished_at] DATETIME NULL,
    [error_message] NVARCHAR(MAX) NULL,
    CONSTRAINT [PK_Survey_AI_Summary_Job] PRIMARY KEY ([job_id]),
    CONSTRAINT [FK_SurveyAISummaryJob_Survey] FOREIGN KEY ([survey_id]) REFERENCES [dbo].[Survey]([survey_id]),
    CONSTRAINT [FK_SurveyAISummaryJob_Summary] FOREIGN KEY ([summary_id]) REFERENCES [dbo].[Survey_AI_Summary]([summary_id])
);
GO

CREATE INDEX [IX_SurveyAISummary_SurveyId_CreatedAt]
    ON [dbo].[Survey_AI_Summary] ([survey_id], [created_at] DESC);
GO

CREATE INDEX [IX_SurveyAISummaryJob_SurveyId_CreatedAt]
    ON [dbo].[Survey_AI_Summary_Job] ([survey_id], [created_at] DESC);
GO
